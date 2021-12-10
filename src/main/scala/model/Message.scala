package model
import scala.util.Try
import util.Database
import scalikejdbc._
// import model.{ User, ChatSession }
import java.util.Date
import java.util.UUID

case class Message(_content: String, _senderId: Long, _chatSessionId: Long) extends Database {
    var id: Long = 0
    var content: String = _content
    var senderId: Long = _senderId
    var chatSessionId: Long = _chatSessionId
    var createdAt: Date = null
    var updatedAt: Date = new Date() // if we allow messages to be updated

    def isExist: Boolean = {
        DB readOnly { implicit session =>
            sql"""
                select * from messages
                where id = ${id.intValue()}
            """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    def upsert(): Try[Long] = {
        if (!isExist) {
            Try (DB autoCommit { implicit session =>
                id = sql"""
                    insert into messages (content, sender_id, chat_session_id, created_at, updated_at)
                    values (${content}, ${senderId}, ${chatSessionId}, ${createdAt}, ${updatedAt})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update messages
                    set content = ${content}, sender_id = ${senderId}, chat_session_id = ${chatSessionId}, updated_at = ${updatedAt}
                    where id = ${id.intValue()}
                """.update().apply()
            })
        }
    }
}

object Message extends Database {
    def apply(_id: Long, _content: String, _senderId: Long, _chatSessionId: Long, _createdAt: Date, _updatedAt: Date): Message = {
        new Message(_content, _senderId, _chatSessionId) {
            id = _id
            createdAt = _createdAt
            updatedAt = _updatedAt
        }
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table messages (
                    id int GENERATED ALWAYS AS IDENTITY,
                    content varchar(255),
                    sender_id int,
                    chat_session_id int,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT NULL,
                    foreign key (sender_id) references users(id),
                    foreign key (chat_session_id) references chat_sessions(id),
                    primary key (id)
                )
            """.execute().apply()
        }
    }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into messages (content, sender_id, chat_session_id)
                values 
                    ('Hello everyone from Shi Qi', 2, 1),
                    ('Hello everyone from Nick', 1, 1),
                    ('Hello John from Shi Qi', 2, 2),
                    ('Hello Shi Qi from John', 3, 2)
            """.update().apply()
        }
    }
}