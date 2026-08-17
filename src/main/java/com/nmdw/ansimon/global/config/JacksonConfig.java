package com.nmdw.ansimon.global.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public JsonMapperBuilderCustomizer asiaSeoulInstantJsonMapperCustomizer() {
        return builder -> builder.addModule(instantModule());
    }

    private SimpleModule instantModule() {
        SimpleModule instantModule = new SimpleModule();
        instantModule.addSerializer(Instant.class, new AsiaSeoulInstantSerializer());
        instantModule.addDeserializer(Instant.class, new InstantDeserializer());
        return instantModule;
    }

    private static final class AsiaSeoulInstantSerializer extends ValueSerializer<Instant> {

        @Override
        public void serialize(Instant value, JsonGenerator generator, SerializationContext context) throws JacksonException {
            generator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value.atZone(ASIA_SEOUL)));
        }
    }

    private static final class InstantDeserializer extends ValueDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            return OffsetDateTime.parse(parser.getValueAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        }
    }
}