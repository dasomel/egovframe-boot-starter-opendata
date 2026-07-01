package org.egovframe.boot.opendata.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.egovframe.boot.opendata.model.EgovOpenApiException;
import org.egovframe.boot.opendata.model.EgovOpenApiItemsNormalizer;
import org.egovframe.boot.opendata.model.EgovOpenApiResponse;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EgovOpenApiClientWireMockTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance().build();

    record Item(String name) {}

    private EgovOpenApiClient client() {
        WebClient webClient = WebClient.builder().baseUrl(wm.baseUrl()).build();
        EgovOpenApiProperties props = new EgovOpenApiProperties();
        props.setServiceKey("test-key");
        props.setMaxRetry(3);
        return new EgovOpenApiClient(webClient, props, new EgovOpenApiItemsNormalizer(),
                new EgovOpenApiRetryPolicy(3), new ObjectMapper());
    }

    @Test void parsesNormalResponseWithArrayItems() {
        wm.stubFor(get(urlPathEqualTo("/sample")).willReturn(okJson(
                "{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
                        + "\"body\":{\"items\":{\"item\":[{\"name\":\"a\"},{\"name\":\"b\"}]},"
                        + "\"numOfRows\":10,\"pageNo\":1,\"totalCount\":2}}}")));

        EgovOpenApiResponse<Item> result = client().fetch("/sample", java.util.Map.of(), Item.class).block();

        assertThat(result.getItems()).extracting(Item::name).containsExactly("a", "b");
        assertThat(result.getTotalCount()).isEqualTo(2);
    }

    @Test void normalizesSingleObjectItemBug() {
        wm.stubFor(get(urlPathEqualTo("/sample")).willReturn(okJson(
                "{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
                        + "\"body\":{\"items\":{\"item\":{\"name\":\"solo\"}},"
                        + "\"numOfRows\":10,\"pageNo\":1,\"totalCount\":1}}}")));

        EgovOpenApiResponse<Item> result = client().fetch("/sample", java.util.Map.of(), Item.class).block();

        assertThat(result.getItems()).extracting(Item::name).containsExactly("solo");
    }

    @Test void retriesOnHttpErrorThenSucceeds() {
        wm.stubFor(get(urlPathEqualTo("/sample")).inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(okJson("{\"response\":{\"header\":{\"resultCode\":\"04\",\"resultMsg\":\"HTTP_ERROR\"},\"body\":{}}}"))
                .willSetStateTo("retried"));
        wm.stubFor(get(urlPathEqualTo("/sample")).inScenario("retry")
                .whenScenarioStateIs("retried")
                .willReturn(okJson("{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
                        + "\"body\":{\"items\":{\"item\":[{\"name\":\"ok\"}]},\"numOfRows\":10,\"pageNo\":1,\"totalCount\":1}}}")));

        EgovOpenApiResponse<Item> result = client().fetch("/sample", java.util.Map.of(), Item.class).block();

        assertThat(result.getItems()).extracting(Item::name).containsExactly("ok");
    }

    @Test void throwsAfterExhaustingRetries() {
        wm.stubFor(get(urlPathEqualTo("/sample")).willReturn(okJson(
                "{\"response\":{\"header\":{\"resultCode\":\"05\",\"resultMsg\":\"SERVICETIMEOUT_ERROR\"},\"body\":{}}}")));

        assertThatThrownBy(() -> client().fetch("/sample", java.util.Map.of(), Item.class).block())
                .isInstanceOf(EgovOpenApiException.class);
    }

    @Test void throwsImmediatelyOnNonRetryableError() {
        wm.stubFor(get(urlPathEqualTo("/sample")).willReturn(okJson(
                "{\"response\":{\"header\":{\"resultCode\":\"30\",\"resultMsg\":\"SERVICE_KEY_IS_NOT_REGISTERED_ERROR\"},\"body\":{}}}")));

        assertThatThrownBy(() -> client().fetch("/sample", java.util.Map.of(), Item.class).block())
                .isInstanceOf(EgovOpenApiException.class);
    }
}
