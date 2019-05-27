FROM openjdk:8
MAINTAINER Pankaj Semwal <pnkjsmwl09@gmail.com>
ADD target/authenticator.jar authenticator.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "authenticator.jar"]