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
    private val searchIcon: ImageView,
    private val plusIcon: ImageView,
    private val backIcon: ImageView,
    private val createButton: Button,
    private val contactList: ListView[User]   //-- not sure the type

) extends AlertMessage{

    //Variables
    var clientRef: Option[ActorRef[ClientManager.Command]] = None
    var dialogStage: Stage = null
    var title: String = ""
    var names = new ObservableBuffer[User]()

    //Get images from resources
    val searchIconImage = new Image(getClass().getResourceAsStream("searchIcon.png"))
    val plusIconImage = new Image(getClass().getResourceAsStream("plusIcon.png"))
    val backIconImage = new Image(getClass().getResourceAsStream("backIcon.png"))
    var isGroup = false

    //Update the imageViews
    searchIcon.image_=(searchIconImage)
    plusIcon.image_=(plusIconImage)
    backIcon.image_=(backIconImage)

    //allow multiple selection
    contactList.selectionModel().setSelectionMode(SelectionMode.Multiple)

    //Populate the user lists
    def updateContactList() = {
        names.clear()
        if (isGroup)
            ClientManager.users.foreach( u => names += u)
        else
            ClientManager.pmUsers.foreach( u => names += u)

        contactList.items = names
    }

    //Show search bar
    def showSearchBar(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            searchBar.text = ""
            searchBar.visible_=(false)
        }
    }

    //Create chat session
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
                    val selectedItems = contactList.selectionModel().getSelectedItems()
                    var ids: Array[Long] = Array()
                    selectedItems.foreach(i => ids = ids :+ i.id)
                    clientRef.get ! ClientManager.CreateSession(ids, groupName)
                    dialogStage.close()
                    Main.showChatRoomPage(true)
                }
            }
        }
    }

    //Cancel operation and close window
    def cancel: Unit = dialogStage.close()

    //Perform filter based on search text
    searchBar.text.onChange {
        if(searchBar.text.getValue() == ""){
            contactList.items = names
        } else{
            val filtered = names.filter(x => x.username.contains(searchBar.text.getValue()))
            contactList.items = filtered
        }
    }

    //Detect changes and update
    ClientManager.users.onChange {
        Platform.runLater {
            updateContactList()
        }
    }
    ClientManager.pmUsers.onChange {
        Platform.runLater {
            updateContactList()
        }
    }

}