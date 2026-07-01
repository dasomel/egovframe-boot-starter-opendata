# generic-demo

`egovframe-boot-starter-opendata`를 실제 애플리케이션에서 어떻게 사용하는지 보여주는 예시 프로젝트다.
실제 공공데이터포털 서비스키 없이도 동작을 확인할 수 있도록, 애플리케이션 기동 시 WireMock으로 가상 서버를 띄워
공공데이터포털 스타일 응답(정상 배열 케이스 + 단일 객체 버그 케이스)을 스텁하고 `EgovOpenApiClient`로 두 번 호출한다.

## 실행 방법

먼저 루트 모듈을 로컬 저장소에 설치한다.

```bash
cd egovframe-boot-starter-opendata
mvn -q -DskipTests install
```

그 다음 데모를 실행한다.

```bash
cd samples/generic-demo
mvn spring-boot:run
```

## 기대 출력

```
정상 응답(배열 items): [Item[name=first], Item[name=second]]
단일 객체 버그 정규화 결과: [Item[name=only-one]]
```

애플리케이션은 두 결과를 출력한 뒤 `SpringApplication.exit()`로 자동 종료된다.
