package model
import scala.util.Try
import util.Database
import scalikejdbc._
import model.{ User, Message }
import java.util.Date
import java.util.UUID

case class ChatSession(_name: String, _description: String) extends Database {
    var id: Long = 0
    var name: String = _name
    var description: String = _description
    var createdAt: Date = new Date()
    var updatedAt: Date = null // if we allow messages to be updated
    var deletedAt: Date = null // if we allow messages to be deleted
    var messages: List[Message] = List() // message log

    def isExist: Boolean = {
        DB readOnly { implicit session =>
            sql"""
                select * from chat_sessions
                where id = ${id.intValue()}
            """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    def save(): Try[Long] = {
        if (!isExist) {
            Try (DB autoCommit { implicit session =>
                id = sql"""
                    insert into chat_session (name, description, created_at, updated_at, deleted_at)
                    values (${name}, ${description}, ${createdAt}, ${updatedAt}, ${deletedAt})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update chat_session
                    set name = ${name}, description = ${description}, updated_at = ${updatedAt}
                    where id = ${id.intValue}
                """.update().apply()
            })
        }
    }
}

object ChatSession extends Database {
    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table chat_sessions (
                    id int not null,
                    name varchar(255) not null,
                    description varchar(255),
                    created_at timestamp not null,
                    updated_at timestamp,
                    deleted_at timestamp,
                    primary key (id)
                )
            """.execute().apply()
        }
    }
}
