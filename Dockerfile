# Dockerfile to build container for unit testing.
#
# To build the image, run the following from this directory:
#   docker build -t testing .
#
# To run the tests, use
#   docker run testing

FROM openjdk:15.0.1-jdk-buster

RUN  apt-get update \
  && apt-get install -y wget \
  && rm -rf /var/lib/apt/lists/*

# Install Ant https://hub.docker.com/r/webratio/ant/dockerfile
ENV ANT_VERSION 1.10.9
RUN cd && \
    wget -q http://www.us.apache.org/dist//ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz && \
    tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz && \
    mv apache-ant-${ANT_VERSION} /opt/ant && \
    rm apache-ant-${ANT_VERSION}-bin.tar.gz
ENV ANT_HOME /opt/ant
ENV PATH ${PATH}:/opt/ant/bin

ENV USER root

# Ant build fails if the repo dir isn't named linguaPhylo
RUN mkdir /root/linguaPhylo
WORKDIR /root/linguaPhylo

ADD . ./

CMD ant github-actions
