package com.porterhead.sms.provider

import com.porterhead.sms.domain.SmsMessage

interface ProviderRouter {

    fun routeMessage(message: SmsMessage): ProviderResponse
}
