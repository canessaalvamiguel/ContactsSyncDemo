package actors

import actors.AkkaMessages.InvalidMessageException
import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Terminated}
import com.typesafe.config.ConfigException
import play.api.Logger

import scala.util.{Failure, Success, Try}

object AkkaMessages {
  case class InvalidMessageException(invalidMsg: Any)
    extends Exception(s"Unknown message received: $invalidMsg")
}

trait ProjectActor extends Actor{
  val logger: Logger = Logger(this.getClass())

  override val supervisorStrategy = OneForOneStrategy() {
    case e =>
      logger.error(
        s"supervisorStrategy: an exception occurred: ${e.getMessage}\nActor handling exception: ${self.path.name}"
      )
      e match {
        case x: ConfigException => Resume
        case _                  => super.supervisorStrategy.decider.apply(e)
      }
  }

  def receiveMessage: Actor.Receive = {
    case msg @ akka.actor.Status.Failure(e) if !handleMessage.isDefinedAt(msg) =>
      logger.error(
        s"[${self.path.name}]: received an error from actor which I called. Error: $e"
      )
    case msg if handleMessage.isDefinedAt(msg) =>
      Try(handleMessage(msg)) match {
        case Failure(e) =>
          logger.error(
            s"Unhandled exception in receiveMessage(..) for Actor: ${self.path.name}."
          )
          // Throw is justified here because we need to make sure that
          // we don't break supervision functionality.
          sender() ! akka.actor.Status.Failure(e)
          throw e
        case Success(x) => x
      }
  }

  def handleMessage: Actor.Receive

  def receiveDefault: Actor.Receive = {
    case Terminated(actorRef) =>
      logger.error(
        s"an actor terminated unexpectedly ${actorRef}"
      )
    case e @ InvalidMessageException(msg) =>
      logger.warn(
        s"I sent a message that was not explicitly handled. Type: ${ msg.getClass.getSimpleName}. Exception: ${e.getMessage}"
      )
    case msg =>
      logger.warn(
        s"I received a message that was not explicitly handled. Type: ${msg.getClass.getSimpleName}. Message: ${msg}"
      )
      sender() ! InvalidMessageException(msg)
  }

  override def postStop() = {
    val actorName = self.path.name
    logger.error(
      s"$actorName stopped.  You may be interested in this event if you're debugging a [dead letter] or a [message timeout]"
    )
    super.postStop()
  }
  def receive: Actor.Receive = receiveMessage orElse receiveDefault

}
