import java.io.File
import java.security.PrivateKey
import java.util.Base64
import spray.http.{MediaTypes, BodyPart, MultipartFormData}
import akka.actor.ActorSystem
import akka.io.IO
import spray.client.pipelining._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Future, Await}
import spray.can.Http
import spray.http._
import spray.json._
import scala.util.{Failure, Success}

case class AddFriend(myID : Identifier, friendID : Identifier)

object Network {
  implicit val system = ActorSystem("network-system")
  var Hostname = "localhost"
  var HostPort = 8080
  var HostSchema = "http://"
  def HostURI = HostSchema + Hostname + ":" + HostPort

  def send(uri : String) = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher
    // execution context for futures

    val pipeline: Future[SendReceive] =
      for (
        Http.HostConnectorInfo(connector, _) <-
        IO(Http) ? Http.HostConnectorSetup(Network.Hostname, port = Network.HostPort)
      ) yield sendReceive(connector)

    val request = Get(uri)
    val response: Future[HttpResponse] = pipeline.flatMap(_(request))
    response onComplete {
      case Success(r) => println(r.entity.asString)
      case Failure(e) => e
    }
  }

  def post(uri : String) = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher
    // execution context for futures

    val pipeline: Future[SendReceive] =
      for (
        Http.HostConnectorInfo(connector, _) <-
        IO(Http) ? Http.HostConnectorSetup(Network.Hostname, port = Network.HostPort)
      ) yield sendReceive(connector)

    val request = Post(uri)
    val response: Future[HttpResponse] = pipeline.flatMap(_(request))
    response onComplete {
      case Success(r) => ; //println(r.entity.asString)
      case Failure(e) => e
    }
  }

  def addPost(uri : String, content : String, key : String, nonce : String, digitalSig : String) : Future[KeyedEnt] = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val pipeline: HttpRequest => Future[KeyedEnt] = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        ~> unmarshal[KeyedEnt]
      )

    val response: Future[KeyedEnt] =
      pipeline(Post(uri, PostCreateForm(content, KeyMaterial(key, nonce, digitalSig))))

    response
  }

  def getPost(uri : String, private_key : PrivateKey, public_key : String) = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val pipeline: HttpRequest => Future[KeyedEnt] = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        ~> unmarshal[KeyedEnt]
      )

    val response: Future[KeyedEnt] =
      pipeline(Get(uri))

    response onComplete{
      case Success(KeyedEnt(fbent, key)) =>
        val aes = new AEShelper()
        val rsa = new RSAhelper()

        val ent : PostEnt = fbent.asInstanceOf[PostEnt]

        //println("The JSON: " + key.toJson.prettyPrint)

        val decMsg = aes.decryptMessage(ent.content, private_key, key.key, key.nonce)

        val pub_key = rsa.getPublicKey(public_key)
        val sig = Base64.getDecoder.decode(key.sig)
        val verify = rsa.verifySignature(pub_key, sig, ent.content)

        if (verify) {
          println("User " + ent.owner + " to " + ent.target + ": " + decMsg)
          //println("**************** Decrypted Message: " + decMsg + " from user " + ent.owner + " to " + ent.target)
        }
        else {
          println("Failed to verify digital signature")
        }

      case Failure(e) =>
        println("Failed to get post: " + e.getMessage)
    }
  }

  def addKey(uid : Identifier, obj : Identifier, friend : Identifier, mat : KeyMaterial) = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val uri = Network.HostURI + "/key/" + uid + "/add/" + obj + "/" + friend

    val pipeline  = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        //~> unmarshal[KeyedEnt]
      )

    val response =
      pipeline(Post(uri, mat))

    response
  }

  def addAlbum(uri : String, name : String, description : String) : Future[AlbumEnt] = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val pipeline: HttpRequest => Future[AlbumEnt] = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        ~> unmarshal[AlbumEnt]
      )

    val response: Future[AlbumEnt] =
      pipeline(Post(uri, AlbumCreateForm(name, description)))

    response
  }

  def addPicture(uri : String, caption : String, fileId : Identifier) : Future[PictureEnt] = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val pipeline: HttpRequest => Future[PictureEnt] = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        ~> unmarshal[PictureEnt]
      )

    val response: Future[PictureEnt] =
      pipeline(Post(uri, PictureCreateForm(caption, fileId)))

    response
  }

  def getAlbumId(uri : String) : Future[PictureEnt] = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures
    import FacebookJsonSupport._

    val pipeline: HttpRequest => Future[PictureEnt] = (
      addHeader("X-My-Special-Header", "fancy-value")
        ~> sendReceive
        ~> unmarshal[PictureEnt]
      )

    val response: Future[PictureEnt] =
      pipeline(Get(uri))

    response onComplete {
      case Success(r) => println(r.albumId)
      case Failure(e) => e
    }

    response
  }

  def uploadFile() = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher // execution context for futures below

    val pipeline = sendReceive
    val payload = MultipartFormData(Seq(BodyPart(new File("Pictures/icon.png"), "datafile", MediaTypes.`application/base64`)))
    val request =
      Post(Network.HostURI + "/image", payload)

    pipeline(request).onComplete { res =>
      println(res)
      system.shutdown()
    }
  }

}
