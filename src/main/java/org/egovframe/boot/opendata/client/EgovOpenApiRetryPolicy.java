package org.egovframe.boot.opendata.client;

import org.egovframe.boot.opendata.model.EgovOpenApiResultCode;

/**
 * 결과코드·시도횟수 기반 재시도 여부를 판단하는 순수 로직.
 *
 * <p>판단 기준은 두 가지다.
 * <ol>
 *   <li>결과코드 자체가 재시도 가능 코드({@link EgovOpenApiResultCode#isRetryable()})여야 한다.
 *       일시적인 오류(HTTP_ERROR, SERVICETIMEOUT_ERROR, 호출량 초과)만 해당하며, 요청 파라미터
 *       오류나 서비스키 미등록처럼 다시 호출해도 같은 결과가 나오는 오류는 대상에서 제외한다.</li>
 *   <li>현재까지의 시도 횟수가 설정된 최대 재시도 횟수 미만이어야 한다.</li>
 * </ol>
 *
 * <p>Spring이나 WebClient에 의존하지 않는 순수 객체라 결과코드·횟수 조합만으로 단위 테스트할 수 있다.
 */
public class EgovOpenApiRetryPolicy {

    private final int maxRetry;

    /**
     * @param maxRetry 재시도 가능한 오류에 대한 최대 시도 횟수
     */
    public EgovOpenApiRetryPolicy(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    /**
     * 주어진 결과코드와 시도 횟수를 기준으로 재시도 여부를 판단한다.
     *
     * @param code          응답 헤더에서 판정된 결과코드
     * @param attemptNumber 현재까지의 시도 횟수(0부터 시작)
     * @return 재시도 가능한 결과코드이고 아직 최대 시도 횟수에 도달하지 않았으면 true
     */
    public boolean shouldRetry(EgovOpenApiResultCode code, int attemptNumber) {
        return code.isRetryable() && attemptNumber < maxRetry;
    }
}
