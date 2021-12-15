package chat.model

import scala.util.{Try, Success, Failure}
import chat.util.Database
import scalikejdbc._
import java.time._
import java.util.Date


case class User(_username: String, _password: String) {

    // Properties
    var id: Long = -1
    var username: String = _username
    var password: String = _password
    var createdAt: Date = null
    var updatedAt: Date = new Date()

    // Check if a user exists in database
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

    // Check if a username has been used
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

    // Create a new user
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

    // Update or create a new record for user
    def upsert(): Try[Long] = {
        //  For new records, insert the new records into database
        if (!(isExist)){
            Try (DB autoCommit {
                implicit session =>
                    id = sql"""
                        insert into users(username, password)
                        values (${username}, ${password})
                    """.updateAndReturnGeneratedKey.apply()
                    id.intValue
            })
        // For existing records, update new information
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

    // toString method
    override def toString = s"${username}"

}

object User extends Database{

    def apply(_id: Long,  _username: String, _password: String, _createdAt: Date, _updatedAt: Date): User = {
        new User(_username, _password) {
            id = _id
            createdAt = _createdAt
            updatedAt = _updatedAt
        }
    }

    // Initialize the table in database
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

    // Check if a user exists in database by using username and password.
    // If exists, cerate a User object and return it.
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

    // Find the user with his/her id.
    def findOne(id: Long): Option[User] = {
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

    // Select and create all users in the database
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

    // Search a user by using the username containing the parameter s
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

    // Seeding
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
