package controllers

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.APIService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class APIController @Inject()(cc : ControllerComponents, apiService: APIService) extends AbstractController(cc) {

  def syncContacts() = Action.async{ implicit request: Request[AnyContent] =>
    apiService.syncContacts map {
      case Right(contacts) => Ok(Json.toJson(contacts))
      case Left(exception) => BadRequest("Something went wrong")
    }
  }
}
