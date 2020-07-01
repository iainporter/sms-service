package com.porterhead.sms.resource

import io.quarkus.panache.common.Page

class PageableQuery(val page: Page, val queryRequest: QueryRequest) {
}
