// User model for a chat user
// Build a User class that has the following properties: ID, username and password.
// The ID should be a UUID identifier for the user.
// The username should be a string.
// The password should be a string.

package models

case class User(id: String, username: String, password: String)
case class Session(id: String, topicName: String, messages: Array[String])
