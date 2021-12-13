package chat.view
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import chat.Main
import scalafx.scene.image.{ImageView, Image}

@sfxml
class HomeController(
    private val imageView: ImageView
){
    // val image = new Image(getClass().getResourceAsStream("peopleOnDevices.png"))
    // imageView.image_=(image)
    // imageView.preserveRatio_=(false)
    // imageView.opacity_=(0.53)

    def login(): Unit = Main.showPages("view/LogIn.fxml")

    def signup(): Unit = Main.showPages("view/SignUp.fxml")
}