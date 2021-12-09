package controllers

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class APIController @Inject()(cc : ControllerComponents) extends AbstractController(cc) {

  def processRequest() = Action.async{ implicit request: Request[AnyContent] =>
    Future(Ok("test"))
  }
}
