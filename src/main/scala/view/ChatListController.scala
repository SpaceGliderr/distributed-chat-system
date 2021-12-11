package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import chat.Main
import chat.util.AlertMessage
import scalafx.collections.ObservableBuffer

@sfxml
class ChatListController(
    private val searchBar: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val imageView3: ImageView,
    private val imageView4: ImageView,
    private val conversationList: ListView[String]  //-- not sure the type 

)extends AlertMessage{
    val searchIcon = new Image(getClass().getResourceAsStream("searchIcon.png"))
    imageView.image_=(searchIcon)
    val newChatIcon = new Image(getClass().getResourceAsStream("newChatIcon.png"))
    imageView1.image_=(newChatIcon)
    val newGroupIcon = new Image(getClass().getResourceAsStream("newGroupIcon.png"))
    imageView2.image_=(newGroupIcon)
    val viewIcon = new Image(getClass().getResourceAsStream("viewChatIcon.png"))
    imageView3.image_=(viewIcon)
    val deleteIcon = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    imageView4.image_=(deleteIcon)

    val contacts: Array[String] = null  /* -- not sure if is to use string, 
    if not please chg the data type of 
    1. Main's showNewChatOrNewGroupPage method's parameter's type
    2. NewChatOrGroupController's "contacts"" variable's type*/

    //make the menu bar visible
    Main.roots.top.value.visible_=(true)


    // --
    // def showConversationList() = {

    // }

    def search(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            //-- filter conversation list

            searchBar.visible_=(false)
        }
    }

    def addNewChat: Unit = {
        Main.showNewChatOrNewGroupPage("Add New Chat", contacts)
    }

    def addNewGroup: Unit = {
        Main.showNewChatOrNewGroupPage("Add New Group", null)
    }

    def viewConversation: Unit = {
        if (conversationList.selectionModel().selectedItem.value == null) 
            alertError("Open Fail", "Fail to open conversation", "You must select a conversation")
        else
            //-- check if is group chat then pass in true to ChatRoomPage, else pass in false
            // if (--is group--)
            //     Main.showChatRoomPage(null, null, true)      //-- pass in messages and names
            // else
            //     Main.showChatRoomPage(null, null, false)     //-- pass in messages and names

            //============================try run, later delete
            Main.showChatRoomPage(null, null, true)
            //===============================
    }

    //================================ try run, remove later
    val tryy = new ObservableBuffer[String]()
    tryy ++= Array("1","2","3")
    conversationList.items = tryy
    //=================================

    def deleteConversation: Unit = {
        if (conversationList.selectionModel().selectedItem.value == null) 
            alertError("Delete Fail", "Fail to delete conversation", "You must select one conversation")
        else{
            val confirm = alertConfirmation("Delete Confirmation", null, "Are you sure you want to delete this conversation?")
            if (confirm)
                //-- remove from list and database & show updated list

                //======================= try run, remove later
                println("deleted")
                //========================
        }
    }
}