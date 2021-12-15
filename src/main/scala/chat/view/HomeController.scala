package chat.view
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import chat.Client
import scalafx.scene.image.{ImageView, Image}

@sfxml
class HomeController(
    private val imageView: ImageView
){

    //Open Login page
    def login(): Unit = Client.showLoginPage()

    //Open Signup page
    def signup(): Unit = Client.showSignUpPage()
}