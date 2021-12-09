package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.Main
import chat.util.AlertMessage   // <-- directory different
//import chat.model.User    <-- don't know if yall will change the directory for User class

@sfxml
class LogInController(
    private val phoneNumTextField: TextField,
    private val passwordTextField: TextField

) extends AlertMessage{

    def login(): Unit = {
        var errorMessage = ""
        if (phoneNumTextField.text().length == 0)
            errorMessage += "No valid username\n"
        else if(phoneNumTextField.text().length != 10 && phoneNumTextField.text().length != 11)
            errorMessage += "Incorrect format for phone number\n"
        else if(phoneNumTextField.text.value.substring(0,1) != "01")
            errorMessage += "Incorrect format for phone number\n"
        if (passwordTextField.text().length == 0)
            errorMessage += "No valid password"
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            //uncomment this
            // val tempUserList = User.users.toList
            // //see if yall want to change the User's username change to phone number or not
            // val index = tempUserList.indexWhere(user => user.phoneNum.value == phoneNumTextField.text.value)
            // if (index == -1)
            //     alertError("Login Fail", "Fail to login", "This account does not exist.")
            // else{
            //     if(User.users.apply(index).password.value == passwordTextField.text.value)
            //         //jump to chatroom page
            //     else
            //         alertError("Login Fail", "Fail to login", "Incorrect password, please try again.")
            // }
        }
    }

    def cancel(): Unit = Main.showPages("view/Home.fxml")
}