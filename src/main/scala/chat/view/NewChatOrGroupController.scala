package chat.view

import scalafxml.core.macros.sfxml
import javafx.scene.layout.ColumnConstraints
import scalafx.scene.control.{TextField, ListView, MenuItem, Dialog, ButtonType, Label, Button}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import chat.{Main, ClientManager}
import scalafx.stage.Stage
import chat.util.AlertMessage
import scalafx.scene.layout.GridPane
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.SelectionMode
import chat.model.User
import akka.actor.typed.ActorRef

@sfxml
class NewChatOrGroupController(
    private val searchBar: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val createButton: Button,
    private val contactList: ListView[User]   //-- not sure the type

) extends AlertMessage{

    var clientRef: Option[ActorRef[ClientManager.Command]] = None

    val searchIcon = new Image(getClass().getResourceAsStream("searchIcon.png"))
    val plusIcon = new Image(getClass().getResourceAsStream("plusIcon.png"))
    val backIcon = new Image(getClass().getResourceAsStream("backIcon.png"))
    var isGroup = false

    imageView.image_=(searchIcon)
    imageView1.image_=(plusIcon)
    imageView2.image_=(backIcon)


    var dialogStage: Stage = null
    var contacts: Array[String] = null  //-- not sure the type
    var title: String = ""

    //allow multiple selection
    contactList.selectionModel().setSelectionMode(SelectionMode.Multiple)

    var names = new ObservableBuffer[User]()


    searchBar.text.onChange {
        if(searchBar.text.getValue() == ""){
            contactList.items = names
        } else{
            val filtered = names.filter(x => x.username.contains(searchBar.text.getValue()))
            contactList.items = filtered
        }
    }


    // populate the user lists
    def updateContactList() = {
        names = new ObservableBuffer[User]()

        if (isGroup)
            ClientManager.users.foreach( u => names += u)
        else
            ClientManager.pmUsers.foreach( u => names += u)

        contactList.items = names
    }

    def search(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            //-- filter contact list
            searchBar.text = ""
            searchBar.visible_=(false)
        }
    }

    def addNewChatOrGroup(): Unit = {
        // Private Messages
        if (title == "Add New Chat"){
            if (contactList.selectionModel().selectedItem.value == null)
                alertError("Creation Fail", "Fail to create chat", "You must select one contact")
            else if (contactList.selectionModel().getSelectedIndices.length > 1)
                alertError("Creation Fail", "Fail to create chat", "You can only select one contact")
            else{
                val selectedItem = contactList.selectionModel().selectedItem()
                clientRef.get ! ClientManager.CreateSession(Array(selectedItem.id), selectedItem.username)
                ClientManager.pmUsers = ClientManager.pmUsers.filter(_ != selectedItem)
                dialogStage.close()
                Main.showChatRoomPage(false)
            }
        }
        // Group Chat
        else {
            if (contactList.selectionModel().getSelectedItems.length <= 1)
                alertError("Creation Fail", "Fail to create chat", "You must select at least two contacts")

            else{
                var groupName = textInputDialog("Create Group", "Create a group", "Please enter the group name: ")

                if (groupName != ""){
                    //if grpname = "" means click cancel so wont load the chatroompage
                    val selectedItems = contactList.selectionModel().getSelectedItems()

                    var ids: Array[Long] = Array()
                    selectedItems.foreach(i => ids = ids :+ i.id)

                    clientRef.get ! ClientManager.CreateSession(ids, groupName)

                    dialogStage.close()
                    Main.showChatRoomPage(true)   //-- pass name to chatroom page
                }
            }
        }
    }

    def cancel: Unit = dialogStage.close()



}