package util

import scalikejdbc._
import model.{ChatSession, Message, User, UserChatSession}


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

object Database extends Database{

    // create all tables needed
    def setupDB() = {
        // initialize the table todo_list
        // if (!hasDBInitialize("chat_user"))
        User.initializeTable()
        ChatSession.initializeTable()
        Message.initializeTable()
        UserChatSession.initializeTable()
    }


    def hasDBInitialize(name: String) : Boolean = {
        /**
        * check if  table is initialized in the database
        */
        DB getTable name match {
            case Some(x) => true
            case None => false
        }
    }
}

