package org.egovframe.boot.opendata.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("egovframe.opendata")
public class EgovOpenApiProperties {
    private String serviceKey;
    private boolean serviceKeyEncoded = false;
    private String baseUrl;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
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
