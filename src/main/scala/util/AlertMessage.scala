package chat.util
import scalafx.scene.control.{Alert, TextArea}
import scalafx.Includes._
import chat.Main

trait AlertMessage{
    def alertError (_title: String, _headerText: String, _contextText: String): Unit = {
        new Alert(Alert.AlertType.Error){
            initOwner(Main.stage)
            title       = _title
            headerText  = _headerText
            contentText = _contextText
        }.showAndWait()
    }
}