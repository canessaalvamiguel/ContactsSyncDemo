package impl.integration.mailchimp

import actors.ProjectActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import net.liftweb.json.{DefaultFormats, parse}
import akka.pattern.pipe
import impl.integration.mailchimp.MailchimpAPISync.GetIdExistingList
import model.integration.mailchimp.{ListResponseMailchimp, MailchimpAPI}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object MailchimpAPISync {
  sealed trait MailchimpSyncMessage

  case class GetIdExistingList() extends  MailchimpSyncMessage
  case class DeleteList(idList: String) extends  MailchimpSyncMessage
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
  }

  def getIdExistingList: Future[Either[Exception, String]] = {
    def generateUri: String = MailchimpAPI.getListEndpoint()

    def executeRequest(request: HttpRequest)  = {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(_.entity.toStrict(5.seconds))
      entityFuture.map(_.data.utf8String)
    }

    def getListIdFromJson(response: String)  : Either[Exception, String] = {
      val listMailchimp = parse(response).extract[ListResponseMailchimp]
      Right(listMailchimp.lists(0).id)
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
