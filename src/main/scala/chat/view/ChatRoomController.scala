package chat.view
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView, Label, Button}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.SelectionMode
import chat.{Main, ClientManager}
import chat.model.{ChatSession, UserChatSession}
import chat.util.AlertMessage
import akka.actor.typed.ActorRef
import scalafx.application.Platform

@sfxml
class ChatRoomController(
    private val messageTextField: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val sendButton: Button,
    private val groupOrChatName: Label,
    private val statusOrGrpMemNames: Label,
    private val messageList: ListView[String]   //-- not sure the type

) extends AlertMessage{

    //pass in from Main
    // var messages: Array[String] = null    //-- not sure the type
    var chatRoom: ChatSession = null
    var nameList: Array[String] = Array()
    var clientRef: Option[ActorRef[ClientManager.Command]] = None
    var isGroup: Boolean = false
    var messages = new ObservableBuffer[String]()

    //======================== to test run, later delete
    // if(group){
    //     groupOrChatName.text_=("buat assignment")
    //     nameList = Array("john","nick","wenyi", "shiqi", "peini")
    //     val list = nameList.toList
    //     statusOrGrpMemNames.text=(list.mkString(", "))
    // }
    // else{
    //     groupOrChatName.text_=("john")
    //     statusOrGrpMemNames.text=("available")
    // }
    //========================

    //-- use the passed in "nameList" to update the "statusOrGrpMemNames" Label
    //  if is group chat -> show group name and member names
    //  if is personal chat -> show that person's name and status
    // if (group){
    //     groupOrChatName -> //grp name
    //     statusOrGrpMemNames -> //member names, can cankao: mkString method of List
    // }
    // else{
    //     groupOrChatName -> //that ppl name
    //     statusOrGrpMemNames -> //status
    // }

    val deleteIcon = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    imageView.image_=(deleteIcon)
    val sendIcon = new Image(getClass().getResourceAsStream("sendIcon.png"))
    imageView1.image_=(sendIcon)
    val backIcon = new Image(getClass().getResourceAsStream("backIcon.png"))
    imageView2.image_=(backIcon)

    //allow multiple selection
    messageList.selectionModel().setSelectionMode(SelectionMode.Multiple)

    def updateMessage(): Unit = {
        messages.clear()
        ClientManager.sessionMessages.foreach(s => messages += s)
        messageList.setItems(messages)
        messageList.scrollTo(messages.size())
    }
    //if the message text field is empty -> disable the send button, else -> able it
    messageTextField.text.onChange{(_, _, newValue) => {
            if (!newValue.trim().isEmpty)
                sendButton.disable_=(false)
            else
                sendButton.disable_=(true)
            }
    }

    def updateInfo(): Unit = {
        // this.chatRoom = ClientManager.selectedChatRoom
        groupOrChatName.text = this.chatRoom.name

        if (isGroup){
            statusOrGrpMemNames.text = nameList.mkString(", ")
        }

    }
    // --
    // def showMessageList() = {

    // }


    def cancel(): Unit = {
        Main.clientMain ! ClientManager.LeaveSession(chatRoom.id)
        Main.showChatListPage()
    }

    //================================ try run, remove later
    // val tryy = new ObservableBuffer[String]()
    // tryy ++= Array("1","2","3")
    // messageList.items = tryy
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
        // 3. rmb to update the latest message for ChatListPage (if yall got put the latest message la)

        //disable the button again & empty the text field after sending message
        val message = messageTextField.text
        Main.clientMain ! ClientManager.SendMessage(message.get())
        messageTextField.text_=("")
        sendButton.disable_=(true)
    }

    ClientManager.sessionMessages.onChange{(ns, _) =>
        Platform.runLater {
            updateMessage()
        }
    }

    updateMessage()
}