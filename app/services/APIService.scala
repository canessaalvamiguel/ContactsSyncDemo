package services

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import impl.integration.contact.ContactAPISync
import impl.integration.contact.ContactAPISync.GetContacts
import javax.inject.Singleton
import model.integration.contact.Contact

import scala.concurrent.Future
import scala.concurrent.duration.MINUTES

@Singleton
class APIService {

  implicit val timeout = akka.util.Timeout(5, MINUTES)

  object ContactsSync{
    def props: Props = Props(new ContactAPISync())
  }

  val system = ActorSystem("ActorSystem")
  val contactsSyncActor = system.actorOf(ContactsSync.props, "ContactsSyncActor")

  def syncContacts():Future[Either[Exception, List[Contact]]] = {
    (contactsSyncActor ? GetContacts()).mapTo[Either[Exception, List[Contact]]]
  }

}
