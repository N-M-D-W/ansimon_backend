package com.nmdw.ansimon.global.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final ObjectMapper objectMapper = configuredObjectMapper();

    @Test
    void serializesInstantInAsiaSeoulWithIso8601Offset() throws Exception {
        String json = objectMapper.writeValueAsString(Instant.parse("2026-08-17T05:30:00Z"));

        assertThat(json).isEqualTo("\"2026-08-17T14:30:00+09:00\"");
    }

    @Test
    void deserializesIso8601OffsetToInstant() throws Exception {
        Instant instant = objectMapper.readValue("\"2026-08-17T14:30:00+09:00\"", Instant.class);

        assertThat(instant).isEqualTo(Instant.parse("2026-08-17T05:30:00Z"));
    }

    private ObjectMapper configuredObjectMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JacksonConfig().asiaSeoulInstantJsonMapperCustomizer().customize(builder);
        return builder.build();
    }
}