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

/**
 * 공공데이터포털 스타일 Open API를 표준화된 방식으로 호출하는 제네릭 클라이언트.
 *
 * <p>호출 대상 서비스마다 응답 항목(item) 스키마는 다르지만 {@code response.header.resultCode} /
 * {@code response.header.resultMsg} / {@code response.body.items} 구조와 결과코드 체계는 공통이다.
 * 이 클래스는 그 공통 구조를 한 번만 처리하고, 서비스별 항목 타입은 제네릭 파라미터로 받는다.
 *
 * <p>처리 흐름은 다음과 같다.
 * <ol>
 *   <li>{@link #fetch}로 요청 URI를 구성해 WebClient로 호출한다.</li>
 *   <li>응답을 {@link JsonNode}로 받아 {@link #parse}에서 header/body를 분리한다.</li>
 *   <li>{@link EgovOpenApiResultCode}로 성공/실패를 판정한다.</li>
 *   <li>실패이면서 재시도 가능한 코드이면 {@code retryWhen}이 재시도한다.</li>
 *   <li>성공이면 {@link EgovOpenApiItemsNormalizer}로 items를 정규화해 {@link EgovOpenApiResponse}를 만든다.</li>
 * </ol>
 */
public class EgovOpenApiClient {

    private final WebClient webClient;
    private final EgovOpenApiProperties props;
    private final EgovOpenApiItemsNormalizer normalizer;
    private final EgovOpenApiRetryPolicy retryPolicy;
    private final ObjectMapper mapper;

    /**
     * 클라이언트를 구성한다. 정상적으로는 {@code EgovOpenApiAutoConfiguration}이 협력 객체를
     * 조립해 이 생성자를 호출하며, 애플리케이션 코드가 직접 호출할 일은 거의 없다.
     *
     * @param webClient  실제 HTTP 호출에 사용할 WebClient (baseUrl·타임아웃이 이미 설정되어 있어야 함)
     * @param props      서비스키·최대 재시도 횟수 등 호출 설정값
     * @param normalizer 응답 body의 items를 List로 정규화하는 로직
     * @param retryPolicy 결과코드·시도횟수 기반 재시도 판단 로직 (현재는 참고용으로 보관, 실제 재시도 필터는 resultCode로 직접 판단)
     * @param mapper     JsonNode를 항목 타입으로 변환할 때 사용할 ObjectMapper
     */
    public EgovOpenApiClient(WebClient webClient, EgovOpenApiProperties props,
                              EgovOpenApiItemsNormalizer normalizer, EgovOpenApiRetryPolicy retryPolicy,
                              ObjectMapper mapper) {
        this.webClient = webClient;
        this.props = props;
        this.normalizer = normalizer;
        this.retryPolicy = retryPolicy;
        this.mapper = mapper;
    }

    /**
     * 오픈API를 호출하고 표준화된 응답으로 변환한다.
     *
     * <p>요청 URI는 {@code path}에 {@code serviceKey} 쿼리파라미터를 먼저 붙이고, 이어서
     * {@code params}에 담긴 나머지 파라미터(페이지 번호, 조회건수 등 서비스별 파라미터)를 그대로
     * 쿼리스트링에 추가하는 방식으로 구성한다. serviceKey는 공공데이터포털에서 발급 시 이미
     * URL 인코딩된 값으로 내려주는 경우가 많아, 이중 인코딩 여부는 {@code EgovOpenApiProperties}의
     * {@code serviceKeyEncoded} 설정으로 서비스별로 조정한다.
     *
     * <p>재시도는 {@code retryWhen} + {@code onRetryExhaustedThrow}로 처리한다. 재시도가 모두
     * 소진되면 Reactor의 기본 {@code RetryExhaustedException}으로 감싸는 대신 마지막 실패의 원본
     * 예외({@link EgovOpenApiException})를 그대로 던지도록 했는데, 호출부가 결과코드별로
     * 분기 처리할 수 있게 하기 위함이다.
     *
     * @param path     서비스 엔드포인트 경로 (baseUrl 기준 상대 경로)
     * @param params   serviceKey를 제외한 나머지 쿼리 파라미터
     * @param itemType 응답 items 배열의 각 원소를 역직렬화할 타입
     * @param <T>      응답 항목 타입
     * @return 결과코드·페이징 정보·정규화된 items를 담은 응답. 재시도 불가능한 오류이거나 재시도가
     *         모두 소진되면 {@link EgovOpenApiException}으로 종료되는 {@link Mono}
     */
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

    /**
     * 원시 응답 JSON을 header/body로 분리해 결과코드를 판정하고, 성공 시 items를 정규화한다.
     *
     * @param root     응답 전체 JSON 트리
     * @param itemType 항목 역직렬화 타입
     * @param <T>      응답 항목 타입
     * @return 성공이면 정규화된 응답을 담은 {@link Mono}, 실패면 {@link EgovOpenApiException}으로
     *         종료되는 {@link Mono}
     */
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

    /**
     * Reactor {@code retryWhen}의 필터 조건. {@link EgovOpenApiException}이면서 결과코드가
     * 재시도 가능으로 분류된 경우에만 재시도 대상으로 인정한다(예: 일시적인 HTTP/타임아웃 오류).
     * 요청 자체가 잘못된 오류(파라미터 누락, 서비스키 미등록 등)는 재시도해도 결과가 같으므로 제외한다.
     *
     * @param t 발생한 예외
     * @return 재시도 가능한 실패이면 true
     */
    private static boolean isRetryableFailure(Throwable t) {
        return t instanceof EgovOpenApiException e && e.getResultCode().isRetryable();
    }
}
