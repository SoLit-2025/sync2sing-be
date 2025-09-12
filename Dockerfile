FROM openjdk:17-jdk-slim

ENV DEBIAN_FRONTEND=noninteractive TZ=Asia/Seoul
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata ffmpeg ca-certificates && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]