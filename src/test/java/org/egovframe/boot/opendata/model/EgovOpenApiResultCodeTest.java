package org.egovframe.boot.opendata.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EgovOpenApiResultCodeTest {
    @Test void normalIsSuccess() {
        assertThat(EgovOpenApiResultCode.from("00")).isEqualTo(EgovOpenApiResultCode.NORMAL_SERVICE);
        assertThat(EgovOpenApiResultCode.NORMAL_SERVICE.isSuccess()).isTrue();
    }
    @Test void unknownCodeMapsToUnknown() {
        assertThat(EgovOpenApiResultCode.from("77")).isEqualTo(EgovOpenApiResultCode.UNKNOWN);
    }
    @Test void httpErrorIsRetryable() {
        assertThat(EgovOpenApiResultCode.from("04").isRetryable()).isTrue();
    }
    @Test void rateLimitIsRetryable() {
        assertThat(EgovOpenApiResultCode.from("22").isRetryable()).isTrue();
    }
    @Test void applicationErrorIsNotRetryable() {
        assertThat(EgovOpenApiResultCode.from("01").isRetryable()).isFalse();
    }
    @Test void nullCodeMapsToUnknown() {
        assertThat(EgovOpenApiResultCode.from(null)).isEqualTo(EgovOpenApiResultCode.UNKNOWN);
    }
}
