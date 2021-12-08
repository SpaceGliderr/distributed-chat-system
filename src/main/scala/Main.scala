package chat
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import javafx.{scene => jfxs}
import scalafx.scene.image.Image
import scalafx.stage.{Stage, Modality}

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
    //icons += new Image(getClass.getResourceAsStream("/images/icon.jpg"))
    //resizable = false
  }

  //load unresizable pages
  def showPages(fileName: String) = {
    val resource = getClass.getResourceAsStream(fileName)
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]()
    this.roots.setCenter(roots)
    stage.resizable_=(false)
  }

  showPages("view/Home.fxml")

}