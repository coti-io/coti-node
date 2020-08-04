FROM maven:3.5.4-jdk-8
ARG MODULE
ARG VERSION
ENV VERSION $VERSION
RUN mkdir /app
WORKDIR /app
COPY . .
RUN mvn -pl $MODULE -am initialize && mvn -pl $MODULE -am clean compile && mvn -pl $MODULE -am package -DskipTests
