package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import chat.Main
import scalafx.scene.control.SelectionMode
import scalafx.scene.Node
import chat.view.

@sfxml
class ChatRoomController(
    private val messageTextField: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val sendButton: Node,
    private val messageList: ListView[String]   //-- not sure the type

) extends AlertMessage{

    val deleteIcon = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    imageView.image_=(newGroupIcon)
    val sendIcon = new Image(getClass().getResourceAsStream("sendIcon.png"))
    imageView1.image_=(sendIcon)
    val backIcon = new Image(getClass().getResourceAsStream("backIcon.png"))
    imageView2.image_=(backIcon)

    //allow multiple selection
    messageList.selectionModel().setSelectionMode(SelectionMode.Multiple)

    //if the message text field is empty -> disable the send button, else -> able it
    messageTextField.text.onChange{(_, _, newValue) => {
            if (!newValue.trim().isEmpty)
                sendButton.disable_=(false)
            else
                sendButton.disable_=(true)
            }
    }


    // --
    // def showMessageList() = {

    // }


    def cancel(): Unit = {
        Main.showPages("view/ChatList.fxml")
    }

    //================================ try run, remove later
    contacts = Array("1","2","3")
    val tryy = new ObservableBuffer[String]()
    tryy ++= contacts
    contactList.items = tryy
    //=================================

    def deleteChat() : Unit = {
        if (messageList.selectionModel().selectedItem.value == null) 
            alertError("Delete Fail", "Fail to delete message", "You must select at least one message")
        else{
            val confirm = alertConfirmation("Delete Confirmation", null, "Are you sure you want to delete this message(s)?")
            if (confirm)
                //-- remove from list and database & show updated list

                //======================= try run, remove later
                println("deleted")
                //========================
        }
    }

    def sendMessage() : Unit = {
        //-- 
        // 1. update message list
        // 2. update database

        //disable the button again after sending
        sendButton.disable_=(true)
    }
}