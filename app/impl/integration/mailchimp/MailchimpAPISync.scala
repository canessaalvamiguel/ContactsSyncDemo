package impl.integration.mailchimp

import actors.ProjectActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCode}
import akka.stream.ActorMaterializer
import net.liftweb.json.{DefaultFormats, JValue, parseOpt}
import akka.pattern.pipe
import impl.integration.mailchimp.MailchimpAPISync.{CreateList, DeleteList, GetIdExistingList}
import model.integration.mailchimp.{ListMailchimp, ListMailchimpProjection, ListResponseMailchimp, MailchimpAPI}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object MailchimpAPISync {
  sealed trait MailchimpSyncMessage

  case class GetIdExistingList() extends  MailchimpSyncMessage
  case class DeleteList(idList: Option[String]) extends  MailchimpSyncMessage
  case class CreateList() extends MailchimpSyncMessage
  case class AddMembers() extends MailchimpSyncMessage
}

class MailchimpAPISync() extends ProjectActor{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats
  import system.dispatcher

  override def handleMessage: Receive = {
    case GetIdExistingList() => getIdExistingList pipeTo sender()
    case DeleteList(id) => deleteExistingList(id) pipeTo sender()
    case CreateList() => createList pipeTo sender()
  }

  def createList: Future[Either[Exception, Option[String]]] = {
    def generateUri(): String = MailchimpAPI.createListEndpoint()

    def executeRequest(request: HttpRequest): Future[String]  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(_.entity.toStrict(5.seconds))
      entityFuture.map(_.data.utf8String)
    }

    def extractIdFromJson(json: JValue): Either[Exception, Option[String]] = {
      json.extractOpt[ListMailchimp] match {
        case Some(listmailchimp) =>
          Right(Some(listmailchimp.id))
        case None => Left(new Exception("Error while extracting JSON response"))
      }
    }

    def getListIdFromJson(response: String)  : Either[Exception, Option[String]] = {
      println(response)
      parseOpt(response) match {
        case Some(json) => extractIdFromJson(json)
        case None => Left(new Exception("Error while parsing JSON response"))
      }
    }

    val authorization = Authorization(BasicHttpCredentials("Mailchimp", MailchimpAPI.auth_token))
    val body = ListMailchimpProjection.createDefaultListMailchimpJson()
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = generateUri(),
      headers = List(authorization),
      entity = HttpEntity(ContentTypes.`application/json`, body)
    )
    executeRequest(request).map(x => getListIdFromJson(x))
  }

  def deleteExistingList(idList : Option[String]): Future[Either[Exception, Option[Boolean]]] = {
    def generateUri(idList: String): String = MailchimpAPI.deleteListEndpoint(idList)

    def executeRequest(request: HttpRequest): Future[StatusCode]  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      responseFuture.map(_.status)
    }

    idList match {
      case Some(value) =>
        val authorization = Authorization(BasicHttpCredentials("Mailchimp", MailchimpAPI.auth_token))
        val request = HttpRequest(
          method = HttpMethods.DELETE,
          uri = generateUri(value),
          headers = List(authorization)
        )
        executeRequest(request).map( status => status.isSuccess() match {
          case true => Right(Some(true))
          case false => Left(new Exception("Something went wrong"))
        })
      case None => Future.successful(Right(Some(true)))
    }
  }

  def getIdExistingList: Future[Either[Exception, Option[String]]] = {
    def generateUri: String = MailchimpAPI.getListEndpoint()

    def executeRequest(request: HttpRequest): Future[String]  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(_.entity.toStrict(5.seconds))
      entityFuture.map(_.data.utf8String)
    }

    def extractIdFromJson(json: JValue): Either[Exception, Option[String]] = {
      json.extractOpt[ListResponseMailchimp] match {
        case Some(listmailchimp) =>
          if(listmailchimp.lists.isEmpty)
            Left(new Exception("No list found on Mailchimp"))
          else {
           listmailchimp.lists.lift(0).map(_.id) match {
             case Some(element) => Right(Some(element))
             case None => Left(new Exception("Something went wrong"))
           }
          }
        case None => Left(new Exception("Error while extracting JSON response"))
      }
    }

    def getListIdFromJson(response: String)  : Either[Exception, Option[String]] = {
      parseOpt(response) match {
        case Some(json) => extractIdFromJson(json)
        case None => Left(new Exception("Error while parsing JSON response"))
      }
    }

    val authorization = Authorization(BasicHttpCredentials("Mailchimp", MailchimpAPI.auth_token))
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = generateUri,
      headers = List(authorization)
    )
    executeRequest(request).map(x => getListIdFromJson(x))
  }
}
