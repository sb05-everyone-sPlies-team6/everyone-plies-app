# ========================
# 빌드 스테이지
# ========================
FROM amazoncorretto:17 AS builder

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle ./gradle

# 실행 권한 + CRLF -> LF 자동 변환
RUN chmod +x ./gradlew && sed -i 's/\r$//' ./gradlew

# Gradle 캐시용 의존성 복사
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사
COPY src ./src
RUN ./gradlew build -x test # 테스트 제외함 빌드 빠름!
# RUN ./gradlew build # 테스트 포함함! 빌드 느림!!

# 빌드
RUN ./gradlew build -x test --no-daemon

# ========================
# 런타임 스테이지
# ========================
FROM amazoncorretto:17-alpine3.21

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JVM_OPTS -jar /app/app.jar"]
