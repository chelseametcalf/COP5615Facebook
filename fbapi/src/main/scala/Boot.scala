import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem("fbapi")
    val service = system.actorOf(Props[API], "api-service")

    // IO requires an implicit ActorSystem, and ? requires an implicit timeout
    // Bind HTTP to the specified service.
    implicit val timeout = Timeout(5.seconds)
    IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
  }
}