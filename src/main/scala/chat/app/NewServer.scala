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
import scala.collection.mutable.ListBuffer

object ServerManager {
    sealed trait Command
    case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command
    case class CreateSession(from: ActorRef[ClientManager.Command], creatorId: Long, participants: Array[Long], chatName: String) extends Command
    case class JoinSession(sessionId: Long, participants: Array[Long]) extends Command
    case class LeaveSession(from: ActorRef[ClientManager.Command], userId: Long, sessionId: Long) extends Command
    case class DeleteSession(userId: Long, sessionId: Long) extends Command
    case class SendMessage(sessionId: Long, message: String, senderId: Long) extends Command
    case class CreateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
    case class AuthenticateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
    case class GetChatSession(from: ActorRef[ClientManager.Command], userId: Long) extends Command
    case class GetAllUsers(from: ActorRef[ClientManager.Command], user: User) extends Command
    case class GetSessionMessages(from: ActorRef[ClientManager.Command], sessionId: Long) extends Command
    case class PopulateChatSessionMap() extends Command
    case class UpdateChatInfo(from: ActorRef[ClientManager.Command], chatSession: ChatSession) extends Command
    // case object TestCreateSession extends Command
    // case class TestJoinSession(sessionId: String) extends Command
    // case class TestSendMessage(sessionId: String, message: String) extends Command

    var chatSessionMap: Map[Long, ActorRef[ChatRoom.Command]] = Map()
    var userMap: Map[Long, ActorRef[ClientManager.Command]] = Map()

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

                        var chatSession: ChatSession = null

                        if (participants.length == 1)
                            chatSession = new ChatSession(chatName, User.findOne(creatorId).get.username, creatorId)
                        else
                            chatSession = new ChatSession(chatName, "group chat", creatorId)

                        chatSession.create()
                        context.self ! GetChatSession(from, creatorId)

                        var userChatSession: UserChatSession = null

                        participants.foreach( participant => {
                            userChatSession = new UserChatSession(participant, chatSession.id)
                            userChatSession.upsert()
                            //Update new chat session
                            userMap.get(participant).foreach(user => context.self ! GetChatSession(user,participant))
                        })

                        from ! ClientManager.SelectedChat(chatSession, UserChatSession.getUsersInChatSession(chatSession.id))
                        from ! ClientManager.JoinSession(chatSession.id)

                         // Spawn Chat Room actor
                        val chatRoom = context.spawn(ChatRoom(), chatSession.id.toString)

                        // Add chat room to chat session map
                        chatSessionMap += (chatSession.id -> chatRoom)
                        println("CHAT SESSION MAP >>>>> ", chatSessionMap)


                        // for ((id, actorref) <- userMap) {
                        //     context.self ! GetChatSession(actorref,id)
                        // }

                        Behaviors.same

                    case JoinSession(sessionId, participants) =>
                        println("Server received request to join session")

                        println(s"Session ID: ${sessionId}")
                        println(chatSessionMap)

                        var p = Array[ActorRef[ClientManager.Command]]()

                        participants.foreach(participant => {
                            userMap.get(participant).foreach(user => p = p :+ user)
                        })

                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Subscribe(p)
                            //room ! ChatRoom.Publish("New people just joined!!!")
                        })

                        Behaviors.same

                    case LeaveSession(from, userId, sessionId) =>
                        println("Server received request to leave session")
                        println(s"Session ID: ${sessionId}")

                        // UserChatSession.leaveSession(userId, sessionId)

                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Unsubscribe(from)
                        })

                        Behaviors.same

                    case DeleteSession(userId, sessionId) =>
                        UserChatSession.leaveSession(userId, sessionId)
                        Behaviors.same


                    case GetSessionMessages(from, sessionId) =>
                        println("Session ID:" + sessionId)
                        val messages = ChatSession.getMessages(sessionId)
                        val messageString = new ListBuffer[String]()
                        messages.foreach(m => messageString += m.toString())
                        println(messageString)
                        from ! ClientManager.GetSessionMessages(messageString)
                        Behaviors.same

                    case SendMessage(sessionId, message, senderId) =>
                        println(s"Server received message '${message}'")
                        val msg = new model.Message(message, senderId, sessionId)
                        msg.upsert()
                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Publish(msg.toString())
                        })

                        Behaviors.same

                    case CreateUser(from, username, password) =>
                        println(s"Server received a request to create user")

                        val user = new User(username, password)
                        user.create() match {
                            case Success(value) =>
                                from ! ClientManager.SignUpRequest(true, "Successfully Sign Up!")
                                userMap += (user.id -> from)
                                println(s"USER MAP >> ${userMap}")

                                userMap.foreach{
                                    case (u, ref) =>
                                        context.self ! GetAllUsers(ref, User.findOne(u).get)
                                }

                            case Failure(exception) =>
                                from ! ClientManager.SignUpRequest(false, "Failed to Sign Up!")
                        }

                        Behaviors.same

                    case AuthenticateUser(from, username, password) =>
                        println(s"Server received request to authenticate user")
                        User.login(username, password) match {
                            case Some(user) =>
                                from ! ClientManager.Authenticate(true, "Successfully Logged In!")
                                from ! ClientManager.UpdateUser(user)
                                userMap += (user.id -> from)

                            case None =>
                                from ! ClientManager.Authenticate(false, "INVALID USERNAME/PASSWORD")
                        }
                        println(User.selectAll)
                        Behaviors.same

                    case GetChatSession(from, userId) =>
                        val sessions = UserChatSession.getChatSessions(userId)
                        from ! ClientManager.ChatSessions(sessions.distinct)
                        Behaviors.same

                    case GetAllUsers(from, user) =>
                        var allUsers = User.selectAll.filter( _ != user)
                        var availableUsers = User.selectAll.filter( _ != user)

                        val sessions = ChatSession.selectAll
                        sessions.foreach( s => {
                            availableUsers.foreach( u => {
                                if (UserChatSession.getPMChatSessions(u.id, user.id, s.id).length == 2)
                                    availableUsers = availableUsers.filter(_ != u)
                            })
                        })

                        from ! ClientManager.AllUsers(allUsers, availableUsers)
                        Behaviors.same

                    case PopulateChatSessionMap()=>
                        ChatSession.selectAll.foreach(chatSession => {
                            val chatRoom = context.spawn(ChatRoom(), chatSession.id.toString)
                            chatSessionMap += (chatSession.id -> chatRoom)
                        })
                        Behaviors.same

                    case UpdateChatInfo(from, chatSession) =>
                        val users = UserChatSession.getUsersInChatSession(chatSession.id)
                        from ! ClientManager.SelectedChat(chatSession, users)
                        Behaviors.same
                }
            }
        }
}

object NewServer extends App {
    Database.setupDB()

    val greeterMain: ActorSystem[ServerManager.Command] = ActorSystem(ServerManager(), "HelloSystem")

    greeterMain ! ServerManager.PopulateChatSessionMap()

    var msg = scala.io.StdIn.readLine("see database")

    while (msg != "end"){
        var a = User.selectAll
        var b = Message.selectAll
        var c = UserChatSession.selectAll
        var d = ChatSession.selectAll
        println("USER ------>")
        a.foreach(x => println(x))

        println("\nCHAT SESSION ------>")
        d.foreach(x => println(x))

        println("\nUSER CHAT SESSION ------>")
        c.foreach(x => println(x))

        println("\nMESSAGE ------>")
        b.foreach(x => println(x))

        msg = scala.io.StdIn.readLine("see database")
    }



    // greeterMain ! ServerManager.TestCreateSession

    // var sessionId = scala.io.StdIn.readLine("command=")
    // greeterMain ! ServerManager.TestJoinSession(sessionId)

    // var message = scala.io.StdIn.readLine("command=")
    // greeterMain ! ServerManager.TestSendMessage(sessionId, message)

    // greeterMain.terminate
}