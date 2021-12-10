package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import chat.Main
//import chat.model.User    <-- don't know if yall will change the directory for User class
import chat.util.AlertMessage //-- directory different

@sfxml
class SignUpController(
    private val nameTextField: TextField,
    private val phoneNumTextField: TextField,
    private val passwordTextField: TextField

)extends AlertMessage{

    def signup(): Unit = {
        var errorMessage = ""
        if (nameTextField.text().length == 0)
            errorMessage += "No valid username\n"
        errorMessage += phoneNumPwdChecking(phoneNumTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            //================ uncomment this later
            //val account = new User(......)
            //User.users += account
            //account.saveUser()
            //================
            alertInformation("Sign Up Success", null, "Congratulations, your account has been successfully created")
            Main.showPages("view/Home.fxml")
        }
    }
    def cancel(): Unit = Main.showPages("view/Home.fxml")
}
