package chat.tox.antox.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{TextView, Button, Toast}
import chat.tox.Logging.GlobalLog
import chat.tox.antox.R
import chat.tox.antox.data.State
import chat.tox.antox.tox.MessageHelper
import chat.tox.antox.wrapper.MessageType._
import chat.tox.antox.wrapper.{FriendKey, Message, ContactKey}
import im.tox.tox4j.core.enums.ToxMessageType
import rx.lang.scala.Subscription

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ann on 12/24/15.
 */
/*class EvalFragment extends android.support.v4.app.Fragment with View.OnClickListener{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_eval, container, false)
    val button = rootView.findViewById(R.id.evalFragButton)
    button.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        val intent: Intent = new Intent(getActivity, classOf[EvalActivity])
        startActivity(intent)
      }
    })
    rootView
  }

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  override def onClick(v: View): Unit = {

  }
}*/


class EvalFragment extends android.support.v4.app.Fragment with View.OnClickListener{
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

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.activity_eval, container, false)
    contactView = rootView.findViewById(R.id.title).asInstanceOf[TextView]
    messageView = rootView.findViewById(R.id.message).asInstanceOf[TextView]
    msgList = updateMessage()
    if (msgList.nonEmpty) {
      msg = msgList(index)
      contactString = msg.senderName
      messageString = msg.message
      msgId = msg.messageId
      messageType = msg.`type`
      //contactView.setText(contactString)
      contactView.setText("")
      messageView.setText(messageString)
    }
    else {
      contactView.setText("")
      messageView.setText("No new messages")
    }

    dislikeButton = rootView.findViewById(R.id.dislikeButton).asInstanceOf[Button]
    likeButton = rootView.findViewById(R.id.likeButton).asInstanceOf[Button]
    dislikeButton.setOnClickListener(this)
    likeButton.setOnClickListener(this)
    rootView
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
      val toast = Toast.makeText(getActivity(), numOfFriends.toString(), Toast.LENGTH_SHORT)
      toast.show()
      val friendKey: FriendKey = friendList(friendListIndex)
      //val preferences = PreferenceManager.getDefaultSharedPreferences(this)
      //val toxId = preferences.getString("tox_id", "")
      view.getId() match {
        case R.id.likeButton => {
          //db.addPrivateMessage(true, messageString)
          MessageHelper.sendMessage(getActivity(), friendKey, messageString, ToxMessageType.NORMAL, None)
          // logging
          GlobalLog.log("2", friendKey.key)
        }
        case R.id.dislikeButton => {
          val toast = Toast.makeText(getActivity(), "Message Not Sent", Toast.LENGTH_SHORT)
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
        //contactView.setText(contactString)
        contactView.setText("")
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

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }
}