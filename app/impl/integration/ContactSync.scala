package impl.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import model.integration.contact.{Contact, ContactAPI}
import net.liftweb.json.DefaultFormats
import net.liftweb.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

abstract class ContactSync {

  def getContacts: Future[Either[Exception, List[Contact]]]
}

class ContactAPISync extends ContactSync{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats
  import system.dispatcher

  override def getContacts: Future[Either[Exception, List[Contact]]] = {

    def generateUri: String = ContactAPI.contactsGetListOfContacts()

    def executeRequest(request: HttpRequest)  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(_.entity.toStrict(5.seconds))
      entityFuture.map(_.data.utf8String)
    }

    def parseJsonContacts(response: String) : Either[Exception, List[Contact]] = {
      val contacts = parse(response).extract[List[Contact]]
      Right(contacts)
    }

    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = generateUri
    )

    executeRequest(request).map(x => parseJsonContacts(x))

  }
}
