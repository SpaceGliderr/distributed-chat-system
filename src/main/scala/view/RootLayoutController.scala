package chat.view
import scalafxml.core.macros.sfxml
import scalafx.application.Platform
import chat.Main

@sfxml
class RootLayoutController() {
    def handleClose() {
        Platform.exit()
    }
}
