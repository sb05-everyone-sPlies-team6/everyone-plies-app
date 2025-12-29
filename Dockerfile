# 빌드 스테이지
FROM amazoncorretto:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 파일 먼저 복사
COPY gradle ./gradle
COPY gradlew ./gradlew

# Gradle 캐시를 위한 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사 및 빌드
COPY src ./src
# RUN ./gradlew build -x test # 테스트 제외함 빌드 빠름!
RUN ./gradlew build # 테스트 포함함! 빌드 느림!!

# 런타임 스테이지
FROM amazoncorretto:17-alpine3.21

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 8080 포트 노출
EXPOSE 8080

# jar 파일 실행
ENTRYPOINT ["sh","-c","java $JVM_OPTS -jar /app/app.jar"]