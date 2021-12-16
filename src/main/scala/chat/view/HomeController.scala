package chat.view
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import chat.Client

@sfxml
class HomeController(){

    //Open Login page
    def login(): Unit = Client.showLoginPage()

    //Open Signup page
    def signup(): Unit = Client.showSignUpPage()
}