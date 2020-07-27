package com.porterhead.sms.provider

sealed class ProviderResponse {
    object SUCCESS : ProviderResponse()
    data class FAILED(val failureMessage: String) : ProviderResponse()
}
