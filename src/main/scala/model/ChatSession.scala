package model
import scala.util.Try
import util.Database
import scalikejdbc._
import model.{ User, Message }
import java.util.Date
import java.util.UUID
import util.UserRoles

case class ChatSession(_name: String, _description: String, _creatorId: Int) extends Database {
    var id: Long = 0
    var name: String = _name
    var description: String = _description
    var creatorId: Int = _creatorId
    var createdAt: Date = new Date()
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
                    insert into chat_sessions (name, description, creatorId, created_at)
                    values (${name}, ${description}, ${creatorId}, ${createdAt})
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
}

object ChatSession extends Database {
    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table chat_sessions (
                    id int not null,
                    name varchar(255) not null,
                    description varchar(255),
                    creatorId int not null,
                    created_at timestamp not null default current_timestamp,
                    updated_at timestamp,
                    primary key (id),
                    foreign key (creatorId) references users(id)
                )
            """.execute().apply()
        }
    }

    // TODO Problem with Lists
    // def getMessages(chatSessionId: Int): List[Message] = {
    //     DB readOnly { implicit session =>
    //         sql"""
    //             select * from messages
    //             where chatSessionId = ${chatSessionId}
    //         """.map(result => Message(result.int("id"), result.string("content"), result.int("chatSessionId"), result.int("userId"), result.timestamp("created_at"))).list.apply()
    //     }
    // }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into chat_sessions (id, name, description, creatorId)
                values 
                    (1, 'general', 'general chat', 1), 
                    (2, 'private', 'private chat', 2)
            """.update().apply()
        }
    }
}
