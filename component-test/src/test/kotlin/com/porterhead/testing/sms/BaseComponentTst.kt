package com.porterhead.testing.sms

import io.debezium.testing.testcontainers.DebeziumContainer
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.awaitility.Awaitility
import org.junit.BeforeClass
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.lifecycle.Startables
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

abstract class BaseComponentTst {

    companion object {
        val network: Network = Network.newNetwork()

        val kafkaContainer = KafkaContainer()
                .withNetwork(network)
                .withNetworkAliases("kafka")

        var postgresContainer: PostgreSQLContainer<*> = KPostgreSQLContainer("debezium/postgres:11")
                .withNetwork(network)
                .withNetworkAliases("postgres-db")
                .withUsername("postgres")
                .withPassword("postgres")
                .withDatabaseName("sms")

        var debeziumContainer = DebeziumContainer("debezium/connect:1.2.0.Final")
                .withNetwork(network)
                .withExposedPorts(8083)
                .withKafka(kafkaContainer)
                .dependsOn(kafkaContainer)

        var smsServiceContainer = KGenericContainer("porterhead/sms-service")
                .withNetwork(network)
                .withNetworkAliases("sms-service")
                .withExposedPorts(8080)
                .withEnv("quarkus.datasource.username", "postgres")
                .withEnv("quarkus.datasource.password", "postgres")
                .withEnv("quarkus.datasource.jdbc.url", "jdbc:postgresql://postgres-db:5432/sms")
                .dependsOn(postgresContainer)


        @BeforeClass
        @JvmStatic
        fun startContainers() {
            Startables.deepStart(Stream.of(
                    kafkaContainer, postgresContainer, debeziumContainer, smsServiceContainer))
                    .join()
            registerKafkaConnector(debeziumContainer.getMappedPort(8083))
            waitForConnector(debeziumContainer.getMappedPort(8083))
            val port = smsServiceContainer.firstMappedPort
            RestAssured.baseURI = "http://localhost:$port"
        }

        fun registerKafkaConnector(port: Int) {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(connectorRequest)
                    .post("http://localhost:$port/connectors")
        }

        fun waitForConnector(port: Int) {
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until {
                val response = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .get("http://localhost:$port/connectors/sms-connector")
                        .then()
                        .extract()
                        .response()
                response.statusCode == 200

            }

        }

        val connectorRequest = """
        {
        "name": "sms-connector",
        "config": {
            "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
		    "database.hostname": "postgres-db",
		    "database.port": "5432",
		    "database.user": "postgres",
		    "database.password": "postgres",
		    "database.dbname": "sms",
		    "database.server.name": "smsdb1",
		    "table.whitelist": "public.outboxevent",
		    "transforms": "outbox",
		    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
		    "transforms.OutboxEventRouter.event.key": "aggregate_id",
		    "transforms.outbox.table.fields.additional.placement": "type:header:eventType"
            }
        } 
        """

    }


    class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)

    class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

//  Uncomment to use docker-compose file instead of code above
    // could not get it to work as the 8083 port in kafka connect could not be reached for unknown reason
//    companion object {
//
//        val env: KDockerComposeContainer by lazy {initDockerCompose()}
//        class KDockerComposeContainer(path: File) : DockerComposeContainer<KDockerComposeContainer>(path)
//
//        private fun initDockerCompose() = KDockerComposeContainer(File("src/test/resources/docker-compose.yml"))
//                .withExposedService("sms-service_1", 8080)
//                .withExposedService("postgres-db_1", 5432)
//                .withExposedService("kafka_1", 9092)
////                .withExposedService("kafka-connect_1", 8083)
//                .waitingFor("sms-service", Wait.forHttp("/health"))
//
//        init {
//            env.start()
//        }
//    }
}
