package chat.view
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import chat.Main

@sfxml
class HomeController(){
    def login(): Unit = Main.showPages("view/LogIn.fxml")

    def signup(): Unit = Main.showPages("view/SignUp.fxml")
}