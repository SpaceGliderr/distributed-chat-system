package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}

@sfxml
class ChatListController(
    private val searchBar: TextField,
    private val imageView: ImageView,
    private val listView: ListView[String]
){
    val image = new Image(getClass().getResourceAsStream("searchIcon.png"))
    imageView.image_=(image)

    def search(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            //show result
            searchBar.visible_=(false)
        }
    }
}