package org.egovframe.boot.opendata.model;

/**
 * 공공데이터포털 API가 반환한 비정상 결과코드를 감싸는 예외.
 *
 * <p>{@code resultCode}가 {@link EgovOpenApiResultCode#isSuccess()}가 아닌 모든 응답에서
 * 이 예외가 발생한다. {@code org.egovframe.boot.opendata.client.EgovOpenApiClient}는 이
 * 예외의 결과코드가 재시도 가능({@link EgovOpenApiResultCode#isRetryable()})한지 판단해
 * 재시도 여부를 결정하고,
 * 재시도가 모두 소진되면 이 예외를 원본 그대로 호출부에 전달한다.
 */
public class EgovOpenApiException extends RuntimeException {

    private final EgovOpenApiResultCode resultCode;

    /**
     * @param resultCode 응답 헤더에서 판정된 실패 결과코드
     * @param message    응답 헤더의 resultMsg (공공데이터포털이 제공한 원본 오류 메시지)
     */
    public EgovOpenApiException(EgovOpenApiResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * @return 이 예외를 유발한 결과코드
     */
    public EgovOpenApiResultCode getResultCode() { return resultCode; }
}
