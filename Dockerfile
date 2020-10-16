# Dockerfile to build container for unit testing.
#
# To build the image, run the following from this directory:
#   docker build -t testing .
#
# To run the tests, use
#   docker run testing

FROM openjdk:11

# Install Apache Ant
RUN apt-get update && apt-get install -y ant

# Install and configure VNC server
RUN apt-get update && apt-get install -y tightvncserver twm
ENV DISPLAY :0
ENV USER root
RUN mkdir /root/.vnc
RUN echo password | vncpasswd -f > /root/.vnc/passwd
RUN chmod 600 /root/.vnc/passwd

# Ant build fails if the repo dir isn't named linguaPhylo
RUN mkdir /root/linguaPhylo
WORKDIR /root/linguaPhylo

ADD . ./

# To run the tests interactively, use
#   docker run -it -p 5900:5900 beast_testing /bin/bash
# This will give you a shell in the container. From this
# shell, run
CMD vncserver $DISPLAY -geometry 1920x1080; ant travis
