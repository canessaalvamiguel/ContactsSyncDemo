package model.integration.contact

import play.api.libs.json.Json

case class Contact(id: String, firstName: String, lastName: String, email: String, avatar: String, createdAt: String)

object Contact{
  implicit val movieSummaryFormat = Json.format[Contact]
}
