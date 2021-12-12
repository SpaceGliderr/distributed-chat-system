package chat.view
import scalafxml.core.macros.sfxml
import javafx.scene.layout.ColumnConstraints
import scalafx.scene.control.{TextField, ListView, MenuItem, Dialog, ButtonType, Label}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import chat.Main
import scalafx.stage.Stage
import chat.util.AlertMessage
import scalafx.scene.layout.GridPane
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.SelectionMode

@sfxml
class NewChatOrGroupController(
    private val searchBar: TextField,
    private val imageView: ImageView,
    private val imageView1: ImageView,
    private val imageView2: ImageView,
    private val menuItem: MenuItem,
    private val contactList: ListView[String]   //-- not sure the type

) extends AlertMessage{
    val searchIcon = new Image(getClass().getResourceAsStream("searchIcon.png"))
    imageView.image_=(searchIcon)
    val plusIcon = new Image(getClass().getResourceAsStream("plusIcon.png"))
    imageView1.image_=(plusIcon)
    val backIcon = new Image(getClass().getResourceAsStream("backIcon.png"))
    imageView2.image_=(backIcon)

    var dialogStage: Stage = null
    var contacts: Array[String] = null  //-- not sure the type
    var title: String = ""

    //allow multiple selection
    contactList.selectionModel().setSelectionMode(SelectionMode.Multiple)

    // --
    // def showContactList() = {
    //      /* FOR ADD NEW CHAT: if the contact is already in contact list (the contacts variable), 
    //     if yes then disable the cell, but i think is a bit mafan to do this so maybe can jus remove 
    //     from contact list*/
    //
    //      /* FOR ADD NEW GROUP: no nid compare & the "contacts" array passed in will be null 
    // }

    def search(): Unit = {
        if (!searchBar.visible.value){
            searchBar.visible_=(true)
            searchBar.requestFocus()
        }
        else{
            //-- filter contact list

            searchBar.visible_=(false)
        }
    }

    def setMenuItemName(): Unit = {
        if (title == "Add New Chat")
            menuItem.text_=("Create Chat")
        else
            menuItem.text_=("Create Group")
    }

    def addNewContact(): Unit = {
        //dialog box, popup
        case class Result(contactName: String, contactNum: String)
        val dialog = new Dialog[Result]()
        dialog.initOwner(Main.stage)
        dialog.title_=("Add New Contact")
        dialog.headerText_=("Please enter the contact name and contact number")
        val imageView = new ImageView(getClass.getResource("addContact.png").toString())
        imageView.fitWidth_=(44)
        imageView.fitHeight_=(44)
        dialog.graphic_=(imageView)
        val saveButtonType: ButtonType = new ButtonType("Save", ButtonData.OKDone)
        dialog.dialogPane().buttonTypes = Seq(ButtonType.Cancel,saveButtonType)
        val grid: GridPane = new GridPane()
        grid.hgap_=(10)
        grid.vgap_=(10)
        grid.padding_=(Insets(20,150,10,10))
        grid.getColumnConstraints().add(new ColumnConstraints(127))
        grid.getColumnConstraints().add(new ColumnConstraints(210))
        val contactName: TextField = new TextField()
        val contactNum: TextField = new TextField()
        contactNum.promptText_=("0123456789 or 01234567890")
        grid.add(new Label("Contact Name: "), 0, 0)
        grid.add(contactName,1,0)
        grid.add(new Label("Contact Number: "),0,1)
        grid.add(contactNum,1,1)
        val saveButton: Node = dialog.dialogPane().lookupButton(saveButtonType)
        saveButton.disable_=(true)
        contactName.text.onChange{(_, _, newValue) => {
            if (contactNum.text.value.length != 0 && !newValue.trim().isEmpty){
                if ((contactNum.text.value.length == 10 || contactNum.text.value.length == 11) && (contactNum.text.value.substring(0,2) == "01"))
                    saveButton.disable_=(false)
                else
                    saveButton.disable_=(true)
            }
            else
                saveButton.disable_=(true)
            }
        }
        contactNum.text.onChange{(_, _, newValue1) => {
            if (contactName.text.value.length != 0 && !newValue1.trim().isEmpty){
                if ((newValue1.length == 10 || newValue1.length == 11) && (newValue1.substring(0,2) == "01"))
                    saveButton.disable_=(false)
                else
                    saveButton.disable_=(true)
            }
            } 
        }        
        dialog.dialogPane().content_=(grid)
        Platform.runLater(contactName.requestFocus())
        dialog.resultConverter = buttonType => 
            if (buttonType == saveButtonType)
                Result(contactName.text.value, contactNum.text.value)
            else
                null
        val result = dialog.showAndWait()
        result match{
            case Some(Result(name, num)) => //-- use the name and num to create contact obj? or user obj? & save to database
            case None => dialog.close()
        }
    }

    //================================ try run, remove later
    contacts = Array("1","2","3")
    val tryy = new ObservableBuffer[String]()
    tryy ++= contacts
    contactList.items = tryy
    //=================================

    def addNewChatOrGroup(): Unit = {
        if (title == "Add New Chat"){
            if (contactList.selectionModel().selectedItem.value == null) 
                alertError("Creation Fail", "Fail to create chat", "You must select one contact")
            else if (contactList.selectionModel().getSelectedIndices.length > 1)
                alertError("Creation Fail", "Fail to create chat", "You can only select one contact")
            else{
                dialogStage.close()
                Main.showChatRoomPage(null, null, false)   //-- pass name to chatroom page
            }
        }
        else {
            if (contactList.selectionModel().getSelectedIndices.length <= 1)
                alertError("Creation Fail", "Fail to create chat", "You must select at least two contacts")
            else{
                var grpName = textInputDialog("Create Group", "Create a group", "Please enter the group name: ")
                //-- create grp obj? or save the name?? ...
                if (grpName != ""){     //if grpname = "" means click cancel so wont load the chatroompage
                    dialogStage.close()
                    Main.showChatRoomPage(null, null, true)   //-- pass name to chatroom page
                }
            }
        }
    }
        
    def cancel: Unit = dialogStage.close()
}