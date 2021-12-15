package chat

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import scalafx.beans.property.StringProperty
import chat.model.{User, ChatSession}
import scalafx.collections.ObservableBuffer
import scalafx.collections.ObservableHashMap


// Documentation regarding the Actor Receptionist, Listing, etc.
// https://alvinalexander.com/scala/akka-typed-how-lookup-find-actor-receptionist/
object ClientManager {

    sealed trait Command

    final case object FindServer extends Command
    private case class ListingResponse(listing: Receptionist.Listing) extends Command

    case class Message(messageId: Long, message: String) extends Command
    case class UpdateSignUpRequest(value: Boolean, message: String) extends Command
    case class UpdateChatSessions(sessions: List[ChatSession]) extends Command
    case class UpdateSessionMessages(messageMap: Map[Long,String]) extends Command
    case class UpdateUsers(allUsers: List[User], pmUsers: List[User]) extends Command
    case class UpdateSelectedChat(chatSession: ChatSession, users: List[User]) extends Command
    case class SignUp(username: String, password: String) extends Command
    case class LogIn(username: String, password: String) extends Command
    case class Authenticate(value: Boolean, message: String) extends Command
    case class RequestUpdatedUser(user: User) extends Command
    case class RequestUpdatedChat(chatSession: ChatSession) extends Command
    case class CreateSession(participants: Array[Long], chatName: String) extends Command
    case class JoinSession(sessionId: Long) extends Command
    case class LeaveSession(sessionId: Long) extends Command
    case class DeleteSession(sessionId: Long) extends Command
    case class SendMessage(message: String) extends Command
    case class DeleteMessage(messageId: Long) extends Command

    // Class variables
    var user: User = null
    var users: ObservableBuffer[User] = new ObservableBuffer[User]()
    var pmUsers: ObservableBuffer[User] = new ObservableBuffer[User]()
    var chatSessions = new ObservableBuffer[ChatSession]()
    var selectedChatRoom: ChatSession = null
    var usersInChatRoom: ObservableBuffer[User] = new ObservableBuffer[User]()
    var authenticate =  new StringProperty("")
    var signup = new StringProperty("")
    var sessionMessages = new ObservableHashMap[Long,String]()

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
                        for (x <- xs)
                            remoteOpt = Some(x)

                        Behaviors.same

                    case Message(messageId, message) =>
                        println(s"Message received on Client ${context.self.path.name}: ${message}")

                        sessionMessages += (messageId -> message)

                        Behaviors.same

                    case UpdateSignUpRequest(value, message) =>
                        this.signup.value = value.toString

                        Behaviors.same

                    case UpdateChatSessions(sessions) =>
                        chatSessions.clear()
                        sessions.foreach(s => chatSessions += s)

                        Behaviors.same

                    case UpdateSessionMessages(messageMap) =>
                        sessionMessages.clear()

                        for ((messageId, message) <- messageMap)
                           sessionMessages += (messageId -> message)

                        println(s"sessionMessage received from ${context.self.path.name}: ${this.sessionMessages}")

                        Behaviors.same

                    case UpdateUsers(allUsers: List[User], pmUsers: List[User]) =>
                        this.users ++= allUsers
                        this.pmUsers ++= pmUsers
                        this.users = this.users.distinct
                        this.pmUsers = this.pmUsers.distinct

                        Behaviors.same

                    case UpdateSelectedChat(chatSession, users) =>
                        this.selectedChatRoom = chatSession
                        this.usersInChatRoom.clear()
                        users.foreach(u => this.usersInChatRoom += u)

                        Behaviors.same

                    case SignUp(username, password) =>
                        for (remote <- remoteOpt)
                            remote ! ServerManager.CreateUser(context.self, username, password)

                        Behaviors.same

                    case LogIn(username, password) =>
                        for (remote <- remoteOpt)
                            remote ! ServerManager.AuthenticateUser(context.self, username, password)

                        Behaviors.same

                    case Authenticate(value, message) =>
                        this.authenticate.value = value.toString
                        println(authenticate.getValue())
                        println(message)

                        Behaviors.same

                    case RequestUpdatedUser(u: User) =>
                        this.user = u

                        for (remote <- remoteOpt) {
                            remote ! ServerManager.GetChatSession(context.self, this.user.id)
                            remote ! ServerManager.GetAllUsers(context.self, this.user)
                        }

                        Behaviors.same

                    case RequestUpdatedChat(chatSession) =>
                        for (remote <- remoteOpt)
                            remote ! ServerManager.GetChatInfo(context.self, chatSession)

                        Behaviors.same

                    case CreateSession(participants, chatName) =>
                        for (remote <- remoteOpt)
                            remote ! ServerManager.CreateSession(context.self, user.id, participants, chatName)

                        Behaviors.same

                    case JoinSession(sessionId) =>
                        for (remote <- remoteOpt) {
                            remote ! ServerManager.GetSessionMessages(context.self, sessionId)
                            remote ! ServerManager.JoinSession(sessionId , context.self)
                        }

                        Behaviors.same

                    case LeaveSession(sessionId) =>
                        sessionMessages.clear()

                        for (remote <- remoteOpt)
                            remote ! ServerManager.LeaveSession(context.self, this.user.id, sessionId)

                        Behaviors.same

                    case DeleteSession(sessionId) =>
                        context.self ! LeaveSession(sessionId)

                        for (remote <- remoteOpt)
                            remote ! ServerManager.DeleteSession(this.user.id, sessionId)

                        context.self ! RequestUpdatedUser(user)

                        Behaviors.same

                    case SendMessage(message) =>
                        for (remote <- remoteOpt)
                            remote ! ServerManager.SendMessage(selectedChatRoom.id, message, user.id)

                        Behaviors.same

                    case DeleteMessage(messageId) =>
                        sessionMessages.remove(messageId)

                        for (remote <- remoteOpt)
                            remote ! ServerManager.DeleteMessage(messageId)

                        Behaviors.same
                }
            }
        }
}