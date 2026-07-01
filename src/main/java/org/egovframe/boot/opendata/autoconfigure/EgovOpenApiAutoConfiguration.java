package org.egovframe.boot.opendata.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egovframe.boot.opendata.client.EgovOpenApiClient;
import org.egovframe.boot.opendata.client.EgovOpenApiProperties;
import org.egovframe.boot.opendata.client.EgovOpenApiRetryPolicy;
import org.egovframe.boot.opendata.model.EgovOpenApiItemsNormalizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(EgovOpenApiProperties.class)
public class EgovOpenApiAutoConfiguration {

    @Bean
    public EgovOpenApiClient egovOpenApiClient(EgovOpenApiProperties props) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
        WebClient webClient = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        return new EgovOpenApiClient(webClient, props, new EgovOpenApiItemsNormalizer(),
                new EgovOpenApiRetryPolicy(props.getMaxRetry()), new ObjectMapper());
    }
}
