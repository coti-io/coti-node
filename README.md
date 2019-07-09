#COTI_NODE

## requirements
Install java jdk ^1.8
Install mvn ^3.5.3

## build
mvn initialize && mvn clean compile && mvn package

## run localy

java -jar zerospend/target/zerospend-0.0.1-SNAPSHOT.jar --spring.config.additional-location=zerospend.properties

## using docker 
There is a dockerfile and docker compose in this folder

to build:
docker-compose build
to start:
docker-compose up -d
