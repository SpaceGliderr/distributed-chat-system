package chat
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import javafx.{scene => jfxs}
import scalafx.scene.image.Image
import scalafx.stage.{Stage, Modality}
import chat.view.NewChatOrGroupController

//class Main {

//}

object Main extends JFXApp {

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
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
  }

  //to load (add new chat page) or (add new group page)
  def showNewChatOrNewGroupPage(_title: String, _contacts: Array[String]): Boolean = {
    val resource = getClass.getResourceAsStream("view/NewChatOrGroup.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val root1 = loader.getRoot[jfxs.Parent]
    val controller = loader.getController[NewChatOrGroupController#Controller]

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
    controller.okClicked
  }

  def showChatRoomPage() = {
    val resource = getClass.getResourceAsStream("view/ChatRoom.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
  }

  showPages("view/Home.fxml")
  stage.resizable_=(false)
}