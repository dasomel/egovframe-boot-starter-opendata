package org.egovframe.boot.opendata.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code egovframe.opendata} 프리픽스로 바인딩되는 오픈API 호출 설정값.
 *
 * <p>application.yml/properties에서 서비스키, 호출 대상 base URL, 타임아웃, 최대 재시도 횟수를
 * 지정한다. {@code EgovOpenApiAutoConfiguration}이 이 값을 읽어 WebClient와 재시도 정책을 구성한다.
 */
@ConfigurationProperties("egovframe.opendata")
public class EgovOpenApiProperties {

    /** 공공데이터포털에서 발급받은 서비스키. 필수 값이며 기본값이 없다. */
    private String serviceKey;

    /**
     * serviceKey가 이미 URL 인코딩된 상태인지 여부.
     * 공공데이터포털은 발급 시 인코딩된 키와 디코딩된 키를 모두 제공하는데, 인코딩된 키를 그대로
     * 쿼리파라미터에 넣으면 WebClient가 다시 인코딩해 이중 인코딩 문제가 생길 수 있어 구분이 필요하다.
     */
    private boolean serviceKeyEncoded = false;

    /** 호출 대상 API의 base URL. 필수 값이며 기본값이 없다. */
    private String baseUrl;

    /** 연결 타임아웃(ms). 기본값 3000. */
    private int connectTimeoutMs = 3000;

    /** 응답 타임아웃(ms). 기본값 5000. */
    private int readTimeoutMs = 5000;

    /** 재시도 가능한 오류에 대한 최대 시도 횟수. 기본값 3. */
    private int maxRetry = 3;

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public boolean isServiceKeyEncoded() { return serviceKeyEncoded; }
    public void setServiceKeyEncoded(boolean v) { this.serviceKeyEncoded = v; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int v) { this.connectTimeoutMs = v; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int v) { this.readTimeoutMs = v; }
    public int getMaxRetry() { return maxRetry; }
    public void setMaxRetry(int v) { this.maxRetry = v; }
}
