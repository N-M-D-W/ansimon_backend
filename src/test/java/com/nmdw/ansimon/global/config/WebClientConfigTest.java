package com.nmdw.ansimon.global.config;

import com.nmdw.ansimon.global.error.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebClientConfigTest {

    @Test
    void releasesErrorResponseBodyBeforeMappingExchangeFailure() {
        ClientResponse response = mock(ClientResponse.class);
        when(response.statusCode()).thenReturn(HttpStatus.BAD_GATEWAY);
        when(response.releaseBody()).thenReturn(Mono.empty());
        WebClient client = new WebClientConfig().kmaWebClient(
                WebClient.builder().exchangeFunction(request -> Mono.just(response)),
                endpoints(), new WebClientTimeoutProperties());

        assertThatThrownBy(() -> client.get().uri("/forecast?token=secret").exchangeToMono(ignored -> Mono.just("unexpected")).block())
                .isInstanceOf(ExternalServiceException.class);

        verify(response).releaseBody();
    }

    private ExternalApiProperties endpoints() {
        ExternalApiProperties.Endpoint endpoint = new ExternalApiProperties.Endpoint("https://provider.example.test", "");
        return new ExternalApiProperties(endpoint, endpoint, endpoint, endpoint, endpoint);
    }
}
