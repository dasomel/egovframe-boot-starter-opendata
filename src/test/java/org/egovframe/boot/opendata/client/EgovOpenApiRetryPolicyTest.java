package org.egovframe.boot.opendata.client;

import org.egovframe.boot.opendata.model.EgovOpenApiResultCode;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EgovOpenApiRetryPolicyTest {
    private final EgovOpenApiRetryPolicy policy = new EgovOpenApiRetryPolicy(3);

    @Test void retriesHttpErrorWithinMaxAttempts() {
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.HTTP_ERROR, 1)).isTrue();
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.HTTP_ERROR, 2)).isTrue();
    }
    @Test void stopsRetryingAtMaxAttempts() {
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.HTTP_ERROR, 3)).isFalse();
    }
    @Test void doesNotRetryNonRetryableCode() {
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.APPLICATION_ERROR, 1)).isFalse();
    }
    @Test void retriesRateLimitCode() {
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR, 1)).isTrue();
    }
    @Test void doesNotRetrySuccessCode() {
        assertThat(policy.shouldRetry(EgovOpenApiResultCode.NORMAL_SERVICE, 1)).isFalse();
    }
}
