

FROM maven:3.5.4-jdk-10
# VOLUME /tmp
# ARG JAR_FILE
# COPY ${JAR_FILE} app.jar
# ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
RUN mkdir /app
RUN mkdir /configs
VOLUME /configs
WORKDIR /app
COPY . .
RUN mvn initialize && mvn clean compile && mvn package
