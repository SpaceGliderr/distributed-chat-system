// User model for a chat user
// Build a User class that has the following properties: ID, username and password.
// The ID should be a UUID identifier for the user.
// The username should be a string.
// The password should be a string.

import scalafx.beans.property.{StringProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scala.util.Try
// import util.Database
import scalikejdbc._

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// @SerialVersionUID(100L)
case class User(_uuid: String, _username: String, _password: String) extends Database with Serializable{

    // properties
    var id: Long = -1
    var uuid: String = _uuid
    var username: String = _username
    var password: String = _password

    def isExist: Boolean = {

        DB readOnly {
            implicit session =>
                sql"""
                    select * from chat_user
                    where id = ${id}
                """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    def save(): Try[Long] = {

        //  for new records, save it into database
        if (!(isExist)){
            Try (DB autoCommit {
                implicit session =>
                    id = sql"""
                        insert into chat_user(uuid, username, password)
                        values (${uuid}, ${username}, ${password})
                    """.updateAndReturnGeneratedKey.apply()
                    id

            })

        // for existing records, update new information
        // ! if we don't have the function to edit user info, then DELETE this part
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update chat_user
                    set
                    username = ${username},
                    password = ${password}
                    where id = ${id}
                """.update.apply().toLong
            })
        }
    }

    // @throws(classOf[IOException])
    // def writeObject(output: ObjectOutputStream) = {
    //     output.defaultWriteObject()
    //     output.writeLong(id.value)
    //     output.writeUTF(uuid.value)
    //     output.writeUTF(username.value)
    //     output.writeUTF(password.value)
    // }

    // @throws(classOf[IOException])
    // def readObject(input: ObjectInputStream) = {
    //     input.defaultReadObject()
    //     id.value = input.readLong()
    //     uuid.value = input.readUTF()
    //     username.value = input.readUTF()
    //     password.value = input.readUTF()
    // }
}

object User extends Database{
    val users = new ObservableBuffer[User]()

    def apply(_id: Long, _uuid: String, _username: String, _password: String): User = {
        new User(_uuid, _username, _password){
            id = _id
        }
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table chat_user (
                    id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
                    uuid varchar(64) not null,
                    username varchar(64),
                    password varchar(64)
                )
            """.execute.apply()

        }
    }

    def selectAll: List[User] = {
        DB readOnly {
            implicit session =>
                sql"""select * from user""".map(r =>
                    User(r.int("id"), r.string("uuid"), r.string("username"), r.string("password"))
                ).list.apply()
        }
    }
}


// case class Session(id: String, topicName: String, messages: Array[String])
