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

    def joinSession(): Try[Long] = {
        Try (DB autoCommit { implicit session =>
            id = sql"""
                insert into user_chat_sessions (user_id, chat_session_id, role)
                values (${userId.intValue()}, ${chatSessionId.intValue()}, ${role.toString})
            """.updateAndReturnGeneratedKey.apply()
            id.intValue
        })
    }

    def leaveSession(): Try[Long] = {
        Try (DB autoCommit { implicit session =>
            sql"""
                delete from user_chat_sessions where user_id = ${userId.intValue()} and chat_session_id = ${chatSessionId.intValue()}
            """.update().apply()
    }
}

object UserChatSession extends Database {
    // Get Users in ChatSession with ID x
    // def getUsersInChatSession(chatSessionId: Long): Try[List[User]] = {
    //     Try (DB readOnly { implicit session =>
            // TODO: Problems with list conversion at the end
            // var users = sql"""
            //     select u.* from users u
            //     join user_chat_sessions ucs on u.id = ucs.user_id
            //     where ucs.chat_session_id = ${chatSessionId.intValue()}
            // """.map(result => User(result.long("id"), result.string("username"), result.string("email"), result.string("password"), result.string("first_name"), result.string("last_name"), result.string("avatar_url"), result.string("role"))).list.apply()
            // users
    //     })
    // }

    // Get Users not in ChatSession with ID x
    // def getUsersNotInChatSession(chatSessionId: Long): Try[List[User]] = {
    //     Try (DB readOnly { implicit session =>
            // TODO: Problems with list conversion at the end
            // var users = sql"""
            //     select u.* from users u
            //     left join user_chat_sessions ucs on ucs.user_id = u.id
            //     where ucs.chat_session_id = ${chatSessionId.intValue()}
            // """.map(result => User.fromResultSet(result)).list.apply()
            // users
    //     })
    // }

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
