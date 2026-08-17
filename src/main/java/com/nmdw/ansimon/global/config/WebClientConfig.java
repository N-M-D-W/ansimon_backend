package com.nmdw.ansimon.global.config;

import com.nmdw.ansimon.global.error.ExternalServiceException;
import com.nmdw.ansimon.global.webclient.WebClientErrorMapper;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper;
import reactor.core.publisher.Flux;
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(timeoutProperties.getConnectionTimeout().toMillis()))
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
                    .flatMap(response -> {
                        ClientResponse mappedResponse = new ErrorMappingClientResponse(response, errorMapper, context);
                        return response.statusCode().isError()
                                ? mappedResponse.releaseBody().then(Mono.<ClientResponse>error(
                                        errorMapper.forStatus(response.statusCode(), context)))
                                : Mono.just(mappedResponse);
                    })
                    .onErrorMap(throwable -> !(throwable instanceof ExternalServiceException),
                            throwable -> errorMapper.forFailure(throwable, context));
        };
    }

    private static final class ErrorMappingClientResponse extends ClientResponseWrapper {

        private final WebClientErrorMapper errorMapper;
        private final WebClientErrorMapper.ExternalRequestContext context;

        private ErrorMappingClientResponse(ClientResponse response, WebClientErrorMapper errorMapper,
                                           WebClientErrorMapper.ExternalRequestContext context) {
            super(response);
            this.errorMapper = errorMapper;
            this.context = context;
        }

        @Override
        public <T> Mono<T> bodyToMono(Class<? extends T> elementClass) {
            return map(super.bodyToMono(elementClass));
        }

        @Override
        public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> elementTypeRef) {
            return map(super.bodyToMono(elementTypeRef));
        }

        @Override
        public <T> Flux<T> bodyToFlux(Class<? extends T> elementClass) {
            return map(super.bodyToFlux(elementClass));
        }

        @Override
        public <T> Flux<T> bodyToFlux(ParameterizedTypeReference<T> elementTypeRef) {
            return map(super.bodyToFlux(elementTypeRef));
        }

        @Override
        public Mono<Void> releaseBody() {
            return map(super.releaseBody());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T body(BodyExtractor<T, ? super org.springframework.http.client.reactive.ClientHttpResponse> extractor) {
            try {
                T body = super.body(extractor);
                if (body instanceof Mono<?> mono) {
                    return (T) map(mono);
                }
                if (body instanceof Flux<?> flux) {
                    return (T) map(flux);
                }
                return body;
            } catch (ExternalServiceException exception) {
                throw exception;
            } catch (Throwable failure) {
                throw mapFailure(failure);
            }
        }

        private <T> Mono<T> map(Mono<T> body) {
            return body.onErrorMap(this::shouldMap, this::mapFailure);
        }

        private <T> Flux<T> map(Flux<T> body) {
            return body.onErrorMap(this::shouldMap, this::mapFailure);
        }

        private boolean shouldMap(Throwable failure) {
            return !(failure instanceof ExternalServiceException);
        }

        private ExternalServiceException mapFailure(Throwable failure) {
            return errorMapper.forFailure(failure, context);
        }
    }
}
