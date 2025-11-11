package com.hanguyen.search_service.config;

import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Value("${spring.elasticsearch.api-key}")
    private String apiKey;
    @Bean
    public RestClientBuilderCustomizer restClientBuilderCustomizer() {

        final String authorizationHeaderValue = "ApiKey " + apiKey;

        return builder -> {
            builder.setDefaultHeaders(new BasicHeader[]{
                    new BasicHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue)
            });
        };
    }
}