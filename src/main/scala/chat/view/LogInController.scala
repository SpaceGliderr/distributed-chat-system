package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.{Client, ClientManager}
import scalafx.application.Platform
import chat.util.AlertMessage
import akka.actor.typed.ActorRef

@sfxml
class LogInController(
    private val userNameTextField: TextField,
    private val passwordTextField: TextField
) extends AlertMessage{

    //Variable
    var clientRef: Option[ActorRef[ClientManager.Command]] = None

    //Login user based on input username and password
    def login(): Unit = {
        var errorMessage = ""
        errorMessage += userNamePwdChecking(userNameTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            clientRef.get ! ClientManager.LogIn(userNameTextField.text.getValue(), passwordTextField.text.getValue())
        }
    }

    //Detect changes after login; if successfully authenticated, then open chat list page; else show error alert.
    ClientManager.authenticate.onChange{
        if(ClientManager.authenticate.getValue() == "true"){
            Platform.runLater {
                Client.showChatListPage()
            }
        } else if(ClientManager.authenticate.getValue() == "false"){
            Platform.runLater{
                alertError("Login Failed", "Invalid Credentials.","Username or password did not match")
            }
        }
        ClientManager.authenticate.value = "handled"
    }

    //Return to home page
    def cancel(): Unit = Client.showPages("view/Home.fxml")
}
