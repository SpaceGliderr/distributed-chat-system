package chat

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import scalafx.beans.property.StringProperty
import model.{User, ChatSession, UserChatSession, Message}
import util.{Database, UserRoles}
import scala.util.{ Success, Failure }
import scala.collection.mutable.{ListBuffer, Set}


object ServerManager {

    sealed trait Command

    case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command
    case class CreateSession(from: ActorRef[ClientManager.Command], creatorId: Long, participants: Array[Long], chatName: String) extends Command
    case class JoinSession(sessionId: Long, from: ActorRef[ClientManager.Command]) extends Command
    case class LeaveSession(from: ActorRef[ClientManager.Command], userId: Long, sessionId: Long) extends Command
    case class DeleteSession(userId: Long, sessionId: Long) extends Command
    case class SendMessage(sessionId: Long, message: String, senderId: Long) extends Command
    case class DeleteMessage(messageId: Long) extends Command
    case class CreateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
    case class AuthenticateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
    case class GetChatSession(from: ActorRef[ClientManager.Command], userId: Long) extends Command
    case class GetAllUsers(from: ActorRef[ClientManager.Command], user: User) extends Command
    case class GetSessionMessages(from: ActorRef[ClientManager.Command], sessionId: Long) extends Command
    case class GetChatInfo(from: ActorRef[ClientManager.Command], chatSession: ChatSession) extends Command


    // var chatSessionMap: Map[Long, ActorRef[ChatRoom.Command]] = Map()
    var chatSessionMap: Map[Long, Set[ActorRef[ClientManager.Command]]] = Map()
    var userMap: Map[Long, ActorRef[ClientManager.Command]] = Map()

    // Server key
    val ServerKey: ServiceKey[ServerManager.Command] = ServiceKey("Server")

    def apply(): Behavior[ServerManager.Command] =
        Behaviors.setup { context =>
            context.system.receptionist ! Receptionist.Register(ServerKey, context.self)

            Behaviors.receiveMessage { message =>
                message match {
                    case Message(value, from) =>
                        println(s"Server received message '${value}'")

                        Behaviors.same

                    case CreateSession(from, creatorId, participants, chatName) =>
                        println(s"Server received request to create session")

                        // Create a new chat session
                        var chatSession: ChatSession = null
                        if (participants.length == 1)
                            chatSession = new ChatSession(chatName, User.findOne(creatorId).get.username, creatorId)
                        else
                            chatSession = new ChatSession(chatName, "group chat", creatorId)

                        chatSession.create() // save into database
                        context.self ! GetChatSession(from, creatorId) // update the all chat sessions to client

                        // Create UserChatSession for all participants
                        participants.foreach( participant => {
                            var userChatSession = new UserChatSession(participant, chatSession.id)
                            userChatSession.upsert()

                            userMap.get(participant).foreach(user => {
                                context.self ! GetChatSession(user,participant)
                                context.self ! GetAllUsers(user, User.findOne(creatorId).get)
                            })
                        })

                        // Update the created chat to client
                        from ! ClientManager.UpdateSelectedChat(chatSession, UserChatSession.getUsersInChatSession(chatSession.id))
                        // Join the creator into the the created chat
                        from ! ClientManager.JoinSession(chatSession.id)

                        // Add the created chat session into the map
                        chatSessionMap += (chatSession.id -> Set(from))

                        Behaviors.same

                    case JoinSession(sessionId, from) =>
                        println(s"Server received request to join session ${sessionId}")

                        // Join the users/actors into the chat session
                        chatSessionMap.get(sessionId) match {
                            case Some(participants) =>
                                participants += from
                                chatSessionMap += (sessionId -> participants)

                            case None =>
                                chatSessionMap += (sessionId -> Set(from))
                        }

                        Behaviors.same

                    case LeaveSession(from, userId, sessionId) =>
                        println(s"Server received request to leave session ${sessionId}")

                        chatSessionMap.get(sessionId).foreach(participants => {
                            participants -= from
                            chatSessionMap += (sessionId -> participants)
                        })

                        Behaviors.same

                    case DeleteSession(userId, sessionId) =>
                        println(s"Server received request to delete session ${sessionId} for user ${userId}")

                        UserChatSession.leaveSession(userId, sessionId)

                        Behaviors.same

                    case GetSessionMessages(from, sessionId) =>
                        println(s"Server received request to get session messages ${sessionId}")

                        val messages = ChatSession.getMessages(sessionId)
                        var messageMap: Map[Long, String] = Map()

                        messages.foreach(m => messageMap += (m.id -> m.toString()))
                        from ! ClientManager.UpdateSessionMessages(messageMap)

                        Behaviors.same

                    case SendMessage(sessionId, message, senderId) =>
                        println(s"Server received message '${message}'")

                        val msg = new model.Message(message, senderId, sessionId)
                        msg.upsert()

                        chatSessionMap.get(sessionId).foreach(participants => {
                            participants.foreach(participant =>
                                participant ! ClientManager.Message(msg.id, msg.toString())
                            )
                        })

                        Behaviors.same

                    case DeleteMessage(messageId) =>
                        println(s"Server received delete message '${messageId}'")

                        model.Message.deleteMessage(messageId)

                        Behaviors.same

                    case CreateUser(from, username, password) =>
                        println(s"Server received a request to create user")

                        val user = new User(username, password)
                        user.create() match {
                            case Success(value) =>
                                from ! ClientManager.UpdateSignUpRequest(true, "Successfully Sign Up!")
                                userMap.foreach{
                                    case (u, ref) =>
                                        context.self ! GetAllUsers(ref, User.findOne(u).get)
                                }

                            case Failure(exception) =>
                                from ! ClientManager.UpdateSignUpRequest(false, "Failed to Sign Up!")
                        }

                        Behaviors.same

                    case AuthenticateUser(from, username, password) =>
                        println(s"Server received request to authenticate user")

                        User.login(username, password) match {
                            case Some(user) =>
                                from ! ClientManager.Authenticate(true, "Successfully Logged In!")
                                from ! ClientManager.RequestUpdatedUser(user)
                                userMap += (user.id -> from)

                            case None =>
                                from ! ClientManager.Authenticate(false, "INVALID USERNAME/PASSWORD")
                        }

                        Behaviors.same

                    case GetChatSession(from, userId) =>
                        println(s"Server received request to retrieve all chat sessions for user ${userId}")

                        val sessions = UserChatSession.getChatSessions(userId)
                        from ! ClientManager.UpdateChatSessions(sessions.distinct)

                        Behaviors.same

                    case GetAllUsers(from, user) =>
                        var allUsers = User.selectAll.filter( _ != user)
                        var availableUsers = User.selectAll.filter( _ != user)

                        val sessions = ChatSession.selectAll
                        sessions.foreach( s => {
                            availableUsers.foreach( u => {
                                val allUsers = UserChatSession.getUsersInChatSession(s.id)
                                if (allUsers.length == 2) {
                                    var ids: List[Long] = List()
                                    allUsers.foreach( x => ids :+= x.id )
                                    if ((ids.contains(user.id)) && (ids.contains(u.id)))
                                        availableUsers = availableUsers.filter(_ != u)
                                }
                            })
                        })
                        from ! ClientManager.UpdateUsers(allUsers, availableUsers)

                        Behaviors.same

                    case GetChatInfo(from, chatSession) =>
                        val users = UserChatSession.getUsersInChatSession(chatSession.id)

                        from ! ClientManager.UpdateSelectedChat(chatSession, users)

                        Behaviors.same
                }
            }
        }
}

object Server extends App {
    Database.setupDB()
    val greeterMain: ActorSystem[ServerManager.Command] = ActorSystem(ServerManager(), "HelloSystem")
}