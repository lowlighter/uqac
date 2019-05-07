//Imports and packages
package com.exercise2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import scala.util.Properties
import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

//-------------------------------------------------------------------------------------------
//Web services

package object WebServices {

  def start():Unit = {
    //Debug
    println("Starting server...")
    //Setup WebSockets server with akka
    implicit val system = ActorSystem("akka-system")
    implicit val materializer = ActorMaterializer()
    val route = MainService.route ~ SocketService.route

    //Start server
    val port = Properties.envOrElse("PORT", "8080").toInt
    val binding = Http().bindAndHandle(route, "0.0.0.0", port)
    println(s"Server is online on port ${port}")
  }

  //Send message to connected sockets
  def send(message:String):Unit = {
    SocketService.send(message)
  }

}

//Response
case class Response(status:String, results:Array[Json] = Array[Json](), n:Long = 0)

//WebServices
trait WebService { def route: Route }

//Default service
object MainService extends WebService {
  override def route: Route = get {
    (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
      getFromFile("src/resources/www/Exercice2/index.html")
    } ~ {
      getFromDirectory("src/resources/www/Exercice2")
    }
  }
}

//Query service
object SocketService extends WebService {
  override def route: Route = path("ws") {
    get {
      handleWebSocketMessages(service)
    }
  }

  //This function handle Web Sockets messages
  private var connections: List[TextMessage => Unit] = List()
  val service: Flow[Message, Message, NotUsed] = {
    val inbound: Sink[Message, Any] = Sink.foreach(_ => ())
    val outbound: Source[Message, SourceQueueWithComplete[Message]] = Source.queue[Message](16, OverflowStrategy.fail)
    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      connections ::= outboundMat.offer
      NotUsed
    })
  }

  //Send message to connected sockets
  def send(message:String): Unit = {
    for (connection <- connections) connection(TextMessage.Strict(message))
  }

}



