package view
import scalafxml.core.macros.sfxml
import scalafx.application.Platform
import Main.scala

@sfxml
class RootLayoutController() {
    def handleClose() {
        Platform.exit()
    }
}
