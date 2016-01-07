package chat.tox.antox.activities

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Toast, TextView, Button}
import chat.tox.antox.R
import chat.tox.antox.data.State
import chat.tox.antox.tox.MessageHelper
import chat.tox.antox.wrapper.{SelfKey, FriendKey, ContactKey, Message}
import chat.tox.antox.wrapper.MessageType._
import im.tox.tox4j.core.enums.ToxMessageType
import rx.lang.scala.Subscription

import scala.collection.mutable.ArrayBuffer
import chat.tox.Logging.GlobalLog

/**
 * Created by Ann on 12/31/15.
 */
class EvalActivity extends Activity with OnClickListener{
  protected var dislikeButton: Button = _
  protected var likeButton: Button = _
  protected var contactView: TextView = _
  protected var messageView: TextView = _
  protected var contactChangeSub: Subscription = _
  protected var activeKey: ContactKey = _
  protected var msgList: ArrayBuffer[Message] = _
  protected var index: Int = 0
  protected var msg: Message = _
  protected var contactString: String = _
  protected var messageString: String = _
  protected var msgId: Int = _
  protected var messageType: MessageType = _


  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_eval)
    //msgList.clear()
    //val tmpString:String = updateMessage()
    val toast = Toast.makeText(this, "hello", Toast.LENGTH_SHORT)
    toast.show()
    contactView = findViewById(R.id.title).asInstanceOf[TextView]
    messageView = findViewById(R.id.message).asInstanceOf[TextView]
    msgList = updateMessage()
    if (msgList.nonEmpty) {
      msg = msgList(index)
      contactString = msg.senderName
      messageString = msg.message
      msgId = msg.messageId
      messageType = msg.`type`
      contactView.setText(contactString)
      messageView.setText(messageString)
    }
    else {
      contactView.setText("")
      messageView.setText("No new messages")
    }

    dislikeButton = findViewById(R.id.dislikeButton).asInstanceOf[Button]
    likeButton = findViewById(R.id.likeButton).asInstanceOf[Button]
    dislikeButton.setOnClickListener(this)
    likeButton.setOnClickListener(this)
  }

  def onClick(view: View) {
    // display a new message
    if (msgList.nonEmpty) {
      // set messages as read
      val db = State.db
      db.setMessageEvaled(msgId)

      // forward or discard this message
      val friendList = db.getAllFriendKey()
      val numOfFriends = friendList.size
      val r = scala.util.Random
      val friendListIndex: Int = r.nextInt(numOfFriends)
      val toast = Toast.makeText(this, numOfFriends.toString(), Toast.LENGTH_SHORT)
      toast.show()
      val friendKey: FriendKey = friendList(friendListIndex)
      //val preferences = PreferenceManager.getDefaultSharedPreferences(this)
      //val toxId = preferences.getString("tox_id", "")
      view.getId() match {
        case R.id.likeButton => {
          MessageHelper.sendMessage(this, friendKey, messageString, ToxMessageType.NORMAL, None)
          // logging
          GlobalLog.log("2", friendKey.key)
        }
        case R.id.dislikeButton => {
          val toast = Toast.makeText(this, "Message Not Sent", Toast.LENGTH_SHORT)
          toast.show()
          // logging
          GlobalLog.log("3", friendKey.key)
        }
      }

      // display the next message, index points to the current message
      msgList.trimStart(1)
      //index += 1
      if (msgList.nonEmpty) {
        msg = msgList(index)
        contactString = msg.senderName
        messageString = msg.message
        msgId = msg.messageId
        messageType = msg.`type`
        contactView.setText(contactString)
        messageView.setText(messageString)
      }
      else {
        contactView.setText("")
        messageView.setText("No new messages")
      }
    }
  }

  // display all unread messages
  private def updateMessage(): ArrayBuffer[Message] = {
    val db = State.db
    val unreadMsg = db.unreadMsgList()
    unreadMsg
  }

}
