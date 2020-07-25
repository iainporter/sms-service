package com.porterhead.sms.provider.clicksend

class ClickSendData {
    val validResponse = """
        {
          "http_code": 200,
          "response_code": "SUCCESS",
          "response_msg": "Here are your data.",
          "data": {
            "total_price": 0.28,
            "total_count": 2,
            "queued_count": 2,
            "messages": [
              {
                "direction": "out",
                "date": 1436871253,
                "to": "+61411111111",
                "body": "Jelly liquorice marshmallow candy carrot cake 4Eyffjs1vL.",
                "from": "sendmobile",
                "schedule": 1436874701,
                "message_id": "BF7AD270-0DE2-418B-B606-71D527D9C1AE",
                "message_parts": 1,
                "message_price": 0.07,
                "custom_string": "this is a test",
                "user_id": 1,
                "subaccount_id": 1,
                "country": "AU",
                "carrier": "Telstra",
                "status": "SUCCESS"
              },
              {
                "direction": "out",
                "date": 1436871253,
                "to": "+61411111111",
                "body": "Chocolate bar icing icing oat cake carrot cake jelly cotton MWEvciEPIr.",
                "from": "sendlist",
                "schedule": 1436876011,
                "message_id": "D0C273EE-816D-4DF2-8E9D-9D9C65F168F3",
                "message_parts": 1,
                "message_price": 0.07,
                "custom_string": "this is a test",
                "user_id": 1,
                "subaccount_id": 1,
                "country": "AU",
                "carrier": "Telstra",
                "status": "SUCCESS"
              }
            ],
            "currency": {
              "currency_name_short": "USD",
              "currency_prefix_d": "${'$'}",
              "currency_prefix_c": "¢",
              "currency_name_long": "US Dollars"
            }
          }
        }
    """

    val unauthorizedResponse = """
        {
        "http_code":401,
        "response_code":"UNAUTHORIZED",
        "response_msg":"Authorization failed.",
        "data":null
        }
    """

    val invalidRecipient = """
        {
        	"http_code": 200,
        	"response_code": "SUCCESS",
        	"response_msg": "Messages queued for delivery.",
        	"data": {
        		"total_price": 0,
        		"total_count": 1,
        		"queued_count": 0,
        		"messages": [{
        			"to": "+44123123123",
        			"body": "Test Foo Bar",
        			"from": "",
        			"schedule": "",
        			"message_id": "7FCCC0C8-B327-4C4B-B9AD-9B330E75A50B",
        			"custom_string": "",
        			"status": "INVALID_RECIPIENT"
        		}],
        		"_currency": {
        			"currency_name_short": "AUD",
        			"currency_prefix_d": "${'$'}",
        			"currency_prefix_c": "c",
        			"currency_name_long": "Australian Dollars"
        		}
        	}
        }
    """
}
