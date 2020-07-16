package com.porterhead.sms.event

import io.smallrye.reactive.messaging.kafka.KafkaRecord
import mu.KotlinLogging
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
class SmsMessageCreatedConsumer {

    @Inject
    lateinit var messageHandler : SmsMessageCreatedHandler

    private val log = KotlinLogging.logger{}

    @Incoming("sms")
    fun onMessage(message: KafkaRecord<String?, String?>): CompletionStage<Void?>? {
        log.debug { "Kafka message with key = $message.key arrived" }
        return CompletableFuture.runAsync {
            val eventId: String = getHeaderAsString(message, "id")
            val eventType: String = getHeaderAsString(message, "eventType")
            messageHandler.onEvent(
                    UUID.fromString(eventId),
                    eventType,
                    message.key!!,
                    message.payload!!,
                    message.timestamp
            )
        }
    }

    private fun getHeaderAsString(record: KafkaRecord<*, *>, name: String): String {
        val header = record.headers.lastHeader(name)
                ?: throw IllegalArgumentException("Expected record header '$name' not present")
        return String(header.value(), Charset.forName("UTF-8"))
    }
}
