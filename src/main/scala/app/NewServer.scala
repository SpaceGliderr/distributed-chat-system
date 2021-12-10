import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import scalafx.beans.property.StringProperty
import java.util.UUID.randomUUID
import model.{User}
import util.Database

object ServerManager {
    sealed trait Command
    case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command
    case class CreateSession(participants: Array[String]) extends Command
    case class JoinSession(sessionId: String, participants: Array[String]) extends Command
    case class SendMessage(sessionId: String, message: String) extends Command
    case class CreateUser(from: ActorRef[ClientManager.Command], user: User) extends Command
    // case object TestCreateSession extends Command
    // case class TestJoinSession(sessionId: String) extends Command
    // case class TestSendMessage(sessionId: String, message: String) extends Command

    var chatSessionMap: Map[String, ActorRef[ChatRoom.Command]] = Map()
    var userMap: Map[String, ActorRef[ClientManager.Command]] = Map()

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
                        val chatId = randomUUID.toString
                        val chatRoom = context.spawn(ChatRoom(), chatId)

                        // Add chat room to chat session map
                        chatSessionMap += (chatId -> chatRoom)

                        println("CHAT SESSION MAP >>>>> ", chatSessionMap)

                        var p = Array[ActorRef[ClientManager.Command]]()

                        participants.foreach { participant =>
                            userMap.get(participant).foreach(user => p = p :+ user)
                        }

                        chatRoom ! ChatRoom.Subscribe(p)

                        chatRoom ! ChatRoom.Publish("Welcome to Hello System!")

                        Behaviors.same
                    case JoinSession(sessionId, participants) =>
                        println("Server received request to join session")

                        println(s"Session ID: ${sessionId}")
                        println(chatSessionMap)

                        var p = Array[ActorRef[ClientManager.Command]]()

                        participants.foreach { participant =>
                            userMap.get(participant).foreach(user => p = p :+ user)
                        }

                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Subscribe(p)
                            room ! ChatRoom.Publish("New people just joined!!!")
                        })

                        Behaviors.same
                    case SendMessage(sessionId, message) =>
                        println("send msg")
                        println(s"Server received message '${message}'")

                        chatSessionMap.get(sessionId).foreach(room => {
                            room ! ChatRoom.Publish(message)
                        })

                        Behaviors.same
                    case CreateUser(from, user) =>
                        println(s"Server received request to create user")

                        // Add User to userMap
                        userMap += (user.uuid -> from)

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

    // greeterMain ! ServerManager.TestCreateSession

    // var sessionId = scala.io.StdIn.readLine("command=")
    // greeterMain ! ServerManager.TestJoinSession(sessionId)

    // var message = scala.io.StdIn.readLine("command=")
    // greeterMain ! ServerManager.TestSendMessage(sessionId, message)

    // greeterMain.terminate
}