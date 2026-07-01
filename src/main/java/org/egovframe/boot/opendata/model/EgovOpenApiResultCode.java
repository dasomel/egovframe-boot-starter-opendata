package org.egovframe.boot.opendata.model;

import java.util.Arrays;

/** 공공데이터포털(data.go.kr) 공통 오픈API 결과코드 표준 레지스트리. */
public enum EgovOpenApiResultCode {
    NORMAL_SERVICE("00"),
    APPLICATION_ERROR("01"),
    DB_ERROR("02"),
    NODATA_ERROR("03"),
    HTTP_ERROR("04"),
    SERVICETIMEOUT_ERROR("05"),
    INVALID_REQUEST_PARAMETER_ERROR("10"),
    NO_MANDATORY_REQUEST_PARAMETERS_ERROR("11"),
    NO_OPENAPI_SERVICE_ERROR("12"),
    SERVICE_ACCESS_DENIED_ERROR("20"),
    LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR("22"),
    SERVICE_KEY_IS_NOT_REGISTERED_ERROR("30"),
    DEADLINE_HAS_EXPIRED_ERROR("31"),
    UNREGISTERED_IP_ERROR("32"),
    UNSIGNED_CALL_ERROR("33"),
    UNKNOWN("99");

    private final String code;

    EgovOpenApiResultCode(String code) { this.code = code; }

    public String getCode() { return code; }

    public static EgovOpenApiResultCode from(String code) {
        if (code == null) return UNKNOWN;
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isSuccess() { return this == NORMAL_SERVICE; }

    public boolean isRetryable() {
        return this == HTTP_ERROR || this == SERVICETIMEOUT_ERROR
                || this == LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR;
    }
}
