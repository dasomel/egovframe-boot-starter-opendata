# egovframe-boot-starter-opendata

[![CI](https://github.com/eGovFramework/egovframe-boot-starter-opendata/actions/workflows/ci.yml/badge.svg)](https://github.com/eGovFramework/egovframe-boot-starter-opendata/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

공공데이터포털(data.go.kr) 스타일 Open API 호출을 표준화하는 Spring Boot 자동구성 스타터(커뮤니티 구현)다.

> 본 저장소는 개인/커뮤니티 구현이며 표준프레임워크 센터의 공식 배포가 아니다.
> groupId `org.egovframe.boot`는 센터 소유 네임스페이스이며, Maven Central에 배포되지 않는다.
> 사용하려면 소스를 내려받아 `mvn install`로 로컬 저장소에 설치해야 한다.

## 왜 필요한가

공공데이터포털의 오픈API는 서비스마다 XML/JSON 스펙이 조금씩 다르지만 다음 특징을 공통으로 가진다.

- 응답 포맷이 `response.header.resultCode` / `response.header.resultMsg` / `response.body.items` 구조로 고정되어 있다.
- 결과가 1건일 때 `items.item`이 배열이 아니라 단일 객체로 오는 고질적인 버그가 있다.
- resultCode에 따라 재시도 가능 여부가 달라진다(일시적 오류 vs 요청 자체의 오류).

이 스타터는 이런 반복 작업을 서비스별로 다시 구현하지 않도록, 제네릭 클라이언트 한 벌로 표준화한다.

## 아키텍처

```
EgovOpenApiClient (WebClient 기반)
  └─ 요청 전송 (serviceKey, 파라미터 쿼리스트링 구성)
  └─ 응답 JsonNode 파싱 → resultCode 확인
        ├─ 실패(재시도 가능) → EgovOpenApiRetryPolicy 판단 → retryWhen으로 재시도
        ├─ 실패(재시도 불가) → EgovOpenApiException 즉시 발행
        └─ 성공 → EgovOpenApiItemsNormalizer로 items 정규화 → EgovOpenApiResponse<T> 반환
```

`EgovOpenApiItemsNormalizer`는 Jackson `JsonNode`만 다루는 순수 로직이라 Spring 컨텍스트 없이도 단위 테스트가 가능하다.
제네릭 타입 소거 문제를 피하기 위해 `JsonDeserializer` 서브클래싱 대신 이 방식을 택했다.

## 설정 키

| 키 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `egovframe.opendata.service-key` | String | (필수) | 공공데이터포털 발급 서비스키 |
| `egovframe.opendata.service-key-encoded` | boolean | `false` | 서비스키가 이미 URL 인코딩되어 있는지 여부 |
| `egovframe.opendata.base-url` | String | (필수) | 호출 대상 API 베이스 URL |
| `egovframe.opendata.connect-timeout-ms` | int | `3000` | 연결 타임아웃(ms) |
| `egovframe.opendata.read-timeout-ms` | int | `5000` | 응답 타임아웃(ms) |
| `egovframe.opendata.max-retry` | int | `3` | 재시도 가능한 오류에 대한 최대 시도 횟수 |

## 결과코드

| 코드 | 상수 | 재시도 가능 |
|---|---|---|
| 00 | NORMAL_SERVICE | - (성공) |
| 01 | APPLICATION_ERROR | X |
| 02 | DB_ERROR | X |
| 03 | NODATA_ERROR | X |
| 04 | HTTP_ERROR | O |
| 05 | SERVICETIMEOUT_ERROR | O |
| 10 | INVALID_REQUEST_PARAMETER_ERROR | X |
| 11 | NO_MANDATORY_REQUEST_PARAMETERS_ERROR | X |
| 12 | NO_OPENAPI_SERVICE_ERROR | X |
| 20 | SERVICE_ACCESS_DENIED_ERROR | X |
| 22 | LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR | O |
| 30 | SERVICE_KEY_IS_NOT_REGISTERED_ERROR | X |
| 31 | DEADLINE_HAS_EXPIRED_ERROR | X |
| 32 | UNREGISTERED_IP_ERROR | X |
| 33 | UNSIGNED_CALL_ERROR | X |
| 99 | UNKNOWN | X |

## 사용법

```xml
<dependency>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-opendata</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```yaml
egovframe:
  opendata:
    base-url: https://apis.data.go.kr/some/service
    service-key: ${OPENDATA_SERVICE_KEY}
    max-retry: 3
```

```java
@Service
public class WeatherService {

    private final EgovOpenApiClient client;

    public WeatherService(EgovOpenApiClient client) {
        this.client = client;
    }

    public List<WeatherItem> getForecast() {
        return client.fetch("/forecast", Map.of("numOfRows", 10, "pageNo", 1), WeatherItem.class)
                .block()
                .getItems();
    }
}
```

## 데모 실행

`samples/generic-demo`에 실제 서비스키 없이 동작을 확인할 수 있는 예시가 있다. 애플리케이션 기동 시 WireMock으로
가상 공공데이터포털 서버를 띄우고, 정상 배열 응답과 단일 객체 버그 응답 두 케이스를 호출해 정규화 결과를 콘솔에 출력한다.

```bash
mvn -q -DskipTests install
cd samples/generic-demo
mvn spring-boot:run
```

## 한계

- 1단계 구현은 JSON 응답만 지원한다. XML 응답 파싱은 지원하지 않는다.
- 실제 라이브 공공데이터포털 API에 대한 End-to-End 검증은 수행하지 않았다. WireMock 기반 통합테스트로 응답 파싱·재시도·예외 처리 로직만 검증했다.

## 라이선스

Apache License 2.0. 자세한 내용은 [LICENSE](LICENSE) 참고.
