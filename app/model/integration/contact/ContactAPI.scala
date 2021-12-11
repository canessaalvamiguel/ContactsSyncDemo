package model.integration.contact

object ContactAPI {

  val API_VERSION = "v1"
  val CONTACT_HOST = "https://613b9035110e000017a456b1.mockapi.io/api"

  def contactsGetListOfContactsEndpoint() = s"${getContactHost()}/contacts"

  def getContactHost() = CONTACT_HOST + "/" +API_VERSION

}
