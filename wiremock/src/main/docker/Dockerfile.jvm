FROM openjdk:8-jre

# meta
LABEL maintainer="Iain Porter<changeant@gmail.com>"

# vars
ENV WIREMOCK_VERSION 2.23.2

# new directories
RUN mkdir /var/wiremock
RUN mkdir /var/wiremock/mappings

# default directory
WORKDIR /var/wiremock

# download wiremock stand alone build
RUN wget -O /var/wiremock/wiremock-standalone.jar https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/$WIREMOCK_VERSION/wiremock-jre8-standalone-$WIREMOCK_VERSION.jar

# expose wiremock ports
EXPOSE 8080 8443

# start wiremock
CMD java -jar /var/wiremock/wiremock-standalone.jar
