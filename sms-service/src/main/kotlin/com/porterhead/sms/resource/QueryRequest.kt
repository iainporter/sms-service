package com.porterhead.sms.resource

import com.porterhead.sms.domain.MessageStatus

class QueryRequest private constructor(
        val status: MessageStatus?,
        val toNumber: String?,
        val sortString: String?
) {
    data class Builder(
            var status: MessageStatus? = null,
            var toNumber: String? = null,
            var sortString: String? = null
    ) {
        fun status(status: MessageStatus?) = apply { this.status = status }
        fun toNumber(toNumber: String?) = apply { this.toNumber = toNumber }
        fun sortString(sortString: String?) = apply { this.sortString = sortString }
        fun build() = QueryRequest(status, toNumber, sortString)
    }
}
