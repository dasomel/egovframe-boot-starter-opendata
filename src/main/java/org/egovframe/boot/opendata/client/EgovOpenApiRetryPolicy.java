package org.egovframe.boot.opendata.client;

import org.egovframe.boot.opendata.model.EgovOpenApiResultCode;

/** 결과코드·시도횟수 기반 재시도 여부를 판단하는 순수 로직. */
public class EgovOpenApiRetryPolicy {

    private final int maxRetry;

    public EgovOpenApiRetryPolicy(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public boolean shouldRetry(EgovOpenApiResultCode code, int attemptNumber) {
        return code.isRetryable() && attemptNumber < maxRetry;
    }
}
