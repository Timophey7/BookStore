FROM openjdk:17

COPY target/library-0.0.1-SNAPSHOT.jar library-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar","/library-service.jar"]
