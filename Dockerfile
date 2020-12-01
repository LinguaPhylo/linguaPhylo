# Dockerfile to build container for unit testing.
#
# To build the image, run the following from this directory:
#   docker build -t testing .
#
# To run the tests, use
#   docker run testing

FROM openjdk:15

# Install Apache Ant
RUN apt-get update && apt-get install -y ant

ENV USER root

# Ant build fails if the repo dir isn't named linguaPhylo
RUN mkdir /root/linguaPhylo
WORKDIR /root/linguaPhylo

ADD . ./

CMD ant travis
