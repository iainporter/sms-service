package com.porterhead.sms.resource

import io.quarkus.panache.common.Page

/**
 * DAO for wrapping query requests
 */
class PageableQuery(val page: Page, val queryRequest: QueryRequest)
