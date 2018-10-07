FROM maven:3.5.4-jdk-8
RUN mkdir /app
WORKDIR /app
RUN mkdir config
COPY basenode basenode
COPY dspnode dspnode
COPY pot pot
COPY libs libs
COPY poms/pom_dsp.xml pom.xml
RUN chmod 777 -R /app
RUN mvn initialize && mvn clean compile && mvn package

FROM openjdk:8
RUN mkdir /app
WORKDIR /app
RUN mkdir config
COPY --from=0 /app/dspnode/target/dspnode.jar dspnode.jar
COPY properties properties
COPY bashFiles bashFiles
COPY snapshot.csv snapshot.csv
RUN chmod 777 -R /app

