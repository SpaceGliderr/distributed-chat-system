package chat
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import javafx.{scene => jfxs}
import scalafx.scene.image.Image
import scalafx.stage.{Stage, Modality}
import chat.view.{NewChatOrGroupController, ChatRoomController, ChatListController}
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import chat.model.ChatSession

object Main extends JFXApp {

  // create Client's Actor System
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val clientMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem", ConfigFactory.load("client"))
  clientMain ! ClientManager.FindServer

  //primary stage
  val rootResource = getClass.getResourceAsStream("view/RootLayout.fxml")
  val loader = new FXMLLoader(null, NoDependencyResolver)
  loader.load(rootResource);
  val roots: jfxs.layout.BorderPane = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    title = "Chatty"
    scene = new Scene {
      root = roots
      stylesheets += getClass.getResource("view/css.css").toString()
    }
    icons += new Image(getClass.getResourceAsStream("view/chatIcon.png"))
  }

  def showPages(fileName: String) = {
    val resource = getClass.getResourceAsStream(fileName)
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
  }

  def showChatListPage() = {
    val resource = getClass.getResourceAsStream("view/ChatList.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource)
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    val controller = loader.getController[ChatListController#Controller]
    controller.clientRef = Option(clientMain)
    this.roots.setCenter(roots)
    controller.showConversationList()
  }

  //to load (add new chat page) or (add new group page)
  def showNewChatOrNewGroupPage(_title: String, _isGroup: Boolean) = {
    val resource = getClass.getResourceAsStream("view/NewChatOrGroup.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val root1 = loader.getRoot[jfxs.Parent]
    val controller = loader.getController[NewChatOrGroupController#Controller]
    controller.clientRef = Option(clientMain)

    val window = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(stage)
      scene = new Scene{
        root = root1
        stylesheets += getClass.getResource("view/css.css").toString()
      }
      title = _title
      resizable_=(false)
      icons += new Image(getClass.getResourceAsStream("view/chatIcon.png"))
    }
    controller.dialogStage = window
    controller.isGroup = _isGroup
    controller.title = _title
    controller.updateContactList()
    window.showAndWait()
  }



  def showChatRoomPage(_isGroup: Boolean) = {
    val resource = getClass.getResourceAsStream("view/ChatRoom.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
    val controller = loader.getController[ChatRoomController#Controller]
    controller.clientRef = Option(clientMain)
    // Thread.sleep(2000)
    // controller.chatRoom = ClientManager.selectedChatRoom
    // ClientManager.usersInChatRoom.foreach( user =>
    //   controller.nameList = controller.nameList :+ user.username
    // )
    controller.isGroup = _isGroup
    controller.updateInfo()
  }

  def loginSuccess(): Unit = {
    this.showPages("view/ChatList.fxml")
    this.stage.resizable_=(true)
  }

  this.roots.top.value.visible_=(false)
  showPages("view/Home.fxml")
  stage.resizable_=(false)


  stage.onCloseRequest = handle( {
    clientMain.terminate
  })
}