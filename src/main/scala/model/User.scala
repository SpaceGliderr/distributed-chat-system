// User model for a chat user
// Build a User class that has the following properties: ID, username and password.
// The ID should be a UUID identifier for the user.
// The username should be a string.
// The password should be a string.

package model
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import scala.util.Try
import util.Database
import scalikejdbc._
// import jsr310._
import java.time._


class User(_uuid: String, _username: String, _password: String) extends Database {

    // properties
    var id = ObjectProperty[Long](-1)
    var uuid = new StringProperty(_uuid)
    var username = new StringProperty(_username)
    var password = new StringProperty(_password)
    val createdAt = new StringProperty(ZonedDateTime.now().toString) // only on initialization, not on update

    def isExist: Boolean = {
        DB readOnly {
            implicit session =>
                sql"""
                    select * from users
                    where id = ${id.value}
                """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    def upsert(): Try[Long] = {
        //  for new records, save it into database
        if (!(isExist)){
            Try (DB autoCommit {
                implicit session =>
                    id.value = sql"""
                        insert into users(uuid, username, password)
                        values (${uuid.value}, ${username.value}, ${password.value})
                    """.updateAndReturnGeneratedKey.apply()
                    id.value

            })

        // for existing records, update new information
        // ! if we don't have the function to edit user info, then DELETE this part
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update users
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
                create table users (
                    id int not null,
                    uuid varchar(64) not null,
                    username varchar(64),
                    password varchar(64),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    primary key (id)
                )
            """.execute.apply()

        }
    }

    def login(username: String, password: String): Option[User] = {
        DB readOnly { implicit session =>
            sql"""
                select * from users
                where username = ${username}
                and password = ${password}
            """.map(result => User(result.string("uuid"), result.string("username"), result.string("password"))).single.apply()
        }
    }

    def getUser(id: String): Option[User] = {
        DB readOnly { implicit session =>
            sql"""
                select * from users
                where uuid = ${id}
            """.map(result => User(result.string("uuid"), result.string("username"), result.string("password"))).single.apply()
        }
    }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into users (id, uuid, username, password)
                values 
                    (1, 'something', 'nick', '1234'),
                    (2, 'something', 'shi qi', '5678'),
                    (3, 'something', 'john', '9101')
            """.update().apply()
        }
    }
}
