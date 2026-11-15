package com.doeja.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Aceita o banco no formato que Neon, Render, Heroku etc. entregam:
 *   DATABASE_URL=postgresql://usuario:senha@host:5432/banco?sslmode=require
 * e converte para spring.datasource.url/username/password (formato JDBC).
 *
 * Se DATABASE_URL não existir, nada muda (vale SPRING_DATASOURCE_* do docker-compose).
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        environment.getPropertySources().addFirst(new MapPropertySource("databaseUrl", converter(databaseUrl.trim())));
    }

    static Map<String, Object> converter(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String esquema = uri.getScheme();
        if (!"postgres".equals(esquema) && !"postgresql".equals(esquema)) {
            throw new IllegalStateException("DATABASE_URL deve começar com postgresql://");
        }

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbc.append(':').append(uri.getPort());
        }
        jdbc.append(uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbc.append('?').append(uri.getRawQuery());
        }

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", jdbc.toString());

        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            int separador = userInfo.indexOf(':');
            String usuario = separador >= 0 ? userInfo.substring(0, separador) : userInfo;
            props.put("spring.datasource.username", decodificar(usuario));
            if (separador >= 0) {
                props.put("spring.datasource.password", decodificar(userInfo.substring(separador + 1)));
            }
        }
        return props;
    }

    private static String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }
}
