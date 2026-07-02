package org.egovframe.boot.opendata.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * 공공데이터포털 응답의 items 필드를 항상 {@link List}로 정규화한다.
 *
 * <p>공공데이터포털 오픈API는 {@code response.body.items.item}에 실제 데이터를 담아 내려주는데,
 * 조회 결과가 2건 이상이면 {@code item}이 JSON 배열로, 결과가 정확히 1건이면 배열이 아니라
 * 단일 객체로 내려오는 고질적인 버그가 있다. 클라이언트 입장에서는 결과 건수와 무관하게 항상
 * {@code List<T>}로 다루고 싶으므로, 이 클래스가 그 차이를 흡수해 호출부(서비스 로직, 컨트롤러)가
 * 배열/단일객체 분기를 매번 신경 쓰지 않도록 한다.
 *
 * <p>Jackson {@link JsonNode}와 {@link ObjectMapper}만 사용하는 순수 로직이라 Spring 컨텍스트
 * 없이도 다양한 응답 형태를 단위 테스트할 수 있다.
 */
public class EgovOpenApiItemsNormalizer {

    /**
     * 응답 body의 items를 정규화된 리스트로 변환한다.
     *
     * <p>아래 순서로 케이스를 흡수한다.
     * <ol>
     *   <li><b>body 자체가 없음</b> — body가 null이거나 누락 노드이면 빈 리스트. 재시도 소진 등으로
     *       body를 아예 파싱할 수 없는 방어적 상황을 처리한다.</li>
     *   <li><b>items 필드가 컨테이너(배열/객체)가 아님</b> — {@code NODATA_ERROR}처럼 검색 결과가
     *       없을 때 일부 서비스는 {@code "items":""}처럼 items를 빈 문자열로 내려준다. 이 경우
     *       역직렬화를 시도하면 오류가 나므로, 컨테이너 노드가 아니면 곧바로 빈 리스트로 처리한다
     *       (문자열 케이스).</li>
     *   <li><b>items가 배열인 경우</b> — {@code item} 래퍼 없이 곧바로 배열을 내려주는 서비스도
     *       있어, items 자체가 배열이면 그대로 item 노드로 사용한다.</li>
     *   <li><b>items.item이 있는 경우</b> — 표준적인 형태로, item 노드를 꺼내 아래 배열/객체
     *       분기로 넘긴다.</li>
     *   <li><b>item이 배열</b> — 결과 2건 이상. 각 원소를 {@code itemType}으로 변환해 리스트에 담는다.</li>
     *   <li><b>item이 단일 객체</b> — 결과 정확히 1건일 때 발생하는 공공데이터포털의 고질적 버그.
     *       배열이 아니지만 값은 존재하므로, 단일 원소를 가진 리스트로 감싸 반환한다.</li>
     *   <li><b>그 외(item이 null 등)</b> — 방어적으로 빈 리스트를 반환한다.</li>
     * </ol>
     *
     * @param bodyNode 응답의 {@code response.body} 노드 (null 허용)
     * @param itemType 개별 item을 역직렬화할 대상 타입
     * @param mapper   JsonNode를 itemType으로 변환할 때 사용할 ObjectMapper
     * @param <T>      item 타입
     * @return 정규화된 항목 리스트. 데이터가 없으면 빈 리스트(불변)
     */
    public <T> List<T> normalize(JsonNode bodyNode, Class<T> itemType, ObjectMapper mapper) {
        if (bodyNode == null || bodyNode.isMissingNode() || bodyNode.isNull()) {
            return List.of();
        }
        JsonNode itemsNode = bodyNode.get("items");
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode() || !itemsNode.isContainerNode()) {
            // items가 빈 문자열("")로 오는 NODATA_ERROR 케이스 등을 여기서 걸러낸다.
            return List.of();
        }
        JsonNode itemNode = itemsNode.isArray() ? itemsNode
                : itemsNode.has("item") ? itemsNode.get("item") : itemsNode;
        if (itemNode == null || itemNode.isNull() || itemNode.isMissingNode()) {
            return List.of();
        }
        if (itemNode.isArray()) {
            List<T> result = new ArrayList<>();
            for (JsonNode n : itemNode) {
                result.add(mapper.convertValue(n, itemType));
            }
            return result;
        }
        if (itemNode.isObject()) {
            // 결과 1건일 때 item이 배열이 아니라 단일 객체로 오는 공공데이터포털 버그를 흡수한다.
            return List.of(mapper.convertValue(itemNode, itemType));
        }
        return List.of();
    }
}
