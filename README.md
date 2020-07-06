## SMS Service

*****WORK IN PROGRESS ****

Micro Service for delivering SMS messages
The sample providers are Twilio and Sendclick

Make sure you build the dependent Open API module first (see https://github.com/iainporter/sms-openapi)

Build the service

`cd sms-service
 mvn package
 `
 
To build the docker container

`docker build -f Dockerfile.jvm -t porterhead/sms-service .`


Then run docker-compose to bring the service up

`docker-compose up`

