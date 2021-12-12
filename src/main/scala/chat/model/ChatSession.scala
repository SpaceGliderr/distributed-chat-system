package chat.model

import scala.util.Try
import chat.util.{Database, UserRoles}
import scalikejdbc._
import java.util.Date

case class ChatSession(_name: String, _description: String, _creatorId: Long) {
    var id: Long = -1
    var name: String = _name
    var description: String = _description
    var creatorId: Long = _creatorId
    var createdAt: Date = null
    var updatedAt: Date = new Date()
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

    def upsert(): Try[Long] = {
        if (!isExist) {
            Try (DB autoCommit { implicit session =>
                id = sql"""
                    insert into chat_sessions (name, description, created_at)
                    values (${name}, ${description}, ${createdAt})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update chat_sessions
                    set name = ${name}, description = ${description}, updated_at = ${updatedAt}
                    where id = ${id.intValue}
                """.update().apply()
            })
        }
    }

    def create(): Try[Long] = {
        Try (
            DB autoCommit { implicit session =>
                id = sql"""
                    insert into chat_sessions (name, description, creator_id)
                    values (${name}, ${description}, ${creatorId})
                """.updateAndReturnGeneratedKey.apply()

                var userChatSession = new UserChatSession(creatorId, id.intValue, UserRoles.ADMIN)
                userChatSession.upsert()

                id.intValue
            }
        )
    }

    def update(): Try[Long] = {
        Try (
            DB autoCommit { implicit session =>
                sql"""
                    update chat_sessions
                    set name = ${name}, description = ${description}, updated_at = ${updatedAt}
                    where id = ${id.intValue}
                """.update().apply()
                id.intValue
            }
        )
    }

    def delete(): Try[Long] = {
        Try (
            DB autoCommit { implicit session =>
                sql"""
                    delete from chat_sessions where id = ${id.intValue}
                """.update().apply()
                id.intValue
            }
        )
    }

    override def toString = s"ChatSession(${id}, ${name}, creator:${creatorId})"
}

object ChatSession extends Database {
    def apply(_id: Long, _name: String, _description: String, _creatorId: Long, _createdAt: Date, _updatedAt: Date): ChatSession = {
        new ChatSession(_name, _description, _creatorId) {
            id = _id
            createdAt = _createdAt
            updatedAt = _updatedAt
        }
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table chat_sessions (
                    id int GENERATED ALWAYS AS IDENTITY,
                    name varchar(255) not null,
                    description varchar(255),
                    creator_id int not null,
                    created_at timestamp not null default current_timestamp,
                    updated_at timestamp,
                    primary key (id),
                    foreign key (creator_id) references users(id)
                )
            """.execute().apply()
        }
    }

    def findOne(id: Long): Option[ChatSession] = {
        DB readOnly { implicit session =>
            sql"""
                select * from chat_sessions
                where id = ${id.intValue()}
            """.map(res => ChatSession(
                res.long("id"),
                res.string("name"),
                res.string("description"),
                res.long("creator_id"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).single.apply()
        }
    }

    def getMessages(chatSessionId: Int): List[Message] = {
        DB readOnly { implicit session =>
            sql"""
                select * from messages
                where chat_session_id = ${chatSessionId}
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

    def selectAll: List[ChatSession] = {
        DB readOnly { implicit session =>
            sql"""
                select * from chat_sessions
            """.map(res => ChatSession(
                res.long("id"),
                res.string("name"),
                res.string("description"),
                res.long("creator_id"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).list.apply()
        }
    }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into chat_sessions (name, description, creator_id)
                values
                    ('general', 'general chat', 1),
                    ('private', 'private chat', 2)
            """.update().apply()
        }
    }
}
