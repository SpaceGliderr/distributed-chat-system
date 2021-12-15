package chat.util
import scalikejdbc._
import chat.model.{ChatSession, Message, User, UserChatSession, Test}

trait Database {
    val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
    val dbURL = "jdbc:derby:ChatSystem;create=true;";

    // initialize JDBC driver & connection pool
    Class.forName(derbyDriverClassname)

    // user is username, 1234 is password -> create connection to database
    ConnectionPool.singleton(dbURL, "", "")

    // ad-hoc session provider on the REPL
    implicit val session: DBSession = AutoSession
}

object Database extends Database {
    val seed: Boolean = false
    val test: Boolean = false

    // create all tables needed
    def setupDB() = {
        // initialize the tables
        if (!hasDBInitialize("users"))
            User.initializeTable()

        if (!hasDBInitialize("chat_sessions"))
            ChatSession.initializeTable()

        if (!hasDBInitialize("messages"))
            Message.initializeTable()

        if (!hasDBInitialize("user_chat_sessions"))
            UserChatSession.initializeTable()

        if (seed) {
            User.seed()
            ChatSession.seed()
            Message.seed()
            UserChatSession.seed()
        }

        if (test) {
            Test.test
        }
    }

    //Check if  table is initialized in the database
    def hasDBInitialize(name: String) : Boolean = {
        DB getTable name match {
            case Some(x) => true
            case None => false
        }
    }
}

