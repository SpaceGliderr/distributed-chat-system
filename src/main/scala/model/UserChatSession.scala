package model
import scala.util.Try
import util.Database
import scalikejdbc._
import model.{ User, ChatSession }
import java.util.UUID
import util.UserRoles

case class UserChatSession() extends Database {
    var id: String = ""
    var userId: List[User] = List()
    var chatSessionId: List[ChatSession] = List()
    var role: UserRoles.UserRole = UserRoles.MEMBER

    def save(): Try[Long] = {
        Try (DB autoCommit { implicit session =>
            sql"""
            """.update().apply()
        })
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
