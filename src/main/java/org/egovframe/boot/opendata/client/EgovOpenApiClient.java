package org.egovframe.boot.opendata.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egovframe.boot.opendata.model.EgovOpenApiException;
import org.egovframe.boot.opendata.model.EgovOpenApiItemsNormalizer;
import org.egovframe.boot.opendata.model.EgovOpenApiResponse;
import org.egovframe.boot.opendata.model.EgovOpenApiResultCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/** 공공데이터포털 스타일 Open API를 표준화된 방식으로 호출하는 제네릭 클라이언트. */
public class EgovOpenApiClient {

    private final WebClient webClient;
    private final EgovOpenApiProperties props;
    private final EgovOpenApiItemsNormalizer normalizer;
    private final EgovOpenApiRetryPolicy retryPolicy;
    private final ObjectMapper mapper;

    public EgovOpenApiClient(WebClient webClient, EgovOpenApiProperties props,
                              EgovOpenApiItemsNormalizer normalizer, EgovOpenApiRetryPolicy retryPolicy,
                              ObjectMapper mapper) {
        this.webClient = webClient;
        this.props = props;
        this.normalizer = normalizer;
        this.retryPolicy = retryPolicy;
        this.mapper = mapper;
    }

    public <T> Mono<EgovOpenApiResponse<T>> fetch(String path, Map<String, Object> params, Class<T> itemType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path).queryParam("serviceKey", props.getServiceKey());
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(root -> parse(root, itemType))
                .retryWhen(Retry.backoff(props.getMaxRetry(), Duration.ofMillis(200))
                        .filter(EgovOpenApiClient::isRetryableFailure)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private <T> Mono<EgovOpenApiResponse<T>> parse(JsonNode root, Class<T> itemType) {
        JsonNode responseNode = root.path("response");
        JsonNode headerNode = responseNode.path("header");
        JsonNode bodyNode = responseNode.path("body");

        EgovOpenApiResultCode code = EgovOpenApiResultCode.from(headerNode.path("resultCode").asText(null));
        String resultMsg = headerNode.path("resultMsg").asText(null);

        if (!code.isSuccess()) {
            return Mono.error(new EgovOpenApiException(code, resultMsg));
        }

        var items = normalizer.normalize(bodyNode, itemType, mapper);
        int numOfRows = bodyNode.path("numOfRows").asInt(0);
        int pageNo = bodyNode.path("pageNo").asInt(0);
        int totalCount = bodyNode.path("totalCount").asInt(items.size());

        return Mono.just(EgovOpenApiResponse.of(code, resultMsg, items, pageNo, numOfRows, totalCount));
    }

    private static boolean isRetryableFailure(Throwable t) {
        return t instanceof EgovOpenApiException e && e.getResultCode().isRetryable();
    }
}
