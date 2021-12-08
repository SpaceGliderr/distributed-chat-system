package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.text.Text
import scalafx.Includes._
import chat.Main

@sfxml
class HomeController(
    private val welcomeText: Text
){
    def login(): Unit = Main.showPages("view/LogIn.fxml")

    def signup(): Unit = Main.showPages("view/SignUp.fxml")
}