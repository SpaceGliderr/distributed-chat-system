package chat.model

import scala.util.Try
import chat.util.{Database, UserRoles}
import scalikejdbc._
import java.util.UUID
import java.util.Date

case class UserChatSession(_userId: Long, _chatSessionId: Long, _role: UserRoles.UserRole = UserRoles.MEMBER) {
    var id: Long = 0
    var userId: Long = _userId
    var chatSessionId: Long = _chatSessionId
    var role: UserRoles.UserRole = _role
    var joinedAt: Date = new Date()

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

    def inChat: Boolean = {
        DB readOnly { implicit session =>
            sql"""
                select * from user_chat_sessions
                where user_id = ${userId.intValue()} and chat_session_id = ${chatSessionId.intValue()}
            """.map(result => result.int("id")).single.apply()

        } match {
            case Some(x) => true
            case None => false
        }
    }

    // ! Not sure whether we need upsert for this one or maybe upsert can be updateRole?
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

    def updateRole(): Try[Long] = {
        if (inChat) {
            Try (DB autoCommit { implicit session =>
                sql"""
                    update user_chat_sessions
                    set role = ${role.toString}
                    where user_id = ${userId.intValue()} and chat_session_id = ${chatSessionId.intValue()}
                """.update().apply()
                id.intValue
            })
        } else {
            joinSession
            Try(id)
        }
    }

    override def toString = s"UserChatSession(${id}, user:${userId}, chatSession:${chatSessionId})"
}

object UserChatSession extends Database {
    def apply(_id: Long, _userId: Long, _chatSessionId: Long, _role: UserRoles.UserRole, _joinedAt: Date): UserChatSession = {
        new UserChatSession(_userId, _chatSessionId, _role) {
            id = _id
            joinedAt = _joinedAt
        }
    }

    // Get Users in ChatSession with ID x
    def getUsersInChatSession(chatSessionId: Long): List[User] = {
        DB readOnly { implicit session =>
            sql"""
                select u.* from users u
                join user_chat_sessions ucs on u.id = ucs.user_id
                where ucs.chat_session_id = ${chatSessionId.intValue()}
            """.map(res => User(
                res.int("id"),
                res.string("username"),
                res.string("password"),
                res.timestamp("created_at"),
                res.timestamp("updated_at")
            )).list.apply()
        }
    }

    // Get Users not in ChatSession with ID x
    def getUsersNotInChatSession(chatSessionId: Long): List[User] = {
        val users = User.selectAll
        val usersInSession = getUsersInChatSession(chatSessionId).toSet
        users.filterNot(usersInSession)
    }

    def leaveSession(userId: Long, chatSessionId: Long): Long = {
        DB autoCommit { implicit session =>
            sql"""
                delete from user_chat_sessions where user_id = ${userId.intValue()} and chat_session_id = ${chatSessionId.intValue()}
            """.update().apply()
        }
        userId
    }

    def initializeTable() = {
        DB autoCommit { implicit session =>
            sql"""
                create table user_chat_sessions (
                    id int GENERATED ALWAYS AS IDENTITY,
                    user_id int,
                    chat_session_id int,
                    role varchar(64) check (role in ('ADMIN', 'MEMBER')),
                    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    foreign key (user_id) references users(id),
                    foreign key (chat_session_id) references chat_sessions(id),
                    primary key (id)
                )
            """.execute().apply()
        }
    }


    def selectAll: List[UserChatSession] = {
        DB readOnly { implicit session =>
            sql"""
                select * from user_chat_sessions
            """.map(res => UserChatSession(
                res.int("id"),
                res.int("user_id"),
                res.int("chat_session_id"),
                if (res.string("role") == "ADMIN") UserRoles.ADMIN else UserRoles.MEMBER,
                res.timestamp("joined_at")
            )).list.apply()
        }
    }

    def seed() = {
        DB autoCommit { implicit session =>
            sql"""
                insert into user_chat_sessions (user_id, chat_session_id, role)
                values
                    (1, 1, 'ADMIN'),
                    (2, 1, 'MEMBER'),
                    (3, 1, 'MEMBER'),
                    (2, 2, 'ADMIN'),
                    (3, 2, 'MEMBER')
            """.update().apply()
        }
    }
}