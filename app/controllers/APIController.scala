package controllers

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.APIService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class APIController @Inject()(cc : ControllerComponents, apiService: APIService) extends AbstractController(cc) {

  def syncContacts() = Action.async{ implicit request: Request[AnyContent] =>
    apiService.syncContacts() map {
      case Some(contacts) => Ok(
        Json.obj("syncedContacts" -> contacts.length, "contacts" -> contacts)
      )
      case None => BadRequest(
        Json.obj("error" -> "Something went wrong")
      )
    }
  }
}
