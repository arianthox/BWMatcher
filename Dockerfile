FROM openjdk:13-jdk-slim
LABEL maintainer="arianthox@gmail.com"
VOLUME /tmp
EXPOSE 8002
ARG JAR_FILE=build/libs/BWMatcher-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} BWMatcher-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/BWMatcher-0.0.1-SNAPSHOT.jar"]
