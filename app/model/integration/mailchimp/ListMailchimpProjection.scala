package model.integration.mailchimp

import model.integration.contact.Contact

import java.time.LocalDateTime

object ListMailchimpProjection {
  def createListMembersJson(contact: Contact) = {
    s"""
     {
        "email_address": "${contact.email}",
        "status": "subscribed",
        "merge_fields": {
            "FNAME": "${contact.firstName}",
            "LNAME": "${contact.lastName}",
            "ADDRESS": "Trujillo",
            "PHONE": ""
        }
    }
    """
  }


  def createDefaultListMailchimpJson() = {
    s"""
        {
          "name": "MiguelCanessa",
          "contact": {
              "company": "Trio",
              "address1": "Address1",
              "address2": "${LocalDateTime.now()}",
              "city": "Trujillo",
              "state": "La Libertad",
              "zip": "1300110",
              "country": "PERU",
              "phone": ""
          },
          "permission_reminder": "All is ok",
          "campaign_defaults": {
              "from_name": "Miguel Canessa",
              "from_email": "canessaalvamiguel@gmail.com",
              "subject": "From Canessa",
              "language": "en"
          },
          "notify_on_subscribe": "",
          "notify_on_unsubscribe": "",
          "email_type_option": false,
          "double_optin": false,
          "marketing_permissions": false
      }
    """
  }

}
