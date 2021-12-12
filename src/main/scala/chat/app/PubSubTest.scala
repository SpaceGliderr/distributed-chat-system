// // NOTE: This import is from akka.actor package, not the akka.actor.typed package that teacher uses
// // This is merely a test file to test the akka.cluster.pubsub package
// import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
// import akka.cluster.pubsub.DistributedPubSub
// import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck, Publish }
// import java.lang.Thread

// // More on PubSub using Akka clusters
// // https://doc.akka.io/docs/akka/current/distributed-pub-sub.html

// class Subscriber extends Actor with ActorLogging {
//     val mediator = DistributedPubSub(context.system).mediator
//     mediator ! Subscribe("content", self)
//     println("Subscriber started")

//     def receive = {
//         case s: String =>
//             log.info("Actor name = {}", self.path.name)
//             log.info("Received from publisher {}", s)
//         case SubscribeAck(Subscribe("content", None, self)) =>
//             log.info("Subscribing")
//     }
// }

// object Publisher extends Actor {
//     val mediator = DistributedPubSub(context.system).mediator

//     def receive = {
//         case in: String =>
//             val out = in
//             mediator ! Publish("content", out)
//     }
// }

// object PubSubTest extends App {
//     val system: ActorSystem = ActorSystem("HelloSystem")
//     val pubActor = system.actorOf(Props(Publisher), "pub")
//     val subActor1 = system.actorOf(Props[Subscriber], "sub1")
//     val subActor2 = system.actorOf(Props[Subscriber], "sub2")
//     val subActor3 = system.actorOf(Props[Subscriber], "sub3")
//     Thread.sleep(1000)
//     pubActor ! "Hello"
// }
