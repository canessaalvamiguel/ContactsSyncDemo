package model.integration.mailchimp

object MailchimpAPI {
  val API_VERSION = "3.0"
  val MAILCHIMP_SERVER = "us20"
  val MAILCHIMP_HOST = s"https://${MAILCHIMP_SERVER}.api.mailchimp.com"
  val auth_token = "39a326ca19a0d1cb25746928cb3e07d2-us20"

  def getMailChimpHost() = s"${MAILCHIMP_HOST}/${API_VERSION}"

  def getListEndpoint() = s"${getMailChimpHost()}/lists/"
  def deleteListEndpoint(idList: String) = s"${getMailChimpHost()}/lists/${idList}"
  def createListEndpoint() = s"${getMailChimpHost()}/lists/"
  def getMembersEndpoint(idList: String) = s"${getMailChimpHost()}/lists/${idList}/members/"
  def createMemberEndpoint(idList: String, skipMemberValidation: Boolean = true) = {
    val skipMemberValidationParam = if(skipMemberValidation) "?skip_merge_validation=true" else ""
    s"${getMailChimpHost()}/lists/${idList}/members${skipMemberValidationParam}"
  }

}
