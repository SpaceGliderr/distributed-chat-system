import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import ClientManager.Command

object ClientManager {
    sealed trait Command
    final case object FindServer extends Command
    private case class ListingResponse(listing: Receptionist.Listing) extends Command
    case object Start extends Command

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
                    case ClientManager.Start =>
                        context.self ! FindServer
                        for (remote <- remoteOpt){
                            // TODO: Send message to Server here
                        }
                        Behaviors.same
                }
            }
        }
}

object Client extends App {
    val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem")
}
