package com.porterhead.sms

import com.porterhead.api.sms.Message
import com.porterhead.sms.domain.SmsMessage
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Transform an SMSMessage to an API Message instance
 */
fun SmsMessage.toMessageResponse(): Message {
    val message: Message = Message()
    message.id = id
    message.fromNumber = fromNumber
    message.toNumber = toNumber
    message.text = text
    message.status = Message.StatusEnum.fromValue(status.name)
    message.provider = provider
    message.principal = principal
    message.createdAt = OffsetDateTime.ofInstant(createdAt, ZoneId.of("UTC"))
    message.updatedAt = OffsetDateTime.ofInstant(updatedAt, ZoneId.of("UTC"))
    return message
}


