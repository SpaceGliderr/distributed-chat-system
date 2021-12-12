package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import chat.ClientManager
import chat.Main
//import chat.model.User    <-- don't know if yall will change the directory for User class
import chat.util.AlertMessage //-- directory different

@sfxml
class SignUpController(
    private val userNameTextField: TextField,
    private val passwordTextField: TextField

)extends AlertMessage{

    def signup(): Unit = {
        var errorMessage = ""
        errorMessage += userNamePwdChecking(userNameTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            //================ uncomment this later
            //val account = new User(......)
            //User.users += account
            //account.saveUser()
            //================
            Main.clientMain ! ClientManager.SignUp(userNameTextField.text.getValue(), passwordTextField.text.getValue())
            Thread.sleep(100) //Wait for signup result

            if(ClientManager.signup == true){
                alertInformation("Sign Up Success", null, "Congratulations, your account has been successfully created.")
                Main.showPages("view/Home.fxml")
            } else {
                alertError("Sign Up Failed", "Error signing up.","Username already taken.")
            }
            
        }
    }
    def cancel(): Unit = Main.showPages("view/Home.fxml")
}
