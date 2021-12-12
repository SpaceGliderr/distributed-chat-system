// User model for a chat user
// Build a User class that has the following properties: ID, username and password.
// The username should be a string.
// The password should be a string.

package chat.model

import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scala.util.{ Try, Success, Failure }
import chat.util.Database
import scalikejdbc._
import java.time._
import java.util.Date


case class User(_username: String, _password: String) {

    // properties
    var id: Long = -1
    var username: String = _username
    var password: String = _password
    var createdAt: Date = null
    var updatedAt: Date = new Date()

    def isExist: Boolean = {
        DB readOnly {
            implicit session =>
                sql"""
                    select * from users
                    where id = ${id.intValue}
                """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    def isUserNameExist: Boolean = {
        DB readOnly{
            implicit session =>
                sql"""
                    select * from users
                    where username = ${username}
                """.map(result => result.int("id")).single.apply()
        } match {
            case Some(x) => true
            case None => false
        }
    }

    def create(): Try[Long] = {
        if((!isExist) & (!isUserNameExist)){
            Try (DB autoCommit {
                implicit session =>
                    id = sql"""
                        insert into users(username, password)
                        values (${username}, ${password})
                    """.updateAndReturnGeneratedKey.apply()
                    id.intValue
            })
        } else {
            Failure(new Exception("Unable to create user."))
        }
    }

    def upsert(): Try[Long] = {
        //  for new records, save it into database
        if (!(isExist)){
            Try (DB autoCommit {
                implicit session =>
                    id = sql"""
                        insert into users(username, password)
                        values (${username}, ${password})
                    """.updateAndReturnGeneratedKey.apply()
                    id.intValue
            })

        // for existing records, update new information
        // ! if we don't have the function to edit user info, then DELETE this part
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update users
                    set
                    username = ${username},
                    password = ${password}
                    where id = ${id.intValue}
                """.update.apply().toLong
            })
        }
    }

    override def toString = s"User(${id}, ${username}, ${password})"

}

object User extends Database{
    val users = new ObservableBuffer[User]()

    def apply(_id: Long,  _username: String, _password: String, _createdAt: Date, _updatedAt: Date): User = {
        new User(_username, _password) {
            id = _id
            createdAt = _createdAt
            updatedAt = _updatedAt
        }
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table users (
                    id int GENERATED ALWAYS AS IDENTITY,
                    username varchar(64) UNIQUE,
                    password varchar(64),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP,
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
            """.map(res => User(
                res.int("id"),
                res.string("username"),
                res.string("password"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).single.apply()
        }
    }

    def findOne(id: Int): Option[User] = {
        DB readOnly { implicit session =>
            sql"""
                select * from users
                where id = ${id}
            """.map(res => User(
                res.int("id"),
                res.string("username"),
                res.string("password"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).single.apply()
        }
    }

    def selectAll: List[User] = {
        DB readOnly { implicit session =>
            sql"""
                select * from users
            """.map(res => User(
                res.int("id"),
                res.string("username"),
                res.string("password"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).list.apply()
        }
    }

    def search(s: String): List[User] = {
        val q = s"%${s}%"
        DB readOnly { implicit session =>
            sql"""
                select * from users
                where username like ${q}
            """.map(res => User(
                res.int("id"),
                res.string("username"),
                res.string("password"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).list.apply()
        }
    }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into users (username, password)
                values
                    ('nick', '1234'),
                    ('shi qi', '5678'),
                    ('john', '9101')
            """.update().apply()
        }
    }
}
