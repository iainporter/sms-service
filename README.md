## SMS Service

See accompanying blog posts

* [Part One - Building a microservice from the ground up with quarkus kotlin and debezium]( https://medium.com/@changeant/building-a-microservice-from-the-ground-up-with-quarkus-kotlin-and-debezium-83ae5c8a8bbc)
* [Part Two - Implementing the transactional outbox pattern with debezium in quarkus](https://medium.com/@changeant/implementing-the-transactional-outbox-pattern-with-debezium-in-quarkus-f2680306951)
* [Part Three - Building a resilient microservice with quarkus and wiremock](https://levelup.gitconnected.com/building-a-resilient-microservice-with-quarkus-and-wiremock-de59b2a4fac7)
* [Part Four - Securing a microservice in quarkus with openid-connect](https://levelup.gitconnected.com/securing-a-microservice-in-quarkus-with-openid-connect-505204d1c9a9)
* [Part Five - Running a microservice in quarkus on graalvm](https://medium.com/@changeant/running-a-microservice-in-quarkus-on-graalvm-52d6b42a5840)
* [Containerizing your microservice in quarkus with jib](https://medium.com/@changeant/containerizing-your-microservice-in-quarkus-with-jib-fae0f62bd57e)

A production quality micro service for sending SMS messages that demonstrates the use of several technologies including:

* Quarkus
* GraalVM
* Kotlin
* Postgres (Persistence)
* Panache (JPA)
* Flyway (Database migration)
* Kafka (Messaging)
* Debezium, Kafka Connect (Transactional Outbox pattern)
* Okta (OIDC)
* Open API
* Docker
* Test Containers
* Wiremock

The service accepts SMS messages and routes them to configured providers

The service has support for Twilio and ClickSend 

New providers can easily be plugged in by implementing com.porterhead.sms.provider.SmsProvider

You can build the service, but it won't accept messages unless there is at least one provider configured

## The architecture

![Arch diagram](https://github.com/iainporter/sms-service/blob/master/images/sms_service.png?raw=true)

# Configuring the Service
To configure the service, sign up to Twilio and/or ClickSend and add the appropriate properties
to a config/application.properties file.

Sign up to Okta or any other OIDC provider of your choice and add the base url to the properties file

Create a client in your OIDC domain and add the client-id to the config

An example for Okta would be 

```
quarkus.oidc.auth-server-url=<Your Okta server url>>
quarkus.oidc.client-id=<your client-id>
```
 
See application.properties.sample 

Ensure the config directory is mounted by checking the path in the docker-compose.yml file

i.e.
```
    volumes:
      - ../config/application.properties:/deployments/config/application.properties
```

Build the service

```
 mvn install
 ```
 
To build the docker container (not needed if you run mvn install)

```
docker build -f sms-service/Dockerfile.jvm -t porterhead/sms-service .
```


docker-compose to bring the service up

```
cd sms-service
docker-compose up
```

Install the Sms connector
```
curl 'localhost:8083/connectors/' -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
-d '{"name": "sms-connector", "config": {"connector.class": "io.debezium.connector.postgresql.PostgresConnector", "database.hostname": "postgres-db", "database.port": "5432", "database.user": "postgres", "database.password": "postgres", "database.dbname" : "sms", "database.server.name": "smsdb1", "table.whitelist": "public.outboxevent", "transforms" : "outbox","transforms.outbox.type" : "io.debezium.transforms.outbox.EventRouter", "transforms.OutboxEventRouter.event.key": "aggregate_id", "transforms.outbox.table.fields.additional.placement": "type:header:eventType"}}'    
```

Login to the OIDC provider you configured above and obtain an access token

To send a message
```
curl 'http://localhost:8080/v1/sms' -i -X POST  \
   -H 'Content-Type: application/json'  \
   -H 'authorization: Bearer <your access token>'
   -d '{"text":"Foo Bar!", "fromNumber": "+1234567890", "toNumber": "+1234567891"}'
```

From the response you can check the status by using the location header in the response

i.e.
```
HTTP/1.1 202 Accepted
Content-Length: 0
Location: http://localhost:8080/v1/sms/b3a20fac-2d00-49d2-b3ef-b3a3e5ac02ca
```
```
curl 'http://localhost:8080/v1/sms/b3a20fac-2d00-49d2-b3ef-b3a3e5ac02ca' -i -X GET  \
   -H 'Content-Type: application/json'  \
   -H 'authorization: Bearer <your access token>'
```

you should see a response similar to this

```
{
"createdAt":"2020-07-16T16:43:59.43561Z",
"fromNumber":"+1234567890",
"id":"b3a20fac-2d00-49d2-b3ef-b3a3e5ac02ca",
"status":"DELIVERED",
"text":"Foo Bar!",
"provider": "Twilio",
"principal": "backend-service",
"toNumber":"+1234567891",
"updatedAt":"2020-07-16T16:44:00.432926Z"
}
```

# Running as a native executable on GraalVM

To build the native image you must have the native-image tool for GraalVM installed.

To build it 

```
mvn clean install -Pnative
```
