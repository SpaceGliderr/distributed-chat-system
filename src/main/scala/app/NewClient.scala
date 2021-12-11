import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import com.typesafe.config.ConfigFactory
import scalafx.beans.property.StringProperty
// import ClientManager.Command
import model.{User}
import util.Database
import java.util.UUID.randomUUID

// Documentation regarding the Actor Receptionist, Listing, etc.
// https://alvinalexander.com/scala/akka-typed-how-lookup-find-actor-receptionist/
object ClientManager {
    sealed trait Command
    final case object FindServer extends Command
    private case class ListingResponse(listing: Receptionist.Listing) extends Command
    // case class Start(username: String, password: String) extends Command
    case class Message(message: String) extends Command
    case class SignUp(username: String, password: String) extends Command
    case class LogIn(username: String, password: String) extends Command
    case class CreateSession(participants: Array[String]) extends Command
    case class SendMessage(sessionId: String, message: String) extends Command
    // case class User(id: String, username: String, password: String) extends Command

    var user: User = null

    def apply(): Behavior[ClientManager.Command] =
        Behaviors.setup { context =>
            // The program can't initially get a reference to the Server actor
            // Therefore, this variable is a var and uses Option / None
            var remoteOpt: Option[ActorRef[ServerManager.Command]] = None

            // Creates an ActorRef of Receptionist.Listing "adapter"
            // - Adapter is a wrapper of a class that allows another class to interact with it
            val listingAdapter: ActorRef[Receptionist.Listing] =
                context.messageAdapter {
                    listing => ClientManager.ListingResponse(listing)
                    // This code tells the Receptionist how to get back in touch after the message
                }

            // Subscribe to events related to the Server actor
            context.system.receptionist ! Receptionist.Subscribe(ServerManager.ServerKey, listingAdapter)
            Behaviors.receiveMessage { message =>
                message match {
                    case FindServer =>
                        // Send a message to the Receptionist to find any/all listings with ServerKey
                        context.system.receptionist ! Receptionist.Find(ServerManager.ServerKey, listingAdapter)
                        Behaviors.same

                    case ListingResponse(ServerManager.ServerKey.Listing(listings)) =>
                        // Receptionist sends a ListingResponse message
                        // `listings` variable is a set of ActorRefs of type Server.Command
                        val xs: Set[ActorRef[ServerManager.Command]] = listings
                        for (x <- xs) {
                            remoteOpt = Some(x)
                        }
                        Behaviors.same

                    case Message(message) =>
                        println(s"Message received on Client ${context.self.path.name}: ${message}")
                        Behaviors.same

                    case SignUp(username, password) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.CreateUser(context.self, username, password)
                        }
                        Behaviors.same

                    case LogIn(username, password) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.AuthenticateUser(context.self, username, password)
                        }
                        Behaviors.same

                    case CreateSession(participants: Array[String]) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.CreateSession(participants)
                        }
                        Behaviors.same

                    case SendMessage(sessionId, message) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.SendMessage(sessionId, message)
                        }
                        Behaviors.same
                }
            }
        }
}

object NewClient extends App {

    // When a main client is spawned, it will (1) Create the ActorRef for the client and (2) Trigger Client.start
    // val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem")
    val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem", ConfigFactory.load("client"))
    // println("Client started")
    // var text = scala.io.StdIn.readLine("command=")
    // while (text != "end"){
    //     greeterMain ! ClientManager.Start
    //     println("Variable text ", text)
    //     text = scala.io.StdIn.readLine("command=")
    // }
    val status = scala.io.StdIn.readLine("Login or Signup")
    val username = scala.io.StdIn.readLine("Enter Username: ")
    val password = scala.io.StdIn.readLine("Enter Password: ")

    greeterMain ! ClientManager.FindServer

    // ! used when only without frontend
    status match {
        case "login" => greeterMain ! ClientManager.LogIn(username, password)
        case "signup" => greeterMain ! ClientManager.SignUp(username, password)
    }
    // var user: User = null
    // val user = ClientManager.User(randomUUID.toString, username, password)
    // val user = new User(randomUUID.toString, username, password)

    // greeterMain ! ClientManager.Start(username, password)

    // greeterMain ! ClientManager.CreateSession(Array(user.uuid))

    // val sessionId = scala.io.StdIn.readLine("sessionId=")
    // var message = scala.io.StdIn.readLine("message=")
    // while (message != "end"){
    //     greeterMain ! ClientManager.SendMessage(sessionId, message)
    //     message = scala.io.StdIn.readLine("message=")
    // }

    // greeterMain.terminate
}