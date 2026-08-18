package com.nmdw.ansimon.elderly.infra;

import com.nmdw.ansimon.elderly.application.GeocodingResult;
import com.nmdw.ansimon.global.config.ExternalApiProperties;
import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.global.error.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class TmapGeocodingClientTest {

    @Test
    void geocodesAnAddressToCoordinatesAndSigunguCode() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.url().getPath()).isEqualTo("/tmap/geo/fullAddrGeo");
            assertThat(request.headers().getFirst("appKey")).isEqualTo("test-key");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("""
                    {"coordinateInfo":{"coordinate":[
                      {"lat":"37.57300001","lon":"126.97940001","legalDongCode":"1111010100"}
                    ]}}
                    """).build());
        }).build();
        ExternalApiProperties.Endpoint endpoint = new ExternalApiProperties.Endpoint("https://example.test", "test-key");
        TmapGeocodingClient client = new TmapGeocodingClient(webClient,
                new ExternalApiProperties(endpoint, endpoint, endpoint, endpoint, endpoint, endpoint));

        GeocodingResult result = client.geocode("서울시 종로구 1-1");

        assertThat(result.latitude()).isEqualByComparingTo("37.5730000");
        assertThat(result.longitude()).isEqualByComparingTo("126.9794000");
        assertThat(result.regionCode()).isEqualTo("11110");
    }

    @Test
    void rejectsAnEmptyCoordinateResultAsAnExternalServiceFailure() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"coordinateInfo\":{\"coordinate\":[]}}")
                        .build()
        )).build();
        ExternalApiProperties.Endpoint endpoint = new ExternalApiProperties.Endpoint("https://example.test", "test-key");
        TmapGeocodingClient client = new TmapGeocodingClient(webClient,
                new ExternalApiProperties(endpoint, endpoint, endpoint, endpoint, endpoint, endpoint));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.geocode("검색 결과가 없는 주소"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        exception -> assertThat(exception.errorCode())
                                .isEqualTo(ErrorCode.EXTERNAL_SERVICE_SERVER_ERROR));
    }
}
