package com.porterhead.sms

import com.porterhead.api.sms.Message
import com.porterhead.sms.domain.SmsMessage
import io.quarkus.panache.common.Page
import java.time.OffsetDateTime
import java.time.ZoneId


fun SmsMessage.toMessageResponse(): Message {
    val message: Message = Message()
    message.id = id
    message.fromNumber = fromNumber
    message.toNumber = toNumber
    message.text = text
    message.status = Message.StatusEnum.fromValue(status.name)
    message.createdAt = OffsetDateTime.ofInstant(createdAt, ZoneId.of("UTC"))
    message.updatedAt = OffsetDateTime.ofInstant(updatedAt, ZoneId.of("UTC"))
    return message
}


