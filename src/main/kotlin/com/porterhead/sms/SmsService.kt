package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage
import java.util.*

interface SmsService {

    fun createMessage(request: SendSmsRequest): SmsMessage

    fun getMessage(id: UUID): SmsMessage
}
