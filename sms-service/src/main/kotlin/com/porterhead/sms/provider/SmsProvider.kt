package com.porterhead.sms.provider

import com.porterhead.sms.domain.SmsMessage

interface SmsProvider {

    /**
     * Send an SMS message to a provider
     */
    fun sendSms(message: SmsMessage): ProviderResponse

    fun getName(): String
}
