FROM openjdk:8
MAINTAINER Pankaj Semwal <pnkjsmwl09@gmail.com>
ADD target/transaction.jar transaction.jar
EXPOSE 9096
ENTRYPOINT ["java", "-jar", "transaction.jar"]