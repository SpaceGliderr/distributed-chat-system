// User model for a chat user
// Build a User class that has the following properties: ID, username and password.
// The ID should be a UUID identifier for the user.
// The username should be a string.
// The password should be a string.

package model
import scalafx.beans.property.{StringProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scala.util.Try
import util.Database
import scalikejdbc._


class User(_uuid: String, _username: String, _password: String) extends Database{

    // properties
    var id = ObjectProperty[Long](-1)
    var uuid = new StringProperty(_uuid)
    var username = new StringProperty(_username)
    var password = new StringProperty(_password)

    def isExist: Boolean = {

        DB readOnly {
            implicit session =>
                sql"""
                    select * from chat_user
                    where id = ${id.value}
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
                    id.value = sql"""
                        insert into chat_user(uuid, username, password)
                        values (${uuid.value}, ${username.value}, ${password.value})
                    """.updateAndReturnGeneratedKey.apply()
                    id.value

            })

        // for existing records, update new information
        // ! if we don't have the function to edit user info, then DELETE this part
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update chat_user
                    set
                    username = ${username.value},
                    password = ${password.value}
                    where id = ${id.value}
                """.update.apply().toLong
            })
        }
    }
}

object User extends Database{
    val users = new ObservableBuffer[User]()

    def apply(_id: String, _username: String, _password: String): User = {
        new User(_id, _username, _password)
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table chat_user (
                    id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
                    uuid UUID not null,
                    username varchar(64),
                    password varchar(64)
                )
            """.execute.apply()

        }
    }
}


// case class Session(id: String, topicName: String, messages: Array[String])
