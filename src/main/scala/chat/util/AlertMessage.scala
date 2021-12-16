package chat.util
import scalafx.scene.control.{Alert, ButtonType, TextInputDialog, Button}
import scalafx.scene.Node
import scalafx.Includes._
import chat.Client
import scalafx.beans.property.StringProperty

trait AlertMessage{
    //Check username and password for login and signup
    def userNamePwdChecking(username: StringProperty, password: StringProperty): String = {
        var errorMessage = ""
        if (username.value.length == 0)
            errorMessage += "No valid username\n"
        if (password.value.length == 0)
            errorMessage += "No valid password"
        errorMessage
    }

    //Create error alert
    def alertError (_title: String, _headerText: String, _contextText: String): Unit = {
        new Alert(Alert.AlertType.Error){
            initOwner(Client.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()
    }

    //Create information alert
    def alertInformation(_title: String, _headerText: String, _contextText: String): Unit = {
         new Alert(Alert.AlertType.Information){
            initOwner(Client.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()
    }

    //Create confirmation alert
    def alertConfirmation(_title: String, _headerText: String, _contextText: String): Boolean = {
        val alert = new Alert(Alert.AlertType.Confirmation){
            initOwner(Client.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }

        val cancelButton = alert.dialogPane().lookupButton(ButtonType.Cancel)
        cancelButton.getStyleClass().clear();
        cancelButton.getStyleClass().add("secondaryButton");

        val result = alert.showAndWait()
        result match{
            case Some(ButtonType.OK) => return true
            case _ => return false
        }
    }

    //Create text input dialog
    def textInputDialog(_title: String, _headerText: String, _contextText: String): String = {
        val dialog = new TextInputDialog(){
            initOwner(Client.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }

        val okButton: Node = dialog.dialogPane().lookupButton(ButtonType.OK)
        val cancelButton: Node = dialog.dialogPane().lookupButton(ButtonType.Cancel)
        okButton.getStyleClass().add("primaryButton");
        cancelButton.getStyleClass().clear();
        cancelButton.getStyleClass().add("secondaryButton");
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