package com.doeja.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void converteEnderecoDoNeonParaJdbc() {
        Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.converter(
                "postgresql://doeja_owner:s3nh%40forte@ep-abc-123.us-east-1.aws.neon.tech/doeja?sslmode=require&channel_binding=require");

        assertEquals("jdbc:postgresql://ep-abc-123.us-east-1.aws.neon.tech/doeja?sslmode=require&channel_binding=require",
                p.get("spring.datasource.url"));
        assertEquals("doeja_owner", p.get("spring.datasource.username"));
        assertEquals("s3nh@forte", p.get("spring.datasource.password"));
    }

    @Test
    void mantemAPortaQuandoInformada() {
        Map<String, Object> p = DatabaseUrlEnvironmentPostProcessor.converter("postgres://u:p@db.local:5433/app");
        assertEquals("jdbc:postgresql://db.local:5433/app", p.get("spring.datasource.url"));
    }

    @Test
    void recusaOutroBanco() {
        assertThrows(IllegalStateException.class,
                () -> DatabaseUrlEnvironmentPostProcessor.converter("mysql://u:p@h/db"));
    }
}
