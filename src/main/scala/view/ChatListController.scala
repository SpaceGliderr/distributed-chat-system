package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}

@sfxml
class ChatListController(
    private val searchBar: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val imageView3: ImageView,
    private val conversationList: ListView[String]
){
    val searchIcon = new Image(getClass().getResourceAsStream("searchIcon.png"))
    imageView.image_=(searchIcon)
    val newChatIcon = new Image(getClass().getResourceAsStream("newChatIcon.png"))
    imageView1.image_=(newChatIcon)
    val newGroupIcon = new Image(getClass().getResourceAsStream("newGroupIcon.png"))
    imageView2.image_=(newGroupIcon)
    val deleteIcon = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    imageView3.image_=(deleteIcon)


    // 1.var conversations
    // 2.var latestMsg
    // 3.var combinedList
    // 4.def updateConversationList()

    def search(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            // 5.filter conversation list

            searchBar.visible_=(false)
        }
    }

    def addNewChat: Unit = {
        //try
        println("add new chat")
    }

    def addNewGroup: Unit = {
        //try
        println("add new group")
    }

    def deleteConversation: Unit = {
        //try
        println("delete")
    }
}