package model
import scala.util.Try
import util.Database
import scalikejdbc._
// import model.{ User, ChatSession }
import java.util.Date
import java.util.UUID

case class Message(_content: String, _senderId: String, _chatSessionId: String) extends Database {
    var content: String = _content
    var senderId: String = _senderId
    var chatSessionId: String = _chatSessionId
    var createdAt: Date = new Date()
    var updatedAt: Date = null // if we allow messages to be updated
    var deletedAt: Date = null // if we allow messages to be deleted

    def save(): Try[Long] = {
        Try (DB autoCommit { implicit session =>
            sql"""
            """.update().apply()
        })
    }
}

object Message extends Database {
    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table messages (
                    id int not null,
                    content varchar(255),
                    sender_id int,
                    chat_session_id int,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT NULL,
                    deleted_at TIMESTAMP DEFAULT NULL,
                    foreign key (sender_id) references users(id),
                    foreign key (chat_session_id) references chat_sessions(id),
                    primary key (id)
                )
            """.execute().apply()
        }
    }

}