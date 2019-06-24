FROM openjdk:8
MAINTAINER Pankaj Semwal <pnkjsmwl09@gmail.com>
ADD target/configserver.jar configserver.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "configserver.jar"]