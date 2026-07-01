package org.egovframe.boot.opendata.model;

/** 공공데이터포털 API가 반환한 비정상 결과코드를 감싸는 예외. */
public class EgovOpenApiException extends RuntimeException {

    private final EgovOpenApiResultCode resultCode;

    public EgovOpenApiException(EgovOpenApiResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public EgovOpenApiResultCode getResultCode() { return resultCode; }
}
