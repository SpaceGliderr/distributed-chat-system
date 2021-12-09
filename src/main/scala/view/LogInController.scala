package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.Main

@sfxml
class LogInController(
    private val phoneNumTextField: TextField,
    private val passwordTextField: TextField
){
    def login(): Unit = println("") //*

    def cancel(): Unit = Main.showPages("view/Home.fxml")
}