FROM ubuntu:18.04

ARG MAVEN_VERSION=3.6.3
ARG BASE_MAVEN_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries
ARG JAVA_NAME=zulu8.36.0.1-ca-fx-jdk8.0.202-linux_x64
ARG COVERITY_NAME=cov-analysis-linux64
ARG PROJECT
ARG TOKEN

RUN apt-get update && apt-get upgrade -qy && \
    apt-get install -qy git wget gnupg curl

RUN cd /opt && curl -O https://cdn.azul.com/zulu/bin/${JAVA_NAME}.tar.gz && \
    tar -xzvf ${JAVA_NAME}.tar.gz && \
    rm -f ${JAVA_NAME}.tar.gz

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_MAVEN_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && rm -f /usr/bin/mvn \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

RUN wget -q https://scan.coverity.com/download/cxx/linux64 --post-data "token=$TOKEN&project=$PROJECT" -O ${COVERITY_NAME}.tar.gz && \
    mkdir /opt/${COVERITY_NAME} && \
    tar xzf ${COVERITY_NAME}.tar.gz --strip 1 -C /opt/${COVERITY_NAME} && \
    rm ${COVERITY_NAME}.tar.gz

ENV JAVA_HOME=/opt/${JAVA_NAME}
ENV PATH=/opt/${JAVA_NAME}/bin:/opt/${COVERITY_NAME}/bin:$PATH
