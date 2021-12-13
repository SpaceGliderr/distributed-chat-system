package chat.view
import scalafxml.core.macros.sfxml
import scalafx.application.Platform
import chat.Main
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import javafx.stage.Stage

@sfxml
class RootLayoutController() {
    def handleClose() {
        Platform.exit()
        Main.clientMain.terminate
    }

    def handleLogOut() {
        Main.showPages("view/Home.fxml")
        Main.stage.scene().getWindow().asInstanceOf[Stage].setMaximized(false)
        Main.roots.getTop().setVisible(false)
        Main.stage.resizable_=(false)
    }

    def handleAbout(action: ActionEvent) {
        new Alert(AlertType.Information) {
            initOwner(Main.stage)
            title = "About"
            headerText = "Chatty: A distributed chat application"
            contentText = "This is a distributed chat application that allows multiple users to chat simulataneously. This app is created as an assignment project for the subject of Distributed Systems."
        }.showAndWait()
    }
}
