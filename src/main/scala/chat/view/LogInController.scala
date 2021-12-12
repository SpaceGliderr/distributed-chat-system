package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.Main
import chat.util.AlertMessage   // -- directory different
//import chat.model.User    <-- don't know if yall will change the directory for User class

@sfxml
class LogInController(
    private val phoneNumTextField: TextField,
    private val passwordTextField: TextField

) extends AlertMessage{

    def login(): Unit = {
        //input checking
        var errorMessage = ""
        // errorMessage += phoneNumPwdChecking(phoneNumTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            //========================= uncomment this later
            // val tempUserList = User.users.toList
            // //see if yall want to add phone number variable for User class or not
            // val index = tempUserList.indexWhere(user => user.phoneNum.value == phoneNumTextField.text.value)
            // if (index == -1)
            //     alertError("Login Fail", "Fail to login", "This account does not exist.")
            // else{
            //     if(User.users.apply(index).password.value == passwordTextField.text.value){
            //         Main.showPages("view/ChatList.fxml")
            //         Main.stage.resizable_=(true)
            //     }
            //     else
            //         alertError("Login Fail", "Fail to login", "Incorrect password, please try again.")
            // }
            //==============================

            //========================= to test run, later delete
            Main.showChatListPage()
            Main.stage.resizable_=(true)
            //=========================
        }
    }

    def cancel(): Unit = Main.showPages("view/Home.fxml")
}