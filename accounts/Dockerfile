FROM openjdk:8
MAINTAINER Pankaj Semwal <pnkjsmwl09@gmail.com>
ADD target/accounts.jar accounts.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "accounts.jar"]