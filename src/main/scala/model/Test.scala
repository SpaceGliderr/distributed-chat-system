package model
import model._
import util.UserRoles
import scala.util.Try

object Test {
    def test = {
        // USER QUERY TESTS
        testLogin
        testFindOne
        testSelectAll
        testSearch

        // CHAT SESSION TESTS
        testCSFindOne
        testGetMessages

        // USER CHAT SESSION TESTS
        testUsersInChatSession
        testUsersNotInChatSession

        // TEST USER INSERTS AND UPDATES
        testUserCreate
        testUserUpdate

        // TEST CHAT SESSION, USER CHAT SESSION, MESSAGES INSERTS AND UPDATES
        testChatSessionCreate
        testChatSessionUpdate
        testUserChatJoinSession
        testUserChatSessionUpdateRole
        testMessageCreate
        testLeaveSession
    }

    // USER TESTS
    def testLogin = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val user = User.login("nick", "1234")
        println(s"USER RETRIEVED >>>>>>>> ${user}")
    }

    def testFindOne = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val user = User.findOne(1)
        println(s"USER RETRIEVED >>>>>>>> ${user}")
    }

    def testSelectAll = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val users = User.selectAll
        println(s"USER RETRIEVED >>>>>>>> ${users}")
    }

    def testSearch = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val user = User.search("shi")
        println(s"USER RETRIEVED >>>>>>>> ${user}")
    }

    // CHAT SESSION TESTS
    def testCSFindOne = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val chatSession = ChatSession.findOne(1)
        println(s"CHAT SESSION RETRIEVED >>>>>>>> ${chatSession}")
    }

    def testGetMessages = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val messages = ChatSession.getMessages(1)
        println(s"MESSAGES RETRIEVED >>>>>>>> ${messages}")
    }

    // USER CHAT SESSION TESTS
    def testUsersInChatSession = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val users = UserChatSession.getUsersInChatSession(1)
        println(s"USERS RETRIEVED >>>>>>>> ${users}")
    }

    def testUsersNotInChatSession = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val users = UserChatSession.getUsersNotInChatSession(2)
        println(s"USERS RETRIEVED >>>>>>>> ${users}")
    }

    // TEST USER INSERTS AND UPDATES
    def testUserCreate: Long = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val user = new User("someone new", "1234")
        val userId = user.upsert.get
        println(s"USER ID RETRIEVED >>>>>>>> ${userId}")
        userId
    }

    def testUserUpdate = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val user = new User("updated name", "1234") {
            id = 2
        }
        println(s"UPDATED USER ID RETRIEVED >>>>>>>> ${user.upsert}")
    }

    // TEST CHAT SESSION, USER CHAT SESSION, MESSAGES INSERTS AND UPDATES
    def testChatSessionCreate = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val chatSession = new ChatSession("new chat", "newly created chat", 1)
        val chatSessionId: Long = chatSession.create.get
        println(s"CHAT SESSION ID RETRIEVED >>>>>>>> ${chatSessionId}")
    }

    def testChatSessionUpdate = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val chatSession = new ChatSession("updated chat name", "updated chat desc", 1) {
            id = 2
        }
        val chatSessionId: Long = chatSession.update.get
        println(s"UPDATED CHAT SESSION ID RETRIEVED >>>>>>>> ${chatSessionId}")
    }

    def testUserChatJoinSession = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val userChatSession = new UserChatSession(1, 2)
        val userChatSessionId = userChatSession.joinSession.get
        println(s"USER CHAT SESSION ID RETRIEVED >>>>>>>> ${userChatSessionId}")
    }

    def testUserChatSessionUpdateRole = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val userChatSession = new UserChatSession(3, 2, UserRoles.ADMIN) {
            id = 5
        }
        val userChatSessionId: Long = userChatSession.updateRole.get
        println(s"UPDATED USER CHAT SESSION ID RETRIEVED >>>>>>>> ${userChatSessionId}")
    }

    def testMessageCreate = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val message = new Message("testing messageeeee", 1, 2)
        val messageId = message.upsert.get
        println(s"MESSAGE ID RETRIEVED >>>>>>>> ${messageId}")
    }

    def testLeaveSession = {
        println(s"| CURRENTLY TESTING ${currentMethodName} |")
        val userChatSession = UserChatSession.leaveSession(1, 2)
        println(s"USER CHAT SESSION RETRIEVED >>>>>>>> ${userChatSession}")
    }

    def currentMethodName() : String = Thread.currentThread.getStackTrace()(2).getMethodName
}