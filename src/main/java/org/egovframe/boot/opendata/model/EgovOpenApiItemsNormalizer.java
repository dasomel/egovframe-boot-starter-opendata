package org.egovframe.boot.opendata.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * 공공데이터포털 응답의 items 필드를 항상 List로 정규화한다.
 * 결과가 1건일 때 item이 배열이 아닌 단일 객체로 오는 경우를 흡수한다.
 */
public class EgovOpenApiItemsNormalizer {

    public <T> List<T> normalize(JsonNode bodyNode, Class<T> itemType, ObjectMapper mapper) {
        if (bodyNode == null || bodyNode.isMissingNode() || bodyNode.isNull()) {
            return List.of();
        }
        JsonNode itemsNode = bodyNode.get("items");
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode() || !itemsNode.isContainerNode()) {
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
            return List.of(mapper.convertValue(itemNode, itemType));
        }
        return List.of();
    }
}
