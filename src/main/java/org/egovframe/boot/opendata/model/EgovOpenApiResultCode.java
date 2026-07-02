package org.egovframe.boot.opendata.model;

import java.util.Arrays;

/**
 * 공공데이터포털(data.go.kr) 공통 오픈API 결과코드 표준 레지스트리.
 *
 * <p>공공데이터포털 산하 오픈API는 서비스가 달라도 대부분 동일한 결과코드 체계
 * ({@code response.header.resultCode})를 공유한다. 코드는 크게 세 그룹으로 나뉜다.
 * <ul>
 *   <li><b>00 성공</b> — 정상 처리.</li>
 *   <li><b>일시적 오류(재시도 가능)</b> — 서버·네트워크 상태에 따라 재호출하면 성공할 수 있는 오류.
 *       {@code HTTP_ERROR}(04), {@code SERVICETIMEOUT_ERROR}(05),
 *       {@code LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR}(22)가 해당한다.
 *       22는 초당/일별 호출량 제한 초과인데, 시간이 지나면 카운터가 리셋되므로 재시도 가능으로 분류한다.</li>
 *   <li><b>영구적 오류(재시도 불가)</b> — 요청 자체나 서비스키·권한 설정이 잘못된 경우로,
 *       같은 요청을 다시 보내도 동일하게 실패한다(파라미터 오류, 서비스키 미등록, IP 미등록 등).
 *       재시도는 불필요한 호출량만 소모하므로 즉시 예외로 전파한다.</li>
 * </ul>
 *
 * <p>{@code isRetryable()}이 이 분류를 코드화하며, {@code org.egovframe.boot.opendata.client.EgovOpenApiClient}의
 * {@code retryWhen} 필터와 {@code org.egovframe.boot.opendata.client.EgovOpenApiRetryPolicy}가 이 값을 근거로
 * 재시도 여부를 결정한다.
 */
public enum EgovOpenApiResultCode {

    /** 00 - 정상 처리. */
    NORMAL_SERVICE("00"),

    /** 01 - 애플리케이션 내부 오류. 요청 자체의 문제가 아니라 서비스 측 로직 오류이므로 재시도 불가. */
    APPLICATION_ERROR("01"),

    /** 02 - 서비스 데이터베이스 오류. 재시도해도 동일 오류가 재현될 가능성이 높아 재시도 불가로 분류. */
    DB_ERROR("02"),

    /** 03 - 조회 결과가 없음. 오류라기보다 빈 결과 상태에 가까워 재시도 불가. */
    NODATA_ERROR("03"),

    /** 04 - HTTP 오류. 네트워크·서버 상태에 따른 일시적 오류로 보고 재시도 가능. */
    HTTP_ERROR("04"),

    /** 05 - 서비스 처리 시간 초과. 서버 부하 등 일시적 원인일 수 있어 재시도 가능. */
    SERVICETIMEOUT_ERROR("05"),

    /** 10 - 요청 파라미터 값이 유효하지 않음. 호출부 수정이 필요하므로 재시도 불가. */
    INVALID_REQUEST_PARAMETER_ERROR("10"),

    /** 11 - 필수 요청 파라미터 누락. 호출부 수정이 필요하므로 재시도 불가. */
    NO_MANDATORY_REQUEST_PARAMETERS_ERROR("11"),

    /** 12 - 해당 오픈API 서비스가 존재하지 않음(엔드포인트 오류). 재시도 불가. */
    NO_OPENAPI_SERVICE_ERROR("12"),

    /** 20 - 서비스 접근이 거부됨(권한 없음). 설정 수정이 필요하므로 재시도 불가. */
    SERVICE_ACCESS_DENIED_ERROR("20"),

    /** 22 - 서비스 요청 제한 횟수 초과(초당/일별 호출량 제한). 시간 경과 후 카운터가 리셋되므로 재시도 가능. */
    LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR("22"),

    /** 30 - 등록되지 않은 서비스키. 서비스키 재발급·설정이 필요하므로 재시도 불가. */
    SERVICE_KEY_IS_NOT_REGISTERED_ERROR("30"),

    /** 31 - 활용기간이 만료된 서비스키. 활용신청 갱신이 필요하므로 재시도 불가. */
    DEADLINE_HAS_EXPIRED_ERROR("31"),

    /** 32 - 등록되지 않은 IP에서의 요청. IP 등록이 필요하므로 재시도 불가. */
    UNREGISTERED_IP_ERROR("32"),

    /** 33 - 서명되지 않은 호출(서명 검증 실패). 요청 구성 수정이 필요하므로 재시도 불가. */
    UNSIGNED_CALL_ERROR("33"),

    /** 99 - 위 목록에 없는 코드이거나 resultCode 자체가 누락된 경우의 대체값. 재시도 불가. */
    UNKNOWN("99");

    private final String code;

    EgovOpenApiResultCode(String code) { this.code = code; }

    /**
     * @return 공공데이터포털 응답 헤더에 실리는 원본 코드 문자열(예: "00")
     */
    public String getCode() { return code; }

    /**
     * 응답 헤더의 resultCode 문자열을 대응하는 상수로 변환한다.
     * 알려지지 않은 코드이거나 null이면 {@link #UNKNOWN}으로 대체한다.
     *
     * @param code 응답 헤더의 resultCode 값 (null 허용)
     * @return 대응하는 결과코드 상수, 매칭되지 않으면 {@link #UNKNOWN}
     */
    public static EgovOpenApiResultCode from(String code) {
        if (code == null) return UNKNOWN;
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * @return 정상 처리(00) 여부
     */
    public boolean isSuccess() { return this == NORMAL_SERVICE; }

    /**
     * 이 결과코드가 재시도 대상인지 여부를 반환한다.
     * HTTP 오류, 서비스 처리시간 초과, 호출량 제한 초과처럼 시간이 지나거나 재호출 시 성공할
     * 가능성이 있는 일시적 오류만 재시도 대상으로 분류한다. 그 외 코드는 같은 요청을 다시
     * 보내도 결과가 바뀌지 않으므로 재시도하지 않는다.
     *
     * @return 재시도 가능한 결과코드이면 true
     */
    public boolean isRetryable() {
        return this == HTTP_ERROR || this == SERVICETIMEOUT_ERROR
                || this == LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR;
    }
}
