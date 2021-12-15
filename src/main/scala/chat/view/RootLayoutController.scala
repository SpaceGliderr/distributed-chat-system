package chat.view
import scalafxml.core.macros.sfxml
import scalafx.application.Platform
import chat.Client
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import javafx.stage.Stage

@sfxml
class RootLayoutController() {

    //Close the application and terminate client actor
    def handleClose() {
        Platform.exit()
        Client.clientMain.terminate
    }

    //Return to home page
    def handleLogOut() {
        Client.showPages("view/Home.fxml")
        Client.stage.scene().getWindow().asInstanceOf[Stage].setMaximized(false)
        Client.roots.getTop().setVisible(false)
        Client.stage.resizable_=(false)
    }

    //Show application description
    def handleAbout(action: ActionEvent) {
        new Alert(AlertType.Information) {
            initOwner(Client.stage)
            title = "About"
            headerText = "Chatty: A distributed chat application"
            contentText = "This is a distributed chat application that allows multiple users to chat simulataneously. This app is created as an assignment project for the subject of Distributed Systems."
        }.showAndWait()
    }
}
