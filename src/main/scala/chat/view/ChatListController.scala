package chat.view

import scalafxml.core.macros.sfxml
import scalafx.scene.control.{TextField, ListView}
import scalafx.Includes._
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
    private val pmIcon: ImageView,
    private val groupChatIcon: ImageView,
    private val openIcon: ImageView,
    private val deleteIcon: ImageView,
    private val conversationList: ListView[ChatSession]  //-- not sure the type

)extends AlertMessage{

    var clientRef: Option[ActorRef[ClientManager.Command]] = None

    // get images from resources
    val sIcon = new Image(getClass().getResourceAsStream("searchIcon.png"))
    val newChatIcon = new Image(getClass().getResourceAsStream("newChatIcon.png"))
    val newGroupIcon = new Image(getClass().getResourceAsStream("newGroupIcon.png"))
    val viewIcon = new Image(getClass().getResourceAsStream("viewChatIcon.png"))
    val dIcon = new Image(getClass().getResourceAsStream("deleteIcon.png"))

    // update the imageViews
    searchIcon.image_=(sIcon)
    pmIcon.image_=(newChatIcon)
    groupChatIcon.image_=(newGroupIcon)
    openIcon.image_=(viewIcon)
    deleteIcon.image_=(dIcon)


    val contacts: Array[String] = null
    /* -- not sure if is to use string,
    if not please chg the data type of
    1. Main's showNewChatOrNewGroupPage method's parameter's type
    2. NewChatOrGroupController's "contacts"" variable's type
    */

    //make the menu bar visible
    Main.roots.top.value.visible_=(true)

    var chatsessions = new ObservableBuffer[ChatSession]()

    // Populate the conversations in the table
    def showConversationList() = {
        this.chatsessions = new ObservableBuffer[ChatSession]()
        ClientManager.chatSessions.foreach(s => chatsessions += s)
        conversationList.items = this.chatsessions
    }

    ClientManager.chatSessions.onChange{(ns, _) =>
        println("chatsession changes")
        Platform.runLater {
            showConversationList()
        }
    }

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
        else{

        }
            //-- check if is group chat then pass in true to ChatRoomPage, else pass in false
            // if (--is group--)
            //     Main.showChatRoomPage(null, null, true)      //-- pass in messages and names
            // else
            //     Main.showChatRoomPage(null, null, false)     //-- pass in messages and names

            //============================try run, later delete
            // Main.showChatRoomPage(null, null, true)
            //===============================
            println(conversationList.getSelectionModel().getSelectedIndex())
            println(conversationList.selectionModel().selectedItem.value)
            // val sessionId = conversationList.selectionModel().selectedItem.value.id
            // val sessionId = this.chatsessions(conversationList.getSelectionModel().getSelectedIndex())
            Main.clientMain ! ClientManager.UpdateChatInfo(this.chatsessions(conversationList.getSelectionModel().getSelectedIndex()))
            Main.clientMain ! ClientManager.JoinSession(this.chatsessions(conversationList.getSelectionModel().getSelectedIndex()).id.toLong)
            // println(sessionId)
            Main.showChatRoomPage(true)
    }


    def deleteConversation: Unit = {
        if (conversationList.selectionModel().selectedItem.value == null)
            alertError("Delete Fail", "Fail to delete conversation", "You must select one conversation")
        else{
            val confirm = alertConfirmation("Delete Confirmation", null, "Are you sure you want to delete this conversation?")
            if (confirm){
                val selectedItem = conversationList.selectionModel().selectedItem()
                Main.clientMain ! ClientManager.DeleteSession(selectedItem.id)
                Thread.sleep(1000)
                Main.clientMain ! ClientManager.UpdateUser(ClientManager.user)
                showConversationList()
            }
        }
    }
}