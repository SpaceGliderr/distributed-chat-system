import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ChatRoom {
    sealed trait Command
    case class Subscribe(participants: Array[ActorRef[ClientManager.Command]]) extends Command // Join the chat room
    case class Unsubscribe() extends Command // Leave the chat room
    case class Publish(message: String) extends Command // Send a message to all subscribers
    case class GetParticipants() extends Command // Get a list of all participants

    // Class attributes
    private var subscribers: Set[ActorRef[ClientManager.Command]] = Set.empty[ActorRef[ClientManager.Command]]
    private var messages: Map[ActorRef[ClientManager.Command], String] = Map()

    def apply(): Behavior[ChatRoom.Command] =
        Behaviors.setup { context =>
            Behaviors.receiveMessage { message =>
                message match {
                    case Subscribe(participants) =>
                        context.log.info("Subscribing")

                        // Add participants to subscribers
                        participants.foreach(participant =>
                            subscribers += participant
                        )
                        // subscribers = subscribers + participants.toSet
                        Behaviors.same

                    case Unsubscribe() =>
                        context.log.info("Unsubscribing")
                        Behaviors.same

                    case Publish(message) =>
                        context.log.info("Publishing")

                        // Send message to all subscribers
                        subscribers.foreach(subscriber =>
                            subscriber ! ClientManager.Message(message)
                        )
                        Behaviors.same

                    case GetParticipants() =>
                        context.log.info("Getting participants")
                        println(subscribers)
                        Behaviors.same
                }
            }
        }
}