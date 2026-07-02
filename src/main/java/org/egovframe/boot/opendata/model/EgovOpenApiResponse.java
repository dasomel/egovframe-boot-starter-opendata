package org.egovframe.boot.opendata.model;

import java.util.List;

/**
 * 공공데이터포털 표준 응답을 표준화한 모델.
 *
 * <p>서비스마다 항목(item) 스키마는 다르지만 결과코드·페이징 정보의 위치는 공통이므로,
 * 그 공통 부분을 이 클래스로 표준화하고 항목 타입만 제네릭으로 열어둔다.
 * {@code org.egovframe.boot.opendata.client.EgovOpenApiClient#fetch}가 성공 응답을 파싱한 뒤
 * {@link #of}로 생성하며, 인스턴스는 생성 후 변경되지 않는다.
 *
 * @param <T> 개별 응답 항목 타입
 */
public class EgovOpenApiResponse<T> {

    private final EgovOpenApiResultCode resultCode;
    private final String resultMsg;
    private final List<T> items;
    private final int pageNo;
    private final int numOfRows;
    private final int totalCount;

    private EgovOpenApiResponse(EgovOpenApiResultCode resultCode, String resultMsg, List<T> items,
                                 int pageNo, int numOfRows, int totalCount) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.items = items;
        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.totalCount = totalCount;
    }

    /**
     * 응답 인스턴스를 생성한다.
     *
     * @param resultCode 응답 헤더에서 판정된 결과코드 (성공인 경우에만 이 메서드가 호출됨)
     * @param resultMsg  응답 헤더의 resultMsg
     * @param items      정규화된 항목 리스트
     * @param pageNo     현재 페이지 번호
     * @param numOfRows  페이지당 항목 수
     * @param totalCount 전체 결과 건수
     * @param <T>        개별 응답 항목 타입
     * @return 표준화된 응답 인스턴스
     */
    public static <T> EgovOpenApiResponse<T> of(EgovOpenApiResultCode resultCode, String resultMsg,
                                                 List<T> items, int pageNo, int numOfRows, int totalCount) {
        return new EgovOpenApiResponse<>(resultCode, resultMsg, items, pageNo, numOfRows, totalCount);
    }

    public EgovOpenApiResultCode getResultCode() { return resultCode; }
    public String getResultMsg() { return resultMsg; }
    public List<T> getItems() { return items; }
    public int getPageNo() { return pageNo; }
    public int getNumOfRows() { return numOfRows; }
    public int getTotalCount() { return totalCount; }
}
