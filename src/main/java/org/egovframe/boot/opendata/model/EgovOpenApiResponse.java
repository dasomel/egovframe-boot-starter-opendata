package org.egovframe.boot.opendata.model;

import java.util.List;

/** 공공데이터포털 표준 응답을 표준화한 모델. */
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
