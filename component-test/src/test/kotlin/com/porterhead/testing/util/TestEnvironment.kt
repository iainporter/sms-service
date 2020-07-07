package com.porterhead.testing.util

import io.restassured.RestAssured
import org.junit.Before
import org.junit.ClassRule
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

abstract class TestEnvironment {

    @Before
    fun setup() {
        val port = env.getServicePort("sms-service_1", 8080)
        RestAssured.baseURI = "http://localhost:$port"
    }

    companion object {

        val env: KDockerComposeContainer by lazy {initDockerCompose()}
        class KDockerComposeContainer(path: File) : DockerComposeContainer<KDockerComposeContainer>(path)

        private fun initDockerCompose() = KDockerComposeContainer(File("src/test/resources/docker-compose.yml"))
                .withExposedService("sms-service_1", 8080)
                .withExposedService("postgres-db_1", 5432)
                .waitingFor("sms-service", Wait.forHttp("/health"))

        init {
            env.start()
        }
    }
}
