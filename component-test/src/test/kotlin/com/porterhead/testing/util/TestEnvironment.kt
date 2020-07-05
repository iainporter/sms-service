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
        RestAssured.baseURI = "http://localhost:8080"
    }

    companion object {

        class KDockerComposeContainer(path: File) : DockerComposeContainer<KDockerComposeContainer>(path)

        @ClassRule
        @JvmField
        val env = KDockerComposeContainer(File("src/test/resources/docker-compose.yml"))
                .waitingFor("sms-service", Wait.forHttp("/v1/sms"))
    }
}
