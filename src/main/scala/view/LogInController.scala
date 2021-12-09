package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.Main
//import chat.model.User    <-- don't know if yall will change the directory for User class

@sfxml
class LogInController(
    private val phoneNumTextField: TextField,
    private val passwordTextField: TextField
){
    var errorMessage = ""

    def login(): Unit = {
        if (phoneNumTextField.text().length == 0)
            errorMessage += "No valid username\n"
        if (passwordTextField.text().length == 0)
            errorMessage += "No valid password"
        else{
            val tempUserList = User.users.toList
            //see if yall want to change the User's username change to phone number or not
            val index = tempUserList.indexWhere(user => user.phoneNum.value == phoneNumTextField.text.value)
            if (index == -1)
                //print error : no account
            else{
                if(User.users.apply(index).password.value == passwordTextField.text.value)
                    //jump to chatroom page
                else
                    //print error : wrong password
            }
        }
    }

    def cancel(): Unit = Main.showPages("view/Home.fxml")
}