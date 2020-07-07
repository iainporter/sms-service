package com.porterhead.testing.sms

import com.porterhead.testing.RestFunctions
import com.porterhead.testing.util.TestEnvironment
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test

class GetMessagesTest : TestEnvironment() {

    @Test
    fun `paging of messages`() {
        //create a 100 messages
        val request = """{"text":"Foo Bar", "fromNumber":"+1234567890", "toNumber":"+1234567899"}"""
        (1..100).forEach{
            RestFunctions.sendSmsMessage(request)}
        //get a page in the middle
        val response = RestFunctions.getMessages(5, 10)
        val json = response.body.jsonPath()
        MatcherAssert.assertThat(json.get("page.page"), Matchers.`is`(5))
        MatcherAssert.assertThat(json.get("page.pageSize"), Matchers.`is`(10))
        MatcherAssert.assertThat(json.get("page.numberOfElements"), Matchers.`is`(10))
        MatcherAssert.assertThat(json.get("page.totalElements"), Matchers.greaterThanOrEqualTo(100)) //data from other tests could take it over 100
    }

}
