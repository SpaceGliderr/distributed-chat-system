package model
import scala.util.Try
import util.Database
import scalikejdbc._
import model.{ User, ChatSession }
import java.util.UUID
import util.UserRoles

case class UserChatSession() extends Database {
    var id: Long = 0
    var userId: List[User] = List()
    var chatSessionId: List[ChatSession] = List()
    var role: UserRoles.UserRole = UserRoles.MEMBER

    def isExist: Boolean = {
        DB readOnly { implicit session =>
            sql"""
                select * from user_chat_sessions
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
                    insert into user_chat_sessions (user_id, chat_session_id, role)
                    values (${userId.map(_.id.value)}, ${chatSessionId.map(_.id.intValue())}, ${role.toString})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update user_chat_sessions
                    set user_id = ${userId.map(_.id.value)}, chat_session_id = ${chatSessionId.map(_.id.intValue())}, role = ${role.toString}
                    where id = ${id.intValue()}
                """.update().apply()
            })
        }
    }
}

object UserChatSession extends Database {
    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table user_chat_sessions (
                    id int not null,
                    user_id int,
                    chat_session_id int,
                    role varchar(64) check (role in ('ADMIN', 'MEMBER')),
                    foreign key (user_id) references users(id),
                    foreign key (chat_session_id) references chat_sessions(id),
                    primary key (id)
                )
            """.execute().apply()
        }
    }
}
