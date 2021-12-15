package chat.view
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import chat.Main
import scalafx.scene.image.{ImageView, Image}

@sfxml
class HomeController(
    private val imageView: ImageView
){

    //Open Login page
    def login(): Unit = Main.showLoginPage()

    //Open Signup page
    def signup(): Unit = Main.showSignUpPage()
}