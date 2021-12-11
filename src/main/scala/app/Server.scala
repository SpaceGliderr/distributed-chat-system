// import akka.actor.typed.ActorRef
// import akka.actor.typed.ActorSystem
// import akka.actor.typed.Behavior
// import akka.actor.typed.pubsub.Topic
// import akka.actor.typed.scaladsl.Behaviors
// import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
// import java.util.UUID.randomUUID
// import models.{ User, Session }

// // Concept of Actor Discoverability
// // - Gives centralized solution for retrieving any actor existing in the actor system
// // - As long as service key and actor are registered

// // Concept of Receptionist
// // Receives registration requests from actors
// // Sends actor references to actors that requests an actor reference (looks up table mapping from a ServiceKey)
// // Receptionist is an actor -> Communicate with it using messages (responds in message)
// // - Therefore, requesting actor must be prepared to receive the actor listing (response from Receptionist)

// object ServerManager {
//     // Sealed Traits:
//     // - An alternative to enums
//     // - Sealed traits can only be extended in the same file as its declaration
//     // - Single file extension allows the compiler to know every possible subtype
//     // - This provides exhaustive matching -> Will emit error if the match does not cover all subtype cases
//     // https://stackoverflow.com/questions/11203268/what-is-a-sealed-trait
//     sealed trait Command
//     case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command
//     case class CreateUser(user: User) extends Command
//     case class GetListings() extends Command // TODO: This needs to be triggered first
//     case class CreateChatSession(from: ActorRef[ClientManager.Command], participants: Array[ActorRef[ClientManager.Command]]) extends Command
//     case class SendMessage(from: ActorRef[ClientManager.Command], topicName: String, message: String) extends Command // topicName must be a unique ID
//     case object TestChatSession extends Command
//     case class GetUserChatSessions(from: ActorRef[ClientManager.Command], userId: String) extends Command
//     case class JoinChatSession(from: ActorRef[ClientManager.Command], chatRef: ActorRef[Behavior[TopicImpl.Command[ClientManager.Command]]]) extends Command

//     // ServiceKeys are unique keys to identify an actor
//     // It will be used by the receptionist to look up the specific actor references
//     val ServerKey: ServiceKey[ServerManager.Command] = ServiceKey("Server")

//     var chatSessionMap: Map[String, ActorRef[Behavior[TopicImpl.Command[ClientManager.Command]]]] = Map()
//     var userChatSessionMap: Map[String, Array[ActorRef[Behavior[TopicImpl.Command[ClientManager.Command]]]]] = Map()
    
//     // Behavior of an actor -> Defines how it reacts to a message it receives
//     def apply(): Behavior[ServerManager.Command] =
//         // Setup is a factory function -> Creation of behavior instance is deferred until actor is created
//         // Setup passes ActorContext as param
//         Behaviors.setup { context =>
//             // The ! operator is called a "bang"
//             // Syntax: Actor reference to receive the command ! Command to be sent to the actor
//             // The following message is registering the actor to the systems receptionist
//             // ServerKey -> Unique ID of the actor; context.self -> ActorRef
//             context.system.receptionist ! Receptionist.Register(ServerKey, context.self)

//             // Different Behaviors an actor can respond to a message with
//             Behaviors.receiveMessage { message => 
//                 // TODO: Make this into anonymous function
//                 message match {
//                     // TODO: Behaviors in this function
//                     case Message(value, from) =>
//                         println(s"Server received message '${value}'")
//                         // from ! ClientManager.Message("how are you", context.self)
//                         Behaviors.same
//                     case CreateUser(value) =>
//                         println("Server received create user")
//                         Behaviors.same
//                     case CreateChatSession(from, participants) =>
//                         println("Server received create chat session")

//                         // Create a new chat session
//                         val chatId = randomUUID.toString
//                         // Spawn Chat Session actor
//                         // It is recommended to not spawn multiple topics with different types and the same topic name.
//                         val chatSession = context.spawn(Topic[ClientManager.Command](chatId), chatId)
//                         println("Chat Session >>> ", chatSession)

//                         // Save chatId and chatSession to chatSessionMap
//                         chatSessionMap += (chatId -> chatSession)

//                         // Save userId and chatSession to userChatSessionMap
//                         participants.foreach(participant => {
//                             val userId = participant.path.name
//                             userChatSessionMap += (userId -> chatSession)
//                         })

//                         // Check whether from is in participants array
//                         if (!participants.contains(from)) {
//                             participants :+ from
//                         }

//                         // Subscribe all participants to the chat session
//                         participants.foreach(participant => {
//                             // Subscribe to the chat session
//                             chatSession ! Topic.Subscribe(participant)
//                             // participant ! ClientManager.Message("Welcome to the chat session")
//                         })

//                         // WARNING: Do not include this in production code
//                         // Returns dead letters because it needs time to duplicate Actor subscriptions
//                         // Must use Thread.sleep to ensure that all Actor subscriptions are valid before publishing
//                         // Perhaps use a Future to wait for all Actor subscriptions to be valid
//                         Thread.sleep(2000)
//                         chatSession ! Topic.Publish(ClientManager.Message("Session created successfully"))

//                         // participants(0) ! ClientManager.Message("Session created successfully")

//                         // participants.foreach(participant => {
//                         //     participant.expectMessage(ClientManager.Message("Session created successfully"))
//                         // })

//                         Behaviors.same
//                     case GetListings() =>
//                         println("Server received get listings")

//                         // Gets a Listing of Client actors with "Client" ServiceKey

//                         Behaviors.same
//                     case SendMessage(from, topicName, message) =>
//                         println("Server received send message")

//                         // Chat user sends message to chat session
//                         // Find chat session topic actor
//                         val chatSession = chatSessionMap.get(topicName)

//                         // Send message to chat session
//                         chatSession ! Topic.Publish(ClientManager.Message(message))

//                         Behaviors.same
//                     case TestChatSession =>
//                         println("Server received start")

//                         // Spawn Client Manager actor
//                         val clientManager = context.spawn(ClientManager(), randomUUID.toString)
//                         val clientManager2 = context.spawn(ClientManager(), randomUUID.toString)
//                         val clientManager3 = context.spawn(ClientManager(), randomUUID.toString)

//                         var clients = Array(clientManager, clientManager2, clientManager3)

//                         // Subscribe all clients to the chat session
//                         context.self ! CreateChatSession(clientManager, clients)

//                         // Publish message to all clients
//                         // context.self ! SendMessage(clientManager, "ChatSession", "Hello World")

//                         Behaviors.same
//                     case GetUserChatSessions(from, userId) =>
//                         println("Server received get user chat sessions")

//                         // Find user chat session actor
//                         val userChatSessions = userChatSessionMap.get(userId)

//                         // Send chat session to Client
//                         // from ! ClientManager.UserChatSessionsResponse(userChatSessions)
//                     case JoinChatSession(from, chatRef) =>
//                         println("Server received join chat session")

//                         // Subscribe to chat session
//                         chatRef ! Topic.Subscribe(from)

//                         // Send message to chat session
//                         chatRef ! Topic.Publish(ClientManager.Message("Welcome to the client x session"))

//                         Behaviors.same
                    
//                 }
//             }
//         }
// }

// object Server extends App {
//     val greeterMain: ActorSystem[ServerManager.Command] = ActorSystem(ServerManager(), "HelloSystem")
//     // greeterMain ! ServerManager.TestChatSession
// }
