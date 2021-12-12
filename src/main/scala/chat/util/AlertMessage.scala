package chat.util
import scalafx.scene.control.{Alert, TextArea, ButtonType, TextInputDialog, Button}
import scalafx.scene.Node
import scalafx.Includes._
import chat.Main
import scalafx.beans.property.StringProperty
import scala.Tuple2
import scalafx.event.ActionEvent

trait AlertMessage{
    def userNamePwdChecking(username: StringProperty, password: StringProperty): String = {
        var errorMessage = ""
        if (username.value.length == 0)
            errorMessage += "No valid username\n"
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

    def alertConfirmation(_title: String, _headerText: String, _contextText: String): Boolean = {
        val alert = new Alert(Alert.AlertType.Confirmation){
            initOwner(Main.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()

        alert match{
            case Some(ButtonType.OK) => return true
            case _ => return false
        }
    }

    def textInputDialog(_title: String, _headerText: String, _contextText: String): String = {
        val dialog = new TextInputDialog(){
            initOwner(Main.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }
             
        val okButton: Node = dialog.dialogPane().lookupButton(ButtonType.OK)
        okButton.disable_=(true)
        dialog.editor.text.onChange{(_, _, newValue) => {
            if (!newValue.trim().isEmpty)
                okButton.disable_=(false)
            else
                okButton.disable_=(true)
            }
        }
        val result = dialog.showAndWait()

        result match{
            case Some(input) => return (input.asInstanceOf[String])
            case _ => return ""
        }
    }
}