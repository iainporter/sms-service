package com.porterhead.sms

import com.porterhead.api.sms.PagedMessageResponse
import com.porterhead.api.sms.PagedMessageResponsePage
import com.porterhead.api.sms.SendSmsRequest
import com.porterhead.sms.domain.SmsMessage
import com.porterhead.sms.jpa.MessageRepository
import com.porterhead.sms.resource.PageableQuery
import io.quarkus.hibernate.orm.panache.PanacheQuery
import io.quarkus.panache.common.Sort
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.NotFoundException

@ApplicationScoped
class DefaultSmsService: SmsService {

    companion object {
        val DEFAULT_SORT: Sort = Sort.by("updatedAt").descending()
    }

    @Inject
    lateinit var messageRepository: MessageRepository

    @Transactional
    override fun createMessage(request: SendSmsRequest): SmsMessage {
        var entity = SmsMessage(
                fromNumber = request.fromNumber,
                toNumber = request.toNumber,
                text = request.text)
        messageRepository.persist(entity)
        return entity
    }

    override fun getMessage(id: UUID): SmsMessage {
        val message: Optional<SmsMessage> = messageRepository.findByIdOptional(id)
        if (!message.isPresent) {
            throw NotFoundException()
        }
        return message.get()
    }

    override fun getMessages(pageableQuery: PageableQuery): PagedMessageResponse {
        val sort = buildSort(pageableQuery)
        var params = mutableMapOf<String, Any>()
        var queryString: String? = null
        if (pageableQuery.queryRequest.status != null) {
            params.put("status", pageableQuery.queryRequest.status)
            queryString = "status = :status"
        }
        if (pageableQuery.queryRequest.toNumber != null) {
            params.put("toNumber", pageableQuery.queryRequest.toNumber)
            if (queryString.isNullOrEmpty()) {
                queryString = "toNumber = :toNumber"
            } else {
                queryString += " and toNumber = :toNumber"
            }
        }
        val queryResult : PanacheQuery<SmsMessage> = if (queryString != null) {
            messageRepository.find(queryString, sort, params).page(pageableQuery.page.index, pageableQuery.page.size)
        } else {
            messageRepository.findAll(sort).page(pageableQuery.page.index, pageableQuery.page.size)
        }
        val items = queryResult.list<SmsMessage>()
        return buildPagedMessageResponse(pageableQuery, items, queryResult)
    }

    private fun buildSort(pageableQuery: PageableQuery): Sort? {
        if(pageableQuery.queryRequest.sortString == null) {
            return DEFAULT_SORT
        }
        val sortList = pageableQuery.queryRequest.sortString.split(",")
        var sortPair = splitSortPair(sortList[0])
        var sort = Sort.by(sortPair.first).direction(sortPair.second)
        for (i in 1 until sortList.size) {
            sortPair = splitSortPair(sortList[i])
            sort.and(sortPair.first).direction(sortPair.second)
        }
        return sort
    }

    private fun splitSortPair(sortPairString: String): Pair<String, Sort.Direction>{
        val list = sortPairString.split(":")
        if (list.size == 1) {
            return Pair(list[0], Sort.Direction.Ascending) //default
        } else {
            return Pair(list[0], if(list[1] == "asc") Sort.Direction.Ascending else Sort.Direction.Descending)
        }
    }

    private fun buildPagedMessageResponse(pageableQuery: PageableQuery, items: MutableList<SmsMessage>, queryResult: PanacheQuery<SmsMessage>): PagedMessageResponse {
        var response = PagedMessageResponse()
        var page = PagedMessageResponsePage()
        page.page = pageableQuery.page.index
        page.pageSize = pageableQuery.page.size
        page.numberOfElements = items.size
        page.totalElements = queryResult.count()
        page.totalPages = queryResult.pageCount()
        response.page = page
        response.content = items.map { smsMessage -> smsMessage.toMessageResponse() }
        return response
    }
}
