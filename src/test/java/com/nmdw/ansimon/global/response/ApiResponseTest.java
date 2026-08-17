package com.nmdw.ansimon.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successPlacesPayloadInDataAndLeavesErrorEmpty() {
        ApiResponse<String> response = ApiResponse.success("guidance plan");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("guidance plan");
        assertThat(response.error()).isNull();
    }

    @Test
    void failurePlacesStructuredErrorInErrorAndLeavesDataEmpty() {
        ErrorResponse error = new ErrorResponse("GUIDANCE_NOT_FOUND", "Guidance plan was not found.");

        ApiResponse<Void> response = ApiResponse.failure(error);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isEqualTo(error);
    }
}
