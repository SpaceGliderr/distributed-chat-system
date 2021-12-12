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

object Main extends JFXApp {

  // create Client's Actor System
  val greeterMain: ActorSystem[ClientManager.Command] = ActorSystem(ClientManager(), "HelloSystem", ConfigFactory.load("client"))
  greeterMain ! ClientManager.FindServer

  val username = scala.io.StdIn.readLine("Enter Username: ")
  val password = scala.io.StdIn.readLine("Enter Password: ")
  greeterMain ! ClientManager.LogIn(username, password)


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
    controller.clientRef = Option(greeterMain)
    this.roots.setCenter(roots)

  }

  //to load (add new chat page) or (add new group page)
  def showNewChatOrNewGroupPage(_title: String, _contacts: Array[String]) = {
    val resource = getClass.getResourceAsStream("view/NewChatOrGroup.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val root1 = loader.getRoot[jfxs.Parent]
    val controller = loader.getController[NewChatOrGroupController#Controller]
    controller.clientRef = Option(greeterMain)

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
    controller.title = _title
    controller.contacts = _contacts
    window.showAndWait()
  }

  var group: Boolean = false
  def showChatRoomPage(_nameList: Array[String], _messages: Array[String], _group: Boolean) = {
    this.group = _group
    val resource = getClass.getResourceAsStream("view/ChatRoom.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
    val controller = loader.getController[ChatRoomController#Controller]
    controller.nameList = _nameList
    controller.messages = _messages
  }

  this.roots.top.value.visible_=(false)
  showPages("view/Home.fxml")
  stage.resizable_=(false)
}