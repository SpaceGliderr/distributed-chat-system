package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import scalafx.Includes._
import chat.ClientManager
import chat.Main
import chat.util.AlertMessage   // -- directory different
//import chat.model.User    <-- don't know if yall will change the directory for User class

@sfxml
class LogInController(
    private val userNameTextField: TextField,
    private val passwordTextField: TextField
) extends AlertMessage{

    def login(): Unit = {
        //input checking
        var errorMessage = ""
        errorMessage += userNamePwdChecking(userNameTextField.text, passwordTextField.text)
        if (errorMessage.length() > 0)
            alertError("Invalid Fields", "Please check invalid fields", errorMessage)
        else{
            //========================= uncomment this later
            // val tempUserList = User.users.toList
            // //see if yall want to add phone number variable for User class or not 
            // val index = tempUserList.indexWhere(user => user.phoneNum.value == userNameTextField.text.value)
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
            Main.clientMain ! ClientManager.LogIn(userNameTextField.text.getValue(), passwordTextField.text.getValue())
            Thread.sleep(100) //Wait for login result
            
            if(ClientManager.authenticate == true){
                Main.showChatListPage()
                Main.stage.resizable_=(true)
            } else {
                alertError("Login Failed", "Invalid Credentials.","Username or password did not match")
            }
            //=========================
        }
    }

    def cancel(): Unit = Main.showPages("view/Home.fxml")
}
