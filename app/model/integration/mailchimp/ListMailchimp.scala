package model.integration.mailchimp

case class ListResponseMailchimp(lists: List[ListMailchimp])
case class ListMailchimp(id: String, name: String)
