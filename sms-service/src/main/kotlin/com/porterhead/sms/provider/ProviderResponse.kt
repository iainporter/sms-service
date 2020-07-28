package com.porterhead.sms.provider

sealed class ProviderResponse {
    data class SUCCESS(val providerName: String) : ProviderResponse()
    data class FAILED(val providerName: String, val failureMessage: String) : ProviderResponse()
}
