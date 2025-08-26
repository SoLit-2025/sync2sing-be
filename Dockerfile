FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]