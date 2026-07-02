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

/**
 * egovframe-boot-starter-opendata 데모 애플리케이션.
 *
 * <p>실제 공공데이터포털 서비스키 없이도 {@code EgovOpenApiClient}의 동작을 확인할 수 있도록,
 * 내장 WireMock 서버로 공공데이터포털 응답을 흉내 낸다. 기동 시 두 가지 응답 케이스를 호출한다.
 * <ul>
 *   <li>{@code /normal} — 결과 2건, items.item이 배열로 오는 표준적인 응답</li>
 *   <li>{@code /solo} — 결과 1건, items.item이 배열이 아닌 단일 객체로 오는 버그 케이스</li>
 * </ul>
 * 두 응답 모두 {@code EgovOpenApiItemsNormalizer}를 거쳐 동일하게 {@code List<Item>}으로
 * 정규화되는 것을 콘솔 출력으로 확인할 수 있다.
 */
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EgovOpenApiClient egovOpenApiClient;

    /**
     * 데모 애플리케이션 진입점. 실행 후 {@link CommandLineRunner#run}이 끝나면 바로 종료한다.
     *
     * @param args 프로그램 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(DemoApplication.class, args));
    }

    /** WireMock 응답을 역직렬화할 데모용 항목 타입. */
    public record Item(String name) {}

    /**
     * WireMock으로 가상 공공데이터포털 서버를 띄운 뒤, 정상 배열 응답과 단일 객체 버그 응답을
     * 각각 호출해 정규화 결과를 콘솔에 출력한다.
     *
     * @param args 프로그램 인자 (사용하지 않음)
     * @throws Exception WireMock 서버 기동/호출 과정에서 오류가 발생한 경우
     */
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
