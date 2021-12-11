import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import scalafx.beans.property.StringProperty
import model.{User, ChatSession, UserChatSession, Message}
import util.{Database, UserRoles}

object ServerManager {
    sealed trait Command
    case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command
    // ! problem to get creator id
    case class CreateSession(participants: Array[Long]) extends Command
    case class JoinSession(sessionId: Long, participants: Array[Long]) extends Command
    case class SendMessage(sessionId: Long, message: String, senderId: Long) extends Command
    case class CreateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
    case class AuthenticateUser(from: ActorRef[ClientManager.Command], username: String, password: String) extends Command
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

                    case CreateSession(participants) =>
                        println(s"Server received request to create session")

                        // Spawn Chat Room actor
                        val chatSession = new ChatSession("something", "something", participants(0))
                        chatSession.create()
                        // UserChatSession will be created in chatSession.create() method

                        val chatRoom = context.spawn(ChatRoom(), chatSession.id.toString)

                        // Add chat room to chat session map
                        chatSessionMap += (chatSession.id -> chatRoom)

                        println("CHAT SESSION MAP >>>>> ", chatSessionMap)

                        var p: Array[ActorRef[ClientManager.Command]] = Array()

                        participants.foreach(participant => {
                            userMap.get(participant).foreach(user =>
                                p = p :+ user)
                        })

                        chatRoom ! ChatRoom.Subscribe(p)
                        chatRoom ! ChatRoom.Publish("Welcome to Hello System!")

                        Behaviors.same

                    case JoinSession(sessionId, participants) =>
                        println("Server received request to join session")

                        println(s"Session ID: ${sessionId}")
                        println(chatSessionMap)

                        val userChatSession = new UserChatSession(participants(0), sessionId)
                        userChatSession.upsert()

                        var p = Array[ActorRef[ClientManager.Command]]()

                        participants.foreach(participant =>
                            userMap.get(participant).foreach(user => p = p :+ user)
                        )

                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Subscribe(p)
                            room ! ChatRoom.Publish("New people just joined!!!")
                        })

                        Behaviors.same

                    case SendMessage(sessionId, message, senderId) =>
                        println(s"Server received message '${message}'")
                        val msg = new model.Message(message, senderId, sessionId)
                        msg.upsert()
                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Publish(message)
                        })

                        Behaviors.same

                    case CreateUser(from, username, password) =>
                        println(s"Server received a request to create user")

                        val user = new User(username, password)
                        user.upsert()

                        from ! ClientManager.UpdateUser(user)
                        println(User.selectAll)

                        // Add User to userMap
                        userMap += (user.id -> from)
                        println(s"USER MAP >> ${userMap}")

                        Behaviors.same

                    case AuthenticateUser(from, username, password) =>
                        println(s"Server received request to authenticate user")
                        User.login(username, password) match {
                            case Some(user) =>
                                from ! ClientManager.Message("Successfully Logged In!")
                                from ! ClientManager.UpdateUser(user)
                                userMap += (user.id -> from)

                            case None =>
                                from ! ClientManager.Message("INVALID USERNAME/PASSWORD")
                        }
                        println(User.selectAll)
                        Behaviors.same


                    // case TestCreateSession =>
                    //     println("Testing Server")

                    //     val clientManager = context.spawn(ClientManager(), randomUUID.toString)
                    //     val clientManager2 = context.spawn(ClientManager(), randomUUID.toString)
                    //     val clientManager3 = context.spawn(ClientManager(), randomUUID.toString)

                    //     val clients = Array(clientManager, clientManager2, clientManager3)

                    //     context.self ! CreateSession(clients)

                    //     Behaviors.same
                    // case TestJoinSession(sessionId) =>
                    //     val clientManager4 = context.spawn(ClientManager(), randomUUID.toString)
                    //     val clientManager5 = context.spawn(ClientManager(), randomUUID.toString)
                    //     val clients2 = Array(clientManager4, clientManager5)

                    //     context.self ! JoinSession(sessionId, clients2)

                    //     Behaviors.same
                    // case TestSendMessage(sessionId, message) =>
                    //     println(s"Server received message '${message}'")

                    //     context.self ! SendMessage(sessionId, message)

                    //     Behaviors.same
                }
            }
        }
}

object NewServer extends App {
    Database.setupDB()

    val greeterMain: ActorSystem[ServerManager.Command] = ActorSystem(ServerManager(), "HelloSystem")

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