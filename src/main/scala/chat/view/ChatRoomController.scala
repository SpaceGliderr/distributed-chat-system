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
    private val deleteIcon: ImageView,
    private val sendIcon: ImageView,
    private val backIcon: ImageView,
    private val sendButton: Button,
    private val groupOrChatName: Label,
    private val statusOrGrpMemNames: Label,
    private val messageList: ListView[String]   
) extends AlertMessage{

    //Variables
    var clientRef: Option[ActorRef[ClientManager.Command]] = None
    var chatRoom: ChatSession = null
    var nameList: Array[String] = Array()
    var isGroup: Boolean = false
    var messages = new ObservableBuffer[String]()
    var messageIds = new ObservableBuffer[Long]()

    //Get images from resources
    val deleteIconImage = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    val sendIconImage = new Image(getClass().getResourceAsStream("sendIcon.png"))
    val backIconImage = new Image(getClass().getResourceAsStream("backIcon.png"))

    //Update the imageViews
    deleteIcon.image_=(deleteIconImage)
    sendIcon.image_=(sendIconImage)
    backIcon.image_=(backIconImage)

    //Update Messages
    def updateMessage(): Unit = {
        messages.clear()
        messageIds.clear()
        for ((id, value) <- ClientManager.sessionMessages) {
            messages += value
            messageIds += id
        }
        messageList.setItems(messages)
        messageList.scrollTo(messages.size())
    }

    //If the message text field is empty -> disable the send button, else -> able it
    messageTextField.text.onChange{(_, _, newValue) => {
            if (!newValue.trim().isEmpty)
                sendButton.disable_=(false)
            else
                sendButton.disable_=(true)
            }
    }

    //Update chat room information
    def updateInfo(): Unit = {
        this.chatRoom = ClientManager.selectedChatRoom
        groupOrChatName.text = this.chatRoom.name
        this.nameList = Array()
        ClientManager.usersInChatRoom.foreach( user =>
            nameList = nameList :+ user.username
        )
        if (isGroup){
            statusOrGrpMemNames.text = nameList.mkString(", ")
        }

    }

    //Leave current chat room and return to chat list page
    def returnToChatList(): Unit = {
        clientRef.get ! ClientManager.LeaveSession(chatRoom.id)
        Main.showChatListPage()
    }

    //Delete select message
    def deleteMessage() : Unit = {
        if (messageList.selectionModel().selectedItem.value == null)
            alertError("Delete Fail", "Fail to delete message", "You must select at least one message")
        else{
            val confirm = alertConfirmation("Delete Confirmation", null, "Are you sure you want to delete this message(s)?")
            if (confirm) {
                val messageId = messageIds(messageList.getSelectionModel().getSelectedIndex())
                clientRef.get ! ClientManager.DeleteMessage(messageId)
            }
                
        }
    }

    //Send message
    def sendMessage() : Unit = {
        val message = messageTextField.text
        clientRef.get ! ClientManager.SendMessage(message.get())
        messageTextField.text_=("")
        sendButton.disable_=(true)
    }

    //Detect changes in chat room and update
    ClientManager.sessionMessages.onChange{(ns, _) =>
        Platform.runLater {
            updateMessage()
        }
    }
    ClientManager.usersInChatRoom.onChange{
        Platform.runLater {
            updateInfo()
        }
    }
    updateInfo()
    updateMessage()
}