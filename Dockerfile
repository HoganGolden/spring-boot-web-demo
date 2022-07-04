FROM openjdk:8-jdk

VOLUME /tmp
COPY  target/app.jar app.jar

ENV PORT 18094
EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "-Xms1G", "-Xmx2G", "/app.jar"]
