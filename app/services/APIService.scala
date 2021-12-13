package services

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import impl.integration.contact.ContactAPISync
import impl.integration.contact.ContactAPISync.GetContacts
import impl.integration.mailchimp.MailchimpAPISync
import impl.integration.mailchimp.MailchimpAPISync.{CreateList, DeleteList, GetIdExistingList}

import javax.inject.Singleton
import model.integration.contact.Contact

import scala.concurrent.Future
import scala.concurrent.duration.MINUTES

@Singleton
class APIService {

  implicit val timeout = akka.util.Timeout(5, MINUTES)
  import system.dispatcher

  object ContactsSync{
    def props: Props = Props(new ContactAPISync())
  }
  object MailchimpSync{
    def props: Props = Props(new MailchimpAPISync())
  }

  val system = ActorSystem("ActorSystem")
  val contactsSyncActor = system.actorOf(ContactsSync.props, "ContactsSyncActor")
  val mailchimpSyncActor = system.actorOf(MailchimpSync.props, "MailchimpSyncActor")

  def syncContacts():Future[Either[Exception, List[Contact]]] = {
    for {
      idList <- (mailchimpSyncActor ? GetIdExistingList()).mapTo[Either[Exception, Option[String]]].map(extractResult)
      deleteResponse <- (mailchimpSyncActor ? DeleteList(idList)).mapTo[Either[Exception, Option[Boolean]]].map(extractResult)
      continue = deleteResponse.getOrElse(false)
      if(continue)
      idNewList <- (mailchimpSyncActor ? CreateList()).mapTo[Either[Exception, Option[String]]].map(extractResult)
      contacts <- (contactsSyncActor ? GetContacts()).mapTo[Either[Exception, List[Contact]]]
    }yield {
      contacts
    }
  }

  def extractResult[T](result: Either[Exception, Option[T]]): Option[T] = {
    result match{
      case Right(id) => id
      case Left(exception) => None
    }
  }

}
