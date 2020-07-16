package com.porterhead.sms.event

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.porterhead.sms.domain.SmsMessage
import io.debezium.outbox.quarkus.ExportedEvent
import java.time.Instant
import java.util.*

/**
 * An event that gets written to the Outbox table when a message is created
 */
class SmsMessageCreatedEvent (private val id: UUID,
                              private val node: JsonNode,
                              private val kTimestamp: Instant = Instant.now()) : ExportedEvent<String, JsonNode> {

    companion object {
        private val mapper = ObjectMapper()
        fun fromSmsMessage(smsMessage: SmsMessage): SmsMessageCreatedEvent {
            val asJson: ObjectNode = mapper.createObjectNode()
                    .put("id", smsMessage.id.toString())
                    .put("fromNumber", smsMessage.fromNumber)
                    .put("toNumber", smsMessage.toNumber)
                    .put("text", smsMessage.text)
                    .put("status", smsMessage.status.name)
            return SmsMessageCreatedEvent(smsMessage.id, asJson);
        }
    }
    override fun getAggregateId(): String {
        return id.toString()
    }

    override fun getPayload(): JsonNode {
        return node
    }

    override fun getType(): String {
        return "message_created"
    }

    override fun getTimestamp(): Instant {
        return kTimestamp
    }

    override fun getAggregateType(): String {
        return "sms_message"
    }
}
