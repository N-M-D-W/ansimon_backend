package com.nmdw.ansimon.global.config;

import com.nmdw.ansimon.global.error.ExternalServiceException;
import com.nmdw.ansimon.global.webclient.WebClientErrorMapper;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties({ExternalApiProperties.class, WebClientTimeoutProperties.class})
public class WebClientConfig {

    private final WebClientErrorMapper errorMapper = new WebClientErrorMapper();

    @Bean
    public WebClient kmaWebClient(WebClient.Builder builder, ExternalApiProperties properties,
                                  WebClientTimeoutProperties timeoutProperties) {
        return build(builder, properties.kma(), timeoutProperties, "kma");
    }

    @Bean
    public WebClient shelterWebClient(WebClient.Builder builder, ExternalApiProperties properties,
                                      WebClientTimeoutProperties timeoutProperties) {
        return build(builder, properties.shelter(), timeoutProperties, "shelter");
    }

    @Bean
    public WebClient tmapWebClient(WebClient.Builder builder, ExternalApiProperties properties,
                                   WebClientTimeoutProperties timeoutProperties) {
        return build(builder, properties.tmap(), timeoutProperties, "tmap");
    }

    @Bean
    public WebClient mlWebClient(WebClient.Builder builder, ExternalApiProperties properties,
                                 WebClientTimeoutProperties timeoutProperties) {
        return build(builder, properties.ml(), timeoutProperties, "ml");
    }

    @Bean
    public WebClient phoneWebClient(WebClient.Builder builder, ExternalApiProperties properties,
                                    WebClientTimeoutProperties timeoutProperties) {
        return build(builder, properties.phone(), timeoutProperties, "phone");
    }

    private WebClient build(WebClient.Builder builder, ExternalApiProperties.Endpoint endpoint,
                            WebClientTimeoutProperties timeoutProperties, String serviceId) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeoutProperties.getConnectionTimeout().toMillis()))
                .responseTimeout(timeoutProperties.getResponseTimeout());
        WebClient.Builder configuredBuilder = builder.clone()
                .baseUrl(endpoint.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(mapExternalErrors(serviceId));
        if (StringUtils.hasText(endpoint.apiKey())) {
            configuredBuilder.defaultHeader("Authorization", "Bearer " + endpoint.apiKey());
        }
        return configuredBuilder.build();
    }

    private ExchangeFilterFunction mapExternalErrors(String serviceId) {
        return (request, next) -> {
            WebClientErrorMapper.ExternalRequestContext context =
                    WebClientErrorMapper.ExternalRequestContext.forService(serviceId, request.method());
            return next.exchange(request)
                    .flatMap(response -> response.statusCode().isError()
                            ? Mono.error(errorMapper.forStatus(response.statusCode(), context))
                            : Mono.just(response))
                    .onErrorMap(throwable -> !(throwable instanceof ExternalServiceException),
                            throwable -> errorMapper.forFailure(throwable, context));
        };
    }
}