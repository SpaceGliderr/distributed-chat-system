package chat

import akka.actor.typed.ActorRef
// import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
// import com.typesafe.config.ConfigFactory
import scalafx.beans.property.StringProperty
// import ClientManager.Command
import chat.model.{User, ChatSession}
import util.Database
import scalafx.collections.ObservableBuffer
import scala.collection.mutable.ListBuffer


// Documentation regarding the Actor Receptionist, Listing, etc.
// https://alvinalexander.com/scala/akka-typed-how-lookup-find-actor-receptionist/
object ClientManager {
    sealed trait Command
    final case object FindServer extends Command
    private case class ListingResponse(listing: Receptionist.Listing) extends Command
    case class Message(message: String) extends Command
    case class SignUp(username: String, password: String) extends Command
    case class LogIn(username: String, password: String) extends Command
    case class Authenticate(value: Boolean, message: String) extends Command
    case class SignUpRequest(value: Boolean, message: String) extends Command
    case class CreateSession(participants: Array[Long], chatName: String) extends Command
    case class JoinSession(sessionId: Long) extends Command
    case class LeaveSession(sessionId: Long) extends Command
    case class DeleteSession(sessionId: Long) extends Command
    case class SendMessage(message: String) extends Command
    case class UpdateUser(user: User) extends Command
    case class ChatSessions(sessions: List[ChatSession]) extends Command
    case class AllUsers(allUsers: List[User], pmUsers: List[User]) extends Command
    case class SelectedChat(chatSession: ChatSession, users: List[User]) extends Command
    case class UpdateChatInfo(chatSession: ChatSession) extends Command
    case class GetSessionMessages(message: ListBuffer[String]) extends Command

    // case class User(id: String, username: String, password: String) extends Command

    var user: User = null
    var users: ObservableBuffer[User] = new ObservableBuffer[User]()
    var pmUsers: ObservableBuffer[User] = new ObservableBuffer[User]()
    var chatSessions = new ObservableBuffer[ChatSession]()
    var selectedChatRoom: ChatSession = null
    var usersInChatRoom: Set[User] = Set.empty[User]
    //var authenticate: Boolean = false
    //var signup: Boolean = false
    //var authenticate = ObjectProperty[Boolean](false)
    var authenticate =  new StringProperty("")
    var signup = new StringProperty("")
    var sessionMessages = new ObservableBuffer[String]()

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
                        sessionMessages += message
                        Behaviors.same

                    case SignUp(username, password) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.CreateUser(context.self, username, password)
                        }
                        Behaviors.same

                    case SignUpRequest(value, message) =>
                        this.signup.value = value.toString 
                        println(signup.getValue())
                        println(message)
                        Behaviors.same

                    case LogIn(username, password) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.AuthenticateUser(context.self, username, password)
                        }
                        Behaviors.same

                    case Authenticate(value, message) =>
                        this.authenticate.value = value.toString
                        println(authenticate.getValue())
                        println(message)
                        Behaviors.same

                    case CreateSession(participants, chatName) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.CreateSession(context.self, user.id, participants, chatName)
                        }
                        Behaviors.same

                    case JoinSession(sessionId) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.GetSessionMessages(context.self, sessionId)
                            remote ! ServerManager.JoinSession(sessionId , Array(user.id))
                        }
                        Behaviors.same

                    case LeaveSession(sessionId) =>
                        sessionMessages.clear()
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.LeaveSession(context.self, this.user.id, sessionId)
                        }
                        Behaviors.same

                    case DeleteSession(sessionId) =>
                        context.self ! LeaveSession(sessionId)
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.DeleteSession(this.user.id, sessionId)
                        }
                        this.chatSessions = this.chatSessions.filter(_.id != sessionId)
                        Behaviors.same


                    case GetSessionMessages(messages) =>{
                        messages.foreach(m => this.sessionMessages += m)
                        println(s"sessionMessage received from ${context.self.path.name}: ${this.sessionMessages}")
                        Behaviors.same
                    }

                    case SendMessage(message) =>
                        println(s"Current User >>> ${user}")
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.SendMessage(selectedChatRoom.id, message, user.id)
                        }
                        Behaviors.same

                    case UpdateUser(u: User) =>
                        user = u
                        println(s"Current User >>> ${user}")
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.GetChatSession(context.self, this.user.id)
                            remote ! ServerManager.GetAllUsers(context.self, user)
                        }
                        Behaviors.same

                    case ChatSessions(sessions) =>
                        chatSessions.clear()
                        sessions.foreach(s => chatSessions += s)
                        println(s"ChatSessions received from ${context.self.path.name}: ${chatSessions}")
                        Behaviors.same

                    case AllUsers(allUsers: List[User], pmUsers: List[User]) =>
                        this.users ++= allUsers
                        this.pmUsers ++= pmUsers
                        this.users = this.users.distinct
                        this.pmUsers = this.pmUsers.distinct

                        println(s"All Users in the system ${context.self.path.name}: ${this.users}")
                        println(s"PM User in the system ${context.self.path.name}: ${this.pmUsers}")
                        Behaviors.same

                    case UpdateChatInfo(chatSession) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.UpdateChatInfo(context.self, chatSession)
                        }
                        Behaviors.same

                    case SelectedChat(chatSession, users) =>
                        this.selectedChatRoom = chatSession
                        this.usersInChatRoom = users.toSet
                        println(s"Selected > ${this.selectedChatRoom}")
                        println(s"In sessions: ${this.usersInChatRoom}")
                        Behaviors.same
                }
            }
        }
}

// object NewClient extends App {

//     // When a main client is spawned, it will (1) Create the ActorRef for the client and (2) Trigger Client.start
//     // val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem")
//     val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem", ConfigFactory.load("client"))
//     // println("Client started")
//     // var text = scala.io.StdIn.readLine("command=")
//     // while (text != "end"){
//     //     greeterMain ! ClientManager.Start
//     //     println("Variable text ", text)
//     //     text = scala.io.StdIn.readLine("command=")
//     // }

//     val entry = scala.io.StdIn.readLine("Login or Signup")
//     val username = scala.io.StdIn.readLine("Enter Username: ")
//     val password = scala.io.StdIn.readLine("Enter Password: ")

//     greeterMain ! ClientManager.FindServer

//     // ! DELETE this part when linking front end and back end
//     entry match {
//         case "login" => greeterMain ! ClientManager.LogIn(username, password)
//         case "signup" => greeterMain ! ClientManager.SignUp(username, password)
//     }

//     var sessionId: String = null
//     val session = scala.io.StdIn.readLine("Create or Join")
//     session match {
//         case "create" =>
//         greeterMain ! ClientManager.CreateSession()
//         sessionId = scala.io.StdIn.readLine("sessionId=")

//         case "join" =>
//         sessionId = scala.io.StdIn.readLine("sessionId=")
//         greeterMain ! ClientManager.JoinSession(sessionId.toLong)
//     }

//     var message = scala.io.StdIn.readLine("message=")

//     while (message != "end"){
//         greeterMain ! ClientManager.SendMessage(sessionId.toLong, message)
//         message = scala.io.StdIn.readLine("message=")
//     }

//     greeterMain.terminate
// }