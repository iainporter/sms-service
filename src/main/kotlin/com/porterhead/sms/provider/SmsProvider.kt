package com.porterhead.sms.provider

import com.porterhead.sms.domain.SmsMessage

interface SmsProvider {

    fun sendSms(message: SmsMessage)
}
