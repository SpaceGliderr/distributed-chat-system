

import scalikejdbc._
// import model.{User}

trait Database {
    val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
    val dbURL = "jdbc:derby:ChatSystem;create=true;"

    // initialize JDBC driver & connection pool
    Class.forName(derbyDriverClassname)

    // create connection to database
    ConnectionPool.singleton(dbURL, "", "")

    // ad-hoc session provider on the REPL
    implicit val session: DBSession = AutoSession
}

object Database extends Database{

    // create all tables needed
    def setupDB() = {
        if (!hasDBInitialize("chat_user"))
            User.initializeTable()
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

