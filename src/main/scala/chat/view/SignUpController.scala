package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import chat.ClientManager
import chat.Main
import scalafx.application.Platform
import chat.util.AlertMessage 
import akka.actor.typed.ActorRef

@sfxml
class SignUpController(
    private val userNameTextField: TextField,
    private val passwordTextField: TextField

)extends AlertMessage{
    
    //Variable
    var clientRef: Option[ActorRef[ClientManager.Command]] = None

    //Sign up user based on input username and password
    def signup(): Unit = {
        var errorMessage = ""
        errorMessage += userNamePwdChecking(userNameTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            clientRef.get ! ClientManager.SignUp(userNameTextField.text.getValue(), passwordTextField.text.getValue())
        }
    }

     //Detect changes after signup; if successfully signed up, then show success message; else show error alert.
    ClientManager.signup.onChange{ 
        if(ClientManager.signup.getValue() == "true"){
            Platform.runLater {
                alertInformation("Sign Up Success", null, "Congratulations, your account has been successfully created.")
                Main.showPages("view/Home.fxml")
            }
        } else if(ClientManager.signup.getValue() == "false"){
            Platform.runLater{
                alertError("Sign Up Failed", "Error signing up.","Username already taken.")
            }
        }
        ClientManager.signup.value = "handled"
    }

    //Return to home page
    def cancel(): Unit = Main.showPages("view/Home.fxml")
}
