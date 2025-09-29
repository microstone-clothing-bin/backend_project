# Java 21과 Gradle이 설치된 이미지를 사용
FROM gradle:jdk21 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
# 테스트는 제외하고 빌드해서 속도 향상
RUN gradle build --no-daemon -x test

# 더 가벼운 실행 전용 Java 21 이미지 사용
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar
EXPOSE 8080
# 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]