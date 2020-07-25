package com.porterhead.sms.provider.twilio

class TwilioData {

    val validResponse = """
        {
          "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
          "api_version": "2010-04-01",
          "body": "This will be the body of the new message!",
          "date_created": "Thu, 30 Jul 2015 20:12:31 +0000",
          "date_sent": "Thu, 30 Jul 2015 20:12:33 +0000",
          "date_updated": "Thu, 30 Jul 2015 20:12:33 +0000",
          "direction": "outbound-api",
          "error_code": null,
          "error_message": null,
          "from": "+14155552345",
          "messaging_service_sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
          "num_media": "0",
          "num_segments": "1",
          "price": null,
          "price_unit": null,
          "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
          "status": "sent",
          "subresource_uris": {
            "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
          },
          "to": "+14155552345",
          "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
        }
    """

    val unauthorizedResponse = """
        {
        "code": 20003,
        "detail": "Your AccountSid or AuthToken was incorrect.",
        "message": "Authentication Error - invalid username",
        "more_info": "https://www.twilio.com/docs/errors/20003",
        "status": 401
        }

    """

    val badData = """
        {
           "code":21212,
           "message":"The 'From' number 1234567 is not a valid phone number, shortcode, or alphanumeric sender ID.",
           "more_info":"https://www.twilio.com/docs/errors/21212",
           "status":400
        }
    """

    val serviceUnavailable = """
        {
           "message":"The service is unavailable",
           "status":503
        }
    """
}
