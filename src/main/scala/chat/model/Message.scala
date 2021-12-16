package chat.model

import scala.util.Try
import chat.util.Database
import scalikejdbc._
import java.util.Date


case class Message(_content: String, _senderId: Long, _chatSessionId: Long) {

    // Properties
    var id: Long = 0
    var content: String = _content
    var senderId: Long = _senderId
    var chatSessionId: Long = _chatSessionId
    var createdAt: Date = null
    var updatedAt: Date = new Date()

    // Check if a message exists in database
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

    // Update or create a new record for message
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

    // Get the sender's name
    def senderUserName() : String = {
        DB readOnly { implicit session =>
        sql"""
            select username from users
            where id = ${senderId}
        """.map(rs => rs.string("username")).single.apply()
        } match {
            case Some(x) => x
            case None => "null"
        }
    }

    // toString method
    override def toString = s"${senderUserName}: ${content}"
}

object Message extends Database {

    def apply(_id: Long, _content: String, _senderId: Long, _chatSessionId: Long, _createdAt: Date, _updatedAt: Date): Message = {
        new Message(_content, _senderId, _chatSessionId) {
            id = _id
            createdAt = _createdAt
            updatedAt = _updatedAt
        }
    }

    // Initialize the table in database
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

    // Delete a message with id
    def deleteMessage(messageId: Long): Long = {
        DB autoCommit { implicit session =>
            sql"""
                delete from messages where id = ${messageId.intValue}
            """.update().apply()
        }
        messageId
    }

    // Select all messages in the database
    def selectAll: List[Message] = {
        DB readOnly { implicit session =>
            sql"""
                select * from messages
            """.map(res => Message(
                res.int("id"),
                res.string("content"),
                res.int("sender_id"),
                res.int("chat_session_id"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).list.apply()
        }
    }

    // Seeding
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