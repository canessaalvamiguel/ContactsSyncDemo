package impl.integration.contact

import actors.ProjectActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import impl.integration.contact.ContactAPISync.GetContacts
import model.integration.contact.{Contact, ContactAPI}
import net.liftweb.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


object ContactAPISync {
  sealed trait ContactsSyncMessage

  case class GetContacts() extends  ContactsSyncMessage
}

class ContactAPISync() extends ProjectActor{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats
  import system.dispatcher

  override def handleMessage: Receive = {
    case GetContacts() => getContacts pipeTo sender()
  }

  def getContacts: Future[Either[Exception, List[Contact]]] = {

    def generateUri: String = ContactAPI.contactsGetListOfContactsEndpoint()

    def executeRequest(request: HttpRequest): Future[String]  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(_.entity.toStrict(5.seconds))
      entityFuture.map(_.data.utf8String)
    }

    def extractContactsFromJson(json: JValue): Either[Exception, List[Contact]] = {
      json.extractOpt[List[Contact]] match {
        case Some(contacts) => Right(contacts)
        case None => Left(new Exception("Error while extracting JSON response"))
      }
    }

    def parseJsonContacts(response: String) : Either[Exception, List[Contact]] = {
      parseOpt(response) match {
        case Some(json) => extractContactsFromJson(json)
        case None =>  Left(new Exception("Error while parsing JSON response"))
      }
    }

    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = generateUri
    )
    executeRequest(request).map(x => parseJsonContacts(x))
  }
}
