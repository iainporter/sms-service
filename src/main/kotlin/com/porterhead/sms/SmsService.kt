package com.porterhead.sms

import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage

interface SmsService {

    fun createMessage(request: SendSmsRequest): SmsMessage
}
