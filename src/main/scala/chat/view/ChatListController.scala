package chat.view

import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, TableView, TableColumn, Label}
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.scene.image.{ImageView, Image}
import akka.actor.typed.ActorRef
import chat.{Main, ClientManager}
import chat.util.AlertMessage
import chat.model.ChatSession
import scalafx.collections.ObservableBuffer
import scalafx.application.Platform

@sfxml
class ChatListController(
    private val searchBar: TextField,
    private val searchIcon: ImageView,
    private val clearIcon: ImageView,
    private val newChatIcon: ImageView,
    private val groupChatIcon: ImageView,
    private val openIcon: ImageView,
    private val deleteIcon: ImageView,
    private val conversationList: TableView[ChatSession],
    private val chatSessionName: TableColumn[ChatSession, String]

)extends AlertMessage{

    //Variables
    var clientRef: Option[ActorRef[ClientManager.Command]] = None
    var chatsessions = new ObservableBuffer[ChatSession]()

    //Get images from resources
    val searchIconImage = new Image(getClass().getResourceAsStream("searchIcon.png"))
    val newChatIconImage = new Image(getClass().getResourceAsStream("newChatIcon.png"))
    val groupChatIconImage = new Image(getClass().getResourceAsStream("newGroupIcon.png"))
    val openIconImage = new Image(getClass().getResourceAsStream("viewChatIcon.png"))
    val deleteIconImage = new Image(getClass().getResourceAsStream("deleteIcon.png"))
    val clearIconImage = new Image(getClass().getResourceAsStream("clearIcon.png"))

    //Update the imageViews
    searchIcon.image_=(searchIconImage)
    newChatIcon.image_=(newChatIconImage)
    groupChatIcon.image_=(groupChatIconImage)
    openIcon.image_=(openIconImage)
    deleteIcon.image_=(deleteIconImage)
    clearIcon.image_=(clearIconImage)

    //Make the menu bar visible
    Main.roots.top.value.visible_=(true)

    //Populate the conversations in the table
    def showConversationList() = {
        this.chatsessions = new ObservableBuffer[ChatSession]()
        ClientManager.chatSessions.foreach(s => chatsessions += s)
        conversationList.items = this.chatsessions

        chatSessionName.cellValueFactory = cell =>
            if (cell.value.name == ClientManager.user.username)
                ObjectProperty[String](cell.value.description)
            else
                ObjectProperty[String](cell.value.name)

    }

    //Clear search text
    def clearSearchBar(): Unit = {
        searchBar.text = ""
        clearIcon.visible_=(false)
    }

    //Open add new chat page
    def addNewChat: Unit = {
        Main.showNewChatOrNewGroupPage("Add New Chat", false)
    }

    //Open add new group page
    def addNewGroup: Unit = {
        Main.showNewChatOrNewGroupPage("Add New Group", true)
    }

    //Open chat room page
    def viewConversation: Unit = {
        if (conversationList.selectionModel().selectedItem.value == null)
            alertError("Open Fail", "Fail to open conversation", "You must select a conversation")
        else{
            clientRef.get ! ClientManager.UpdateChatInfo(this.chatsessions(conversationList.getSelectionModel().getSelectedIndex()))
            clientRef.get ! ClientManager.JoinSession(this.chatsessions(conversationList.getSelectionModel().getSelectedIndex()).id.toLong)
            Main.showChatRoomPage(true)
        }
    }

    //Delete Conversation by selecting the conversion and click delete icon
    def deleteConversation: Unit = {
        if (conversationList.selectionModel().selectedItem.value == null)
            alertError("Delete Fail", "Fail to delete conversation", "You must select one conversation")
        else{
            val confirm = alertConfirmation("Delete Confirmation", null, "Are you sure you want to delete this conversation?")
            if (confirm){
                val selectedItem = conversationList.selectionModel().selectedItem()
                clientRef.get ! ClientManager.DeleteSession(selectedItem.id)
            }
        }
    }

    //Detect changes in chat sessions list and update
    ClientManager.chatSessions.onChange{(ns, _) =>
        Platform.runLater {
            showConversationList()
        }
    }

    //Perform filter based on search text
    searchBar.text.onChange {
        if(searchBar.text.getValue() == ""){
            conversationList.items = chatsessions
            clearIcon.visible_=(false)
        } else{
            clearIcon.visible_=(true)
            val filtered = chatsessions.filter(x => x.name.contains(searchBar.text.getValue()))
            conversationList.items = filtered
        }
    }
}