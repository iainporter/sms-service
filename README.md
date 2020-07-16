## SMS Service

WORK IN PROGRESS

Blog Posts to follow

A production quality micro service that demonstrates the use of several technologies including:

* Quarkus
* GraalVM
* Kotlin
* Persistence (Postgres)
* JPA (Panache)
* Messaging (Kafka)
* Outbox pattern (Debezium, Kafka Connect)
* Docker
* Test Containers
* Wiremock



Build the service

```
 mvn package
 ```
 
To build the docker container

```
docker build -f sms-service/Dockerfile.jvm -t porterhead/sms-service .
```


docker-compose to bring the service up

```
cd sms-service
docker-compose up
```

To send a message
```
curl 'http://localhost:8080/v1/sms' -i -X POST  \
   -H 'Content-Type: application/json'  \
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
curl http://localhost:8080/v1/sms/b3a20fac-2d00-49d2-b3ef-b3a3e5ac02ca
```
you should see a response similar to this

```
{
"createdAt":"2020-07-16T16:43:59.43561Z",
"fromNumber":"+1234567890",
"id":"b3a20fac-2d00-49d2-b3ef-b3a3e5ac02ca",
"status":"DELIVERED",
"text":"Foo Bar!",
"toNumber":"+1234567891",
"updatedAt":"2020-07-16T16:44:00.432926Z"
}
```
