FROM maven:3.5.4-jdk-10
RUN mkdir /app
RUN mkdir /configs
VOLUME /configs
WORKDIR /app
COPY . .
RUN mvn initialize && mvn clean compile && mvn package
