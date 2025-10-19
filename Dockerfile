FROM maven:latest AS builder

WORKDIR /cdn-slave
COPY . .
RUN mvn clean install -Dmaven.test.skip=true

FROM amazoncorretto:17-alpine
WORKDIR /cdn-slave
COPY --from=builder /cdn-slave/target/*.jar app.jar
EXPOSE 6379
EXPOSE 6868
RUN mkdir /cdn
ENV FILE_PATH /cdn/
ENTRYPOINT ["java", "-jar", "app.jar"]
