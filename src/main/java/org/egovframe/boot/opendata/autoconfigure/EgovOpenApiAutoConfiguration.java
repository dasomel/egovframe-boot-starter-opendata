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

/**
 * 공공데이터포털 오픈API 클라이언트를 위한 Spring Boot 자동구성 클래스.
 *
 * <p>{@link EgovOpenApiProperties}에 바인딩된 설정값(서비스키, base URL, 타임아웃, 최대 재시도 횟수)을
 * 바탕으로 {@link WebClient}와 {@link EgovOpenApiClient} 빈을 구성한다. 애플리케이션 코드는
 * {@code EgovOpenApiClient}를 주입받아 사용하기만 하면 되고, WebClient 구성이나 재시도 정책 조립은
 * 이 클래스가 대신 처리한다.
 *
 * <p>연결 타임아웃은 Reactor Netty {@link HttpClient} 옵션으로, 응답 타임아웃은
 * {@code responseTimeout}으로 각각 반영된다.
 */
@AutoConfiguration
@EnableConfigurationProperties(EgovOpenApiProperties.class)
public class EgovOpenApiAutoConfiguration {

    /**
     * 설정값을 기반으로 {@link EgovOpenApiClient} 빈을 생성한다.
     *
     * <p>정규화 로직({@link EgovOpenApiItemsNormalizer})과 재시도 판단 로직({@link EgovOpenApiRetryPolicy})은
     * Spring 컨텍스트와 무관한 순수 객체이므로 여기서 직접 생성해 클라이언트에 주입한다.
     *
     * @param props {@code egovframe.opendata} 프리픽스로 바인딩된 설정값
     * @return 요청 전송부터 결과코드 판정, 정규화, 재시도까지 처리하는 클라이언트
     */
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
