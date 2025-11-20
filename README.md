# common-jdk25 가이드

이 문서는 common-jdk25 프로젝트의 구조를 간단히 파악하고, 빌드/실행 방법과 공통 코딩 가이드라인을 정리한 문서입니다. 팀 내에서 공통 모듈로 활용하거나 신규 서비스의 템플릿으로 사용할 때 참고하세요.

---

## 1. 개요
- 스택: Java 25, Spring Boot 3.5.x, Spring Web, Spring MVC, Spring Data JPA, Actuator
- 의존성: Lombok, Apache Commons Lang3, HttpClient5, micrometer tracing (OTel bridge), datasource-proxy, springdoc-openapi
- DB: H2 (runtimeOnly)
- 빌드: Gradle
- 목적: 공통 예외 처리, 접근 로그, HTTP 클라이언트 구성, 페이지 응답/요청 모델, 각종 유틸리티 제공

---

## 2. 프로젝트 구조
프로젝트 루트: `C:\Users\mk.jang\Desktop\TLC\common-jdk25`

```
common-jdk25
├─ build.gradle
├─ settings.gradle
├─ HELP.md, todo.md
├─ gradlew, gradlew.bat, gradle\wrapper
├─ logs\ (실행 시 로그 파일 생성 경로)
├─ lombok.config
└─ src
   ├─ main
   │  ├─ java\mingovvv\common
   │  │  ├─ CommonJdk25Application.java              # Spring Boot 애플리케이션 엔트리
   │  │  ├─ annotation\                              # Enum 유효성 검증 어노테이션
   │  │  │  ├─ EnumValidator.java
   │  │  │  └─ ValidEnum.java
   │  │  ├─ constants\                               # 결과 코드/타입
   │  │  │  ├─ ResultCode.java
   │  │  │  └─ ResultType.java
   │  │  ├─ controller\
   │  │  │  └─ TestController.java                   # 예제용 컨트롤러
   │  │  ├─ exception\                               # 예외 계층
   │  │  │  ├─ BusinessException.java
   │  │  │  ├─ ExternalApiException.java
   │  │  │  ├─ GlobalException.java
   │  │  │  └─ InternalServerException.java
   │  │  ├─ filter\                                  # 필터 및 요청 래퍼
   │  │  │  ├─ AccessLogFilter.java                  # 접근 로그 필터
   │  │  │  ├─ ExceptionHandlerFilter.java           # 필터 레벨 예외 처리
   │  │  │  └─ wrapper\
   │  │  │     └─ CustomRequestWrapper.java          # Request 내용 캐싱/가공
   │  │  ├─ handler\
   │  │  │  └─ GlobalExceptionHandler.java           # 전역 예외 처리 (ResponseEntityExceptionHandler 상속)
   │  │  ├─ http\                                    # RestClient 구성 및 DTO
   │  │  │  ├─ client\
   │  │  │  │  └─ TestServerClient.java
   │  │  │  ├─ config\
   │  │  │  │  ├─ RestClientBuilder.java
   │  │  │  │  └─ RestClientConfig.java
   │  │  │  ├─ dto\
   │  │  │  │  ├─ TestServerReq.java
   │  │  │  │  └─ TestServerRes.java
   │  │  │  └─ interceptor\
   │  │  │     └─ RestClientLoggingInterceptor.java
   │  │  ├─ model\                                    # 공통 응답/페이지 모델
   │  │  │  ├─ BaseResponse.java
   │  │  │  ├─ BaseResponseFactory.java
   │  │  │  ├─ PageInfo.java
   │  │  │  ├─ PageRequestDto.java
   │  │  │  └─ PageResponseDto.java
   │  │  └─ utils\                                    # 공통 유틸리티
   │  │     ├─ DateUtil.java
   │  │     ├─ JsonUtil.java
   │  │     ├─ MDCUtil.java
   │  │     ├─ MaskingUtil.java
   │  │     ├─ NetworkUtil.java
   │  │     └─ StringUtil.java
   │  └─ resources
   │     ├─ application.yml
   │     ├─ application-local.yml
   │     ├─ application-dev.yml
   │     ├─ application-prod.yml
   │     └─ logback-spring.xml
   └─ test
      └─ java\mingovvv\common\CommonJdk25ApplicationTests.java
```

---

## 3. 빌드 및 실행
- 요구 사항: JDK 25, Gradle(Wrapper 포함), 인터넷 연결(mavenCentral)
- 의존성/플러그인 주요 버전
  - Spring Boot 3.5.7
  - Java 25 Toolchain (Gradle 설정)
  - datasource-proxy 1.12.0
  - springdoc-openapi 2.8.14

명령어(프로젝트 루트에서 실행):
- 빌드: `./gradlew.bat build` (Windows PowerShell)
- 실행: `./gradlew.bat bootRun`
- 테스트: `./gradlew.bat test`

또는 IDE(IntelliJ)에서 CommonJdk25Application 실행 구성으로 실행할 수 있습니다.

> 참고: `CommonJdk25Application.main` 메서드는 일반적으로 `public static void main(String[] args)` 시그니처를 사용합니다. 현재 시그니처가 다르다면 IDE/빌드 실행 시 진입점 인식에 문제가 없는지 확인하세요.

---

## 4. 환경 설정(Profiles)
- `application.yml` 과 `application-<profile>.yml` 조합으로 환경 분리
  - local, dev, prod 제공
- 실행 시 프로파일 지정 방법
  - Gradle: `./gradlew.bat bootRun --args='--spring.profiles.active=local'`
  - JVM: `-Dspring.profiles.active=local`

주요 프로퍼티들(예상):
- 서버 포트, 로깅 레벨, HTTP 클라이언트 타임아웃, 외부 API 엔드포인트 등

---

## 5. 로깅
- `src/main/resources/logback-spring.xml` 구성
- 로그 파일 경로: `logs\mingo-server.log`, `logs\mingo-server-error.log`
- `AccessLogFilter` 로 요청/응답 요약 로그 남김
- `MDCUtil` 로 트레이스/요청 ID 등을 MDC에 저장하여 추적성 향상
- 민감정보는 `MaskingUtil` 을 통해 마스킹 처리 권장

---

## 6. 예외 처리 정책
- 전역 처리기: `GlobalExceptionHandler` (Spring MVC의 `ResponseEntityExceptionHandler` 확장)
  - 유효성 검증 오류, 요청 파싱 오류, 미지원 미디어 타입 등 표준 스프링 예외를 일관된 응답 포맷으로 변환
  - 비즈니스 예외(`BusinessException`), 외부 API 예외(`ExternalApiException`), 내부 서버 예외(`InternalServerException`) 세분화 처리
  - `BaseResponseFactory`를 통해 `BaseResponse` 생성, `ResultCode/ResultType` 기반 코드/메시지 관리
- 필터 레벨 예외: `ExceptionHandlerFilter`
  - 컨트롤러 진입 전 발생할 수 있는 예외 처리 보완

응답 표준(예시):
- `code`, `message`, `data`, `traceId` 포함

---

## 7. HTTP 클라이언트 구성(RestClient)
- 구성 위치: `http/config/RestClientConfig.java`, `RestClientBuilder.java`
- 인터셉터: `RestClientLoggingInterceptor` 로 요청/응답 로깅 및 마스킹
- HTTP 스택: Apache HttpClient5 + `HttpComponentsClientHttpRequestFactory` 사용
- 예제 클라이언트: `http/client/TestServerClient`
- 요청/응답 DTO: `http/dto/TestServerReq`, `TestServerRes`

사용 가이드(예시):
```java
@Autowired
private TestServerClient testServerClient;

var req = new TestServerReq(/* ... */);
var res = testServerClient.callSomething(req);
```
- 네트워크 타임아웃, 커넥션 풀 크기, 헤더, 리트라이 등은 Config에서 조정

---

## 8. 모델 및 페이징 공통
- `BaseResponse`, `BaseResponseFactory`: 표준 응답 래핑
- `PageRequestDto`, `PageResponseDto`, `PageInfo`: 페이징 요청/응답 모델로 컨트롤러와 리포지토리 사이 표준화

---

## 9. 유효성 검사 및 공통 어노테이션
- `@ValidEnum` + `EnumValidator` 로 Enum 기반 값 검증
- 컨트롤러 메서드 파라미터/DTO에 적용하여 잘못된 값 조기 차단

---

## 10. 유틸리티 모음
- `DateUtil`: 날짜/시간 포매팅, 파싱, 변환
- `JsonUtil`: JSON 직렬화/역직렬화 헬퍼(ObjectMapper 기반)
- `MaskingUtil`: 주민번호/전화번호/이메일 등 마스킹 유틸
- `MDCUtil`: MDC 키 관리(traceId, spanId 등)
- `NetworkUtil`: 클라이언트 IP, 헤더 관련 도우미
- `StringUtil`: 문자열 공통 처리

---

## 11. API 문서(OpenAPI)
- 의존성: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- 접속: `/swagger-ui.html` 또는 `/swagger-ui/index.html`
- 프로덕션 환경에서는 접근 제어 필요(화이트리스트/보안설정 권장)

---

## 12. 테스트
- JUnit Platform 사용
- 실행: `./gradlew.bat test`
- 필요 시 `@SpringBootTest` 기반 통합 테스트 추가

---

## 13. 코딩 컨벤션 & Git 가이드
- Java 스타일: 팀 표준(구글/네이버 컨벤션 등) + IDE 코드 스타일 적용
- Lombok 사용 시
  - `@Slf4j`, `@RequiredArgsConstructor`, `@Getter/@Setter` 등 일관되게 사용
  - 생성자 주입 우선, 필드 주입 지양
- 패키지 구조
  - `controller` → `service` → `repository` (필요 시)로 의존 방향 유지
  - 공통 모듈은 `utils`, `model`, `exception`, `handler`, `filter`, `http` 하위로 정리
- 예외 처리
  - 비즈니스 규칙 위반은 `BusinessException` 파생 사용
  - 외부 연동 실패는 `ExternalApiException` 사용, 원인(exception chain) 보존
- 커밋 메시지 컨벤션(예)
  - `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:` 프리픽스
  - 본문에 변경 이유와 영향 범위 명시
- 브랜치 전략
  - `main`(배포), `develop`(통합), `feature/*`, `hotfix/*`

---

## 14. 운영/모니터링 팁
- Actuator 활성화: 헬스체크(`/actuator/health`), 메트릭(`/actuator/metrics`)
- Micrometer + OTel 연동 시 Trace/Metrics 수집 가능
- 로그 로테이션/보존기간은 `logback-spring.xml`에서 정책화

---

## 15. 트러블슈팅 체크리스트
- 애플리케이션 실행 불가
  - JDK 25 사용 여부, Gradle Wrapper로 빌드했는지 확인
  - `main` 메서드 시그니처 확인(public static void main)
- 외부 호출 타임아웃
  - `RestClientConfig`의 타임아웃 설정 확인
- 응답 포맷이 다름
  - `GlobalExceptionHandler`, `BaseResponseFactory` 로직 확인
- 민감정보 로그 노출
  - `AccessLogFilter`, `RestClientLoggingInterceptor`, `MaskingUtil` 적용 여부 점검

---

## 16. 라이선스
- 사내/프로젝트 정책에 맞춰 LICENSE를 추가하세요(필요 시).
