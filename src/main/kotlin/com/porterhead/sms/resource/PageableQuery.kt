package com.porterhead.sms.resource

import com.porterhead.sms.domain.MessageStatus
import io.quarkus.panache.common.Page

class PageableQuery(val page: Page, val queryRequest: QueryRequest) {
}
