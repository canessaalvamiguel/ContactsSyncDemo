package services

import impl.integration

import javax.inject.Singleton
import impl.integration.ContactAPISync

import scala.concurrent.Future

@Singleton
class APIService {

  def syncContacts() = {
    val e = new integration.ContactAPISync
    e.getContacts
  }

}
