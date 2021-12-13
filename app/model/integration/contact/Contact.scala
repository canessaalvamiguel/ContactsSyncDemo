package model.integration.contact

import play.api.libs.json.Json

case class Contact(firstName: String, lastName: String, email: String)

object Contact{
  implicit val movieSummaryFormat = Json.format[Contact]
}
