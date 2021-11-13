import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}

// Concept of Actor Discoverability
// - Gives centralized solution for retrieving any actor existing in the actor system
// - As long as service key and actor are registered

// Concept of Receptionist
// Receives registration requests from actors
// Sends actor references to actors that requests an actor reference (looks up table mapping from a ServiceKey)
// Receptionist is an actor -> Communicate with it using messages (responds in message)
// - Therefore, requesting actor must be prepared to receive the actor listing (response from Receptionist)

object ServerManager {
    // Sealed Traits:
    // - An alternative to enums
    // - Sealed traits can only be extended in the same file as its declaration
    // - Single file extension allows the compiler to know every possible subtype
    // - This provides exhaustive matching -> Will emit error if the match does not cover all subtype cases
    // https://stackoverflow.com/questions/11203268/what-is-a-sealed-trait
    sealed trait Command
    case class Message(value: String, from: ActorRef[ClientManager.Command]) extends Command

    // ServiceKeys are unique keys to identify an actor
    // It will be used by the receptionist to look up the specific actor references
    val ServerKey: ServiceKey[ServerManager.Command] = ServiceKey("Server")

    // Behavior of an actor -> Defines how it reacts to a message it receives
    def apply(): Behavior[ServerManager.Command] =
        // Setup is a factory function -> Creation of behavior instance is deferred until actor is created
        // Setup passes ActorContext as param
        Behaviors.setup { context =>
            // The ! operator is called a "bang"
            // Syntax: Actor reference to receive the command ! Command to be sent to the actor
            // The following message is registering the actor to the systems receptionist
            // ServerKey -> Unique ID of the actor; context.self -> ActorRef
            context.system.receptionist ! Receptionist.Register(ServerKey, context.self)

            // Different Behaviors an actor can respond to a message with
            Behaviors.receiveMessage { message => 
                // TODO: Make this into anonymous function
                message match {
                    // TODO: Behaviors in this function
                    case Message(value, from) =>
                        println(s"Server received message '${value}'")
                        // from ! ClientManager.Message("how are you", context.self)
                        Behaviors.same
                }
            }
        }
}

object Server extends App {
    val greeterMain: ActorSystem[ServerManager.Command] = ActorSystem(ServerManager(), "HelloSystem")
}
