package org.egovframe.boot.opendata.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class EgovOpenApiItemsNormalizerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final EgovOpenApiItemsNormalizer normalizer = new EgovOpenApiItemsNormalizer();

    record Item(String name) {}

    private JsonNode body(String json) throws Exception { return mapper.readTree(json); }

    @Test void normalizesArrayOfItems() throws Exception {
        JsonNode body = body("{\"items\":{\"item\":[{\"name\":\"a\"},{\"name\":\"b\"}]}}");
        List<Item> result = normalizer.normalize(body, Item.class, mapper);
        assertThat(result).extracting(Item::name).containsExactly("a", "b");
    }

    @Test void normalizesSingleObjectItemAsOneElementList() throws Exception {
        // data.go.kr 고질적 버그: 결과가 1건일 때 item이 배열이 아닌 객체로 온다.
        JsonNode body = body("{\"items\":{\"item\":{\"name\":\"solo\"}}}");
        List<Item> result = normalizer.normalize(body, Item.class, mapper);
        assertThat(result).extracting(Item::name).containsExactly("solo");
    }

    @Test void normalizesDirectArrayItemsWithoutItemWrapper() throws Exception {
        JsonNode body = body("{\"items\":[{\"name\":\"x\"}]}");
        List<Item> result = normalizer.normalize(body, Item.class, mapper);
        assertThat(result).extracting(Item::name).containsExactly("x");
    }

    @Test void missingItemsReturnsEmptyList() throws Exception {
        JsonNode body = body("{}");
        assertThat(normalizer.normalize(body, Item.class, mapper)).isEmpty();
    }

    @Test void nullBodyReturnsEmptyList() {
        assertThat(normalizer.normalize(null, Item.class, mapper)).isEmpty();
    }

    @Test void nodataErrorEmptyItemsStringReturnsEmptyList() throws Exception {
        // NODATA_ERROR 상황에서 items가 빈 문자열로 오는 경우도 있다.
        JsonNode body = body("{\"items\":\"\"}");
        assertThat(normalizer.normalize(body, Item.class, mapper)).isEmpty();
    }
}
