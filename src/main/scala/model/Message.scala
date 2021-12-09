package model
import scala.util.Try
import util.Database
import scalikejdbc._
// import model.{ User, ChatSession }
import java.util.Date
import java.util.UUID

case class Message(_content: String, _senderId: String, _chatSessionId: String) extends Database {
    var id: Long = 0
    var content: String = _content
    var senderId: String = _senderId
    var chatSessionId: String = _chatSessionId
    var createdAt: Date = new Date()
    var updatedAt: Date = null // if we allow messages to be updated
    var deletedAt: Date = null // if we allow messages to be deleted

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
                    insert into messages (content, sender_id, chat_session_id, created_at, updated_at, deleted_at)
                    values (${content}, ${senderId}, ${chatSessionId}, ${createdAt}, ${updatedAt}, ${deletedAt})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update messages
                    set content = ${content}, sender_id = ${senderId}, chat_session_id = ${chatSessionId}, created_at = ${createdAt}, updated_at = ${updatedAt}, deleted_at = ${deletedAt}
                    where id = ${id.intValue()}
                """.update().apply()
            })
        }
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

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into messages (id, content, sender_id, chat_session_id)
                values 
                    (1, 'Hello everyone from Shi Qi', 2, 1),
                    (2, 'Hello everyone from Nick', 1, 1),
                    (3, 'Hello John from Shi Qi', 2, 2),
                    (4, 'Hello Shi Qi from John', 3, 2)
            """.update().apply()
        }
    }

    // def seed() = {
    //     DB autoCommit { implicit session =>
    //         sql"""
    //             insert into chat_session (id, content, sender_id, chat_session_id, created_at)
    //             values (1, 'Hello everyone from Shi Qi', 2, 1, now());

    //             insert into chat_session (id, content, sender_id, chat_session_id, created_at)
    //             values (2, 'Hello everyone from Nick', 1, 1, now());

    //             insert into chat_session (id, content, sender_id, chat_session_id, created_at)
    //             values (3, 'Hello John from Shi Qi', 2, 2, now());

    //             insert into chat_session (id, content, sender_id, chat_session_id, created_at)
    //             values (4, 'Hello Shi Qi from John', 3, 2, now());
    //         """.update().apply()
    //     }
    // }
}