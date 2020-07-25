package com.porterhead.sms.provider

/**
 * General exception to cover 5xx responses
 */
class ServerException(s: String) : ProviderException(s) {
}
