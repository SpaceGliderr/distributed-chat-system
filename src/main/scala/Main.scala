import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import javafx.{scene => jfxs}
import scalafx.scene.image.Image
import scalafx.stage.{Stage, Modality}

class Main {

}

object Main extends JFXApp {

  val rootResource = getClass.getResourceAsStream("view/RootLayout.fxml")
  val loader = new FXMLLoader(null, NoDependencyResolver)
  loader.load(rootResource);
  val roots: javafx.scene.layout.BorderPane = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    title = "Chatty"
    scene = new Scene () {
      root = roots
    }
    //icons += new Image(getClass.getResourceAsStream("/images/icon.jpg"))
    //resizable = false
  }

  def showHome() = {
    val resource = getClass.getResourceAsStream("view/Home.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    Main.roots.setCenter(roots)
  }

  showHome()

}