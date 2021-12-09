package model
import scala.util.Try
import util.Database
import scalikejdbc._
import model.{ User, ChatSession }
import java.util.UUID
import java.util.Date
import util.UserRoles

case class UserChatSession(_userId: Long, _chatSessionId: Long, _role: UserRoles.UserRole = UserRoles.MEMBER) extends Database {
    var id: Long = 0
    var userId: Long = 0
    var chatSessionId: Long = 0
    var role: UserRoles.UserRole = _role
    var joinedAt: Date = new Date()

    def isExist: Boolean = {
        // TODO CHECK IF USER IS ALREADY IN CHAT
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
                    values (${userId.intValue()}, ${chatSessionId.intValue()}, ${role.toString})
                """.updateAndReturnGeneratedKey.apply()
                id.intValue
            })
        } else {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update user_chat_sessions
                    set user_id = ${userId.intValue()}, chat_session_id = ${chatSessionId.intValue()}, role = ${role.toString}
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
