package chat.util
import scalafx.scene.control.{Alert, TextArea}
import scalafx.Includes._
import chat.Main
import scalafx.beans.property.StringProperty

trait AlertMessage{
    def phoneNumPwdChecking(phoneNum: StringProperty, password: StringProperty): String = {
        var errorMessage = ""
        if (phoneNum.value.length == 0)
            errorMessage += "No valid phone number\n"
        else if(phoneNum.value.length != 10 && phoneNum.value.length != 11)
            errorMessage += "Incorrect format for phone number\n"
        else if(phoneNum.value.substring(0,2) != "01")
            errorMessage += "Incorrect format for phone number\n"
        if (password.value.length == 0)
            errorMessage += "No valid password"
        errorMessage
    }

    def alertError (_title: String, _headerText: String, _contextText: String): Unit = {
        new Alert(Alert.AlertType.Error){
            initOwner(Main.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()
    }

    def alertInformation(_title: String, _headerText: String, _contextText: String): Unit = {
         new Alert(Alert.AlertType.Information){
            initOwner(Main.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()
    }
}