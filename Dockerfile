FROM maven:latest

WORKDIR /cdn-slave
COPY . .
RUN mkdir /cdn
ENV FILE_PATH /cdn/
RUN mvn clean install
EXPOSE 6379
EXPOSE 6868
CMD mvn spring-boot:run