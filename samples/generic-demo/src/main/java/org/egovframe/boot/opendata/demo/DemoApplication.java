package org.egovframe.boot.opendata.demo;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.egovframe.boot.opendata.client.EgovOpenApiClient;
import org.egovframe.boot.opendata.model.EgovOpenApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EgovOpenApiClient egovOpenApiClient;

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(DemoApplication.class, args));
    }

    public record Item(String name) {}

    @Override
    public void run(String... args) throws Exception {
        WireMockServer wireMockServer = new WireMockServer(options().port(18080));
        wireMockServer.start();
        try {
            wireMockServer.stubFor(get(urlPathEqualTo("/normal")).willReturn(okJson(
                    "{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
                            + "\"body\":{\"items\":{\"item\":[{\"name\":\"first\"},{\"name\":\"second\"}]},"
                            + "\"numOfRows\":10,\"pageNo\":1,\"totalCount\":2}}}")));
            wireMockServer.stubFor(get(urlPathEqualTo("/solo")).willReturn(okJson(
                    "{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
                            + "\"body\":{\"items\":{\"item\":{\"name\":\"only-one\"}},"
                            + "\"numOfRows\":10,\"pageNo\":1,\"totalCount\":1}}}")));

            EgovOpenApiResponse<Item> normal = egovOpenApiClient
                    .fetch("/normal", java.util.Map.of(), Item.class)
                    .block();
            System.out.println("정상 응답(배열 items): " + normal.getItems());

            EgovOpenApiResponse<Item> solo = egovOpenApiClient
                    .fetch("/solo", java.util.Map.of(), Item.class)
                    .block();
            System.out.println("단일 객체 버그 정규화 결과: " + solo.getItems());
        } finally {
            wireMockServer.stop();
        }
    }
}
