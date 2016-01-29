package chat.tox.antox.tox

import java.sql.Timestamp
import java.util.Calendar

import android.content.Context
import android.os.Handler
import chat.tox.Logging.GlobalLog
import chat.tox.antox.activities.{ChatActivity, GroupChatActivity}
import chat.tox.antox.data.State
import chat.tox.antox.utils._
import chat.tox.antox.wrapper._
import im.tox.tox4j.core.data.{ToxNickname, ToxFriendMessage}
import im.tox.tox4j.core.enums.ToxMessageType
import org.scaloid.common.LoggerTag

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object MessageHelper {

  val TAG = LoggerTag("MessageHelper")
  val initalSendTime: Int = 50
  val sendInterval: Int = 50
  val msgLife: Int = 86400000
  val waitingReplyTime: Int = initalSendTime

  /*def handleMessage(ctx: Context, friendInfo: FriendInfo, message: ToxFriendMessage, messageType: ToxMessageType): Unit = {
    val db = State.db

    AntoxLog.debug(s"Message from: friend id: ${friendInfo.key} activeKey: ${State.activeKey} chatActive: ${State.chatActive}", TAG)

    if (!db.isContactBlocked(friendInfo.key)) {
      val chatActive = State.isChatActive(friendInfo.key)
      db.addMessage(friendInfo.key, friendInfo.key, ToxNickname.unsafeFromValue(friendInfo.getDisplayName.getBytes), new String(message.value), hasBeenReceived = true,
        hasBeenRead = chatActive, successfullySent = true, messageType)
      // update private message database
      db.addPrivateMessage(false, new String(message.value),1,"UP",Timestamp.valueOf("5000"), Timestamp.valueOf("5000"),FriendKey(ToxSingleton.tox.getSelfKey.toString),true)
      // logging, receive a message
      GlobalLog.log("1", friendInfo.key.key)

      if (!chatActive) {
        val unreadCount = db.getUnreadCounts(friendInfo.key)
        AntoxNotificationManager.createMessageNotification(ctx, classOf[ChatActivity], friendInfo.key, friendInfo.name, new String(message.value), unreadCount)
      }
    }
  }*/

  // Handle message with metadata
  def handleMessage(ctx: Context, friendInfo: FriendInfo, message: ToxFriendMessage, messageType: ToxMessageType): Unit = {
    val db = State.db
    val calendar = java.util.Calendar.getInstance()
    val currentTimestamp: Timestamp = new Timestamp(calendar.getTime.getTime)
    AntoxLog.debug("Begin to handle message", TAG)
    val parsedMsg = parseMsg(new String(message.value))
    var msgContent: String = parsedMsg.last

    if (parsedMsg(0).equals("")) {
      db.addPrivateMessage(false, msgContent, 0, "DOWN", currentTimestamp, currentTimestamp, friendInfo.key, false)
      db.addMessage(friendInfo.key, friendInfo.key, friendInfo.name, msgContent, true, false, true, messageType)
    }
    else {
      AntoxLog.debug("Enter first condition", TAG)
      val msgType: String = parsedMsg(0).trim
      var msgTypeNum: Int = 0
      val hop = parsedMsg(1).toInt
      val updown = parsedMsg(2)
      val spreadtime = Timestamp.valueOf(parsedMsg(3))
      val exptime = Timestamp.valueOf(parsedMsg(4))
      val parent = FriendKey(parsedMsg(5))
      val active = parsedMsg(6).toBoolean

      if(msgType.equals("Regular")) {
        AntoxLog.debug(s"MsgType is regular.", TAG)
        msgTypeNum = 1
      }
      else if (msgType.equals("AskBeacon")) {
        AntoxLog.debug(s"MsgType is AskBeacon.", TAG)
        msgTypeNum = 2
      }
      else if (msgType.equals("ReplyBeaconN")) {
        msgTypeNum = 3
      }

      msgTypeNum match {
        case 1 => {
          AntoxLog.debug(s"MsgType is regular.", TAG)
          //db.addPrivateMessage(false, "The Regular message exists", hop, updown, spreadtime, exptime, parent, active)
          //db.addMessage(friendInfo.key, friendInfo.key, friendInfo.name, msgContent, true, false, true, messageType)
          val isExist: Boolean = db.getNewPrivateMsgList(msgContent, exptime)
          if (isExist) {
            // db.updatePrivateMessage(id, hop, updown, spreadtime, exptime, parent, active)
            // why update ?
            AntoxLog.debug("Msg already exists.", TAG)
            }
          // if the message is new
          else {
            AntoxLog.debug("Msg does not exist, added to the database.", TAG)
            db.addPrivateMessage(false, msgContent, hop, updown, spreadtime, exptime, parent, active)
            db.addMessage(friendInfo.key, friendInfo.key, ToxNickname.unsafeFromValue(friendInfo.getDisplayName.getBytes), new String(msgContent), hasBeenReceived = true,
              hasBeenRead = false, successfullySent = true, messageType)
            GlobalLog.log("1", friendInfo.key.key)
          }

          if (exptime.after(currentTimestamp)) {
            if (updown == "UP") {
              val nextSource = chooseNextSource(parent)
              val handlerOne = new Handler()
              var delay = spreadtime.getTime - currentTimestamp.getTime
              if (delay < 0) {delay = 0}
              val runnableOne = new Runnable() {
                override def run(): Unit = {
                  handlerOne.removeCallbacks(this)
                  sendMessageToSource(nextSource, hop, updown, spreadtime, exptime, msgContent)
                  for (nonSource <- ToxSingleton.tox.getFriendList) {
                    if (nonSource != nextSource && nonSource != parent) {
                      sendMessageToNonSource(nonSource, hop, updown, spreadtime, exptime, active, msgContent)
                    }
                  }
                }
              }
              handlerOne.postDelayed(runnableOne, delay)
              //db.removePendingMsg(id)
            }
            if (updown=="DOWN" && active==true && hop>0) {
              sendBeacon(parsedMsg.slice(1,8))
              db.addPendingMessage("None", msgContent, hop, updown, spreadtime, exptime, active)
              var sourceString = ""
              var id: Int = 0
              val handlerTwo = new Handler()
              var delay = spreadtime.getTime - currentTimestamp.getTime
              if (delay < 0) {delay = 0}
              val runnableTwo = new Runnable() {
                override def run(): Unit = {
                  handlerTwo.removeCallbacks(this)
                  val idBuffer = db.getPendingMsg(msgContent)
                  id = idBuffer.head
                  sourceString = db.getPendingSource(id)

                  if (sourceString != "None") {
                    val sourceList = sourceString.split("&")
                    for (source <- sourceList) {
                      sendMessageToNonSource(friendInfo.key, hop, updown, spreadtime, exptime, active, msgContent)
                    }
                  }
                  else {
                    for (friend <- ToxSingleton.tox.getFriendList) {
                      if (friend != parent) {
                        sendMessageToNonSource(friendInfo.key, hop, updown, spreadtime, exptime, active, msgContent)
                      }
                    }
                  }
                }
              }
              handlerTwo.postDelayed(runnableTwo, delay)
              db.removePendingMsg(id)
            }
          }
        }
        case 2 => {
          AntoxLog.debug("Receive AskBeacon Message.", TAG)

          val isExist: Boolean = db.getNewPrivateMsgList(msgContent, exptime)
          var reply = Array("ReplyBeacon")
          if (isExist) {
            reply(0) = "ReplyBeaconY"
          }
          else {
            reply(0) = "ReplyBeaconN"
          }
          // reply to beacon
          reply ++= parsedMsg.slice(1, 8)
          val replyString = reply.mkString("&")
          Try(ToxSingleton.tox.friendSendMessage(friendInfo.key, ToxFriendMessage.unsafeFromValue(replyString.getBytes), messageType))
        }
        case 3 => {
          // check whether the pending messages expired
          AntoxLog.debug("The ReplyBeaconN message comes.", TAG)
          val idBuffer = db.getPendingMsg(msgContent)
          if (idBuffer.size > 0 && exptime.after(currentTimestamp)) {
            // update the list of next sources
            var id = idBuffer.head
            db.updatePendingNextSource(id, friendInfo.key.key)
          }
        }
      }
    }
  }

  def parseMsg(recMsg: String): Array[String] = {
    val tmpResult = recMsg.split("&")
    val tmpResult2 = recMsg.split("&")
    val length = tmpResult.size
    var result = new Array[String](8)

    if (length >= 8) {
      result = tmpResult.slice(0,7)
      val tmp2 = tmpResult2.slice(7,length)
      result ++= Array(tmp2.mkString("&"))
    }
    else {
      result = Array("","","","","","","",recMsg)
    }
    result
  }

  def handleGroupMessage(ctx: Context, groupInfo: GroupInfo, peerInfo: GroupPeer, message: String, messageType: ToxMessageType): Unit = {
    val db = State.db

    val chatActive = State.isChatActive(groupInfo.key)

    db.addMessage(groupInfo.key, peerInfo.key, peerInfo.name, message, hasBeenReceived = true,
      hasBeenRead = chatActive, successfullySent = true, messageType)

    if (!chatActive) {
      AntoxNotificationManager.createMessageNotification(ctx, classOf[GroupChatActivity], groupInfo.key, groupInfo.name, message)
    }
  }

  def sendMessage(ctx: Context, friendKey: FriendKey, msg: String, messageType: ToxMessageType, mDbId: Option[Int]): Unit = {
    val db = State.db
    for (splitMsg <- splitMessage(msg)) {
      val mId = Try(ToxSingleton.tox.friendSendMessage(friendKey, ToxFriendMessage.unsafeFromValue(msg.getBytes), messageType)).toOption

      val senderName = ToxSingleton.tox.getName
      val senderKey = ToxSingleton.tox.getSelfKey
      mId match {
        case Some(id) => {
          mDbId match {
            case Some(dbId) => db.updateUnsentMessage(id, dbId)
            case None => db.addMessage(friendKey, senderKey, senderName,
              splitMsg, hasBeenReceived = false, hasBeenRead = false, successfullySent = true, messageType, messageId = id)
          }
          // logging
          // send a message
          GlobalLog.log("4", friendKey.key)
        }
        case None => db.addMessage(friendKey, senderKey, senderName, splitMsg, hasBeenReceived = false,
          hasBeenRead = false, successfullySent = false, messageType)
      }
    }
  }


  def sendFirstMessage(ctx: Context, msgContent: String): Unit = {
    var db = State.db
    var hop: Int = 0
    var updown = new String("UP")
    val calendar = java.util.Calendar.getInstance()
    val spreadtime: Timestamp = new Timestamp(calendar.getTime.getTime + initalSendTime)
    var exptime: Timestamp = new Timestamp(calendar.getTime.getTime + msgLife)
    var parent: FriendKey = FriendKey(ToxSingleton.tox.getSelfKey.toString)
    var active: Boolean = true
    db.addPrivateMessage(true, msgContent, hop, updown, spreadtime, exptime, parent, active)

    val handlerThree = new Handler()
    val delay = initalSendTime
    var nextSource = chooseNextSource(parent)
    //sendMessageToSource(nextSource, hop, updown, spreadtime, exptime, msgContent)
    val runnableThree = new Runnable() {
      override def run(): Unit = {
        handlerThree.removeCallbacks(this)
        // transmit to the next source
        sendMessageToSource(nextSource, hop, updown, spreadtime, exptime, msgContent)
        // transmit to the nonSources
        updown = new String("DOWN")
        active = false
        for (nonSource <- ToxSingleton.tox.getFriendList) {
          if (nonSource.key != nextSource.key) {
            sendMessageToNonSource(nonSource, hop, updown, spreadtime, exptime, active, msgContent)
          }
        }
      }
    }
    handlerThree.postDelayed(runnableThree, delay)
    //handlerOne.removeCallbacks(runnableOne)
  }

  /* Added by Ann to implement Adaptive Spreading Algorithm
  * Start on 1/23/2016
  * */
  def sendBeacon(msg: Array[String]): Unit = {
    val db = State.db
    var beaconMsg = Array("AskBeacon")
    beaconMsg ++= msg
    val beaconMsgString = beaconMsg.mkString("&")
    val parent = FriendKey(ToxSingleton.tox.getSelfKey.toString)
    for (friend <- ToxSingleton.tox.getFriendList) {
      if (friend != parent) {
        Try(ToxSingleton.tox.friendSendMessage(friend, ToxFriendMessage.unsafeFromValue(beaconMsgString.getBytes), MessageType.toToxMessageType(MessageType.MESSAGE)))
      }
    }
  }

  def sendMessageToSource(friend: FriendKey, hop: Int, updown: String, spreadtime: Timestamp, exptime: Timestamp, msgContent: String): Unit = {
    val db = State.db
    val newhop = hop + 1
    val newUpdown = new String("UP")
    val newSpreadtime = new Timestamp(spreadtime.getTime.toInt + sendInterval)
    val parent = FriendKey(ToxSingleton.tox.getSelfKey.toString)
    val active: Boolean = true
    var msg = Array("Regular")
    msg ++= Array(newhop.toString, newUpdown, spreadtime.toString, exptime.toString, ToxSingleton.tox.getSelfKey.toString, active.toString, msgContent)
    val msgString = msg.mkString("&")

    for (splitMsg <- splitMessage(msgString)) {
      val mId = Try(ToxSingleton.tox.friendSendMessage(friend, ToxFriendMessage.unsafeFromValue(msgString.getBytes), MessageType.toToxMessageType(MessageType.MESSAGE))).toOption

      val senderName = ToxSingleton.tox.getName
      val senderKey = ToxSingleton.tox.getSelfKey
      val messageType = MessageType.toToxMessageType(MessageType.MESSAGE)
      mId match {
        case Some(id) => {
          db.addMessage(friend, senderKey, senderName,
          splitMsg, hasBeenReceived = false, hasBeenRead = false, successfullySent = true, messageType, messageId = id)
          // logging
          // send a message
          GlobalLog.log("4", friend.key)
        }
        case None => db.addMessage(friend, senderKey, senderName, splitMsg, hasBeenReceived = false,
          hasBeenRead = false, successfullySent = false, messageType)
      }
    }
  }

  def sendMessageToNonSource(friend: FriendKey, hop: Int, updown: String, spreadtime: Timestamp, exptime: Timestamp, active: Boolean, msgContent: String): Unit = {
    val db = State.db
    val newhop = hop - 1
    val newUpdown = new String("DOWN")
    // val active: Boolean = true
    val newSpreadtime = new Timestamp(spreadtime.getTime.toInt + sendInterval)
    var msg = Array("Regular")
    msg ++= Array(newhop.toString, newUpdown, newSpreadtime.toString, exptime.toString, ToxSingleton.tox.getSelfKey.toString, active.toString, msgContent)
    val msgString = msg.mkString("&")


    for (splitMsg <- splitMessage(msgString)) {
      /* Handler to send message

       */
      val mId = Try(ToxSingleton.tox.friendSendMessage(friend, ToxFriendMessage.unsafeFromValue(msgString.getBytes), MessageType.toToxMessageType(MessageType.MESSAGE))).toOption

      val senderName = ToxSingleton.tox.getName
      val senderKey = ToxSingleton.tox.getSelfKey
      val messageType = MessageType.toToxMessageType(MessageType.MESSAGE)
      mId match {
        case Some(id) => {
          db.addMessage(friend, senderKey, senderName,
            splitMsg, hasBeenReceived = false, hasBeenRead = false, successfullySent = true, messageType, messageId = id)
          // logging
          // send a message
          GlobalLog.log("4", friend.key)
        }
        case None => db.addMessage(friend, senderKey, senderName, splitMsg, hasBeenReceived = false,
          hasBeenRead = false, successfullySent = false, messageType)
      }
    }
    /* change active state
    db.updatePrivateMsgDB
     */

  }
  /* Added by Ann to implement Adaptive Spreading Algorithm
  * End on 1/23/2016
  * */


  def sendGroupMessage(ctx: Context, groupKey: GroupKey, msg: String, messageType: ToxMessageType, mDbId: Option[Int]): Unit = {
    val db = State.db

    for (splitMsg <- splitMessage(msg)) {
      messageType match {
        case ToxMessageType.ACTION =>
          ToxSingleton.tox.sendGroupAction(groupKey, msg)
        case _ =>
          ToxSingleton.tox.sendGroupMessage(groupKey, msg)
      }

      val senderKey = ToxSingleton.tox.getSelfKey
      val senderName = ToxSingleton.tox.getName
      mDbId match {
        case Some(dbId) => db.updateUnsentMessage(0, dbId)
        case None => db.addMessage(groupKey, senderKey, senderName,
          splitMsg, hasBeenReceived =
            true, hasBeenRead = true, successfullySent = true, messageType)
      }
    }
  }

  def splitMessage(msg: String): Array[String] = {
    var currSplitPos = 0
    // convert the string to bytes to handle multibyte languages
    val message = msg.getBytes("UTF-8")
    val result: ArrayBuffer[String] = new ArrayBuffer[String]()

    while (message.length - currSplitPos > Constants.MAX_MESSAGE_LENGTH) {
      var pos = currSplitPos + Constants.MAX_MESSAGE_LENGTH

      // find the last whole unicode char
      while ((message(pos) & 0xc0) == 0x80) {
        pos -= 1
      }

      val str = new String(message.slice(currSplitPos, pos))
      result += str
      currSplitPos = pos
    }
    if (message.length - currSplitPos > 0) {
      result += new String(message.slice(currSplitPos, message.length))
    }

    result.toArray
  }

  def sendUnsentMessages(contactKey: ContactKey, ctx: Context) {
    val db = State.db
    val unsentMessageList = db.getUnsentMessageList(contactKey)

    AntoxLog.debug(s"Sending ${unsentMessageList.length} unsent messages.", TAG)

    for (unsentMessage <- unsentMessageList) {
        contactKey match {
          case key: FriendKey =>
            sendMessage(ctx, key, unsentMessage.message,
              MessageType.toToxMessageType(unsentMessage.`type`), Some(unsentMessage.id))
          case key: GroupKey =>
            sendGroupMessage(ctx, key, unsentMessage.message,
              MessageType.toToxMessageType(unsentMessage.`type`), Some(unsentMessage.id))
        }
    }
  }

  def chooseNextSource(grandParent: FriendKey): FriendKey = {
    val db = State.db
    val friendList = db.getAllFriendKey()
    val numOfFriends = friendList.size
    val r = scala.util.Random
    var friendListIndex: Int = r.nextInt(numOfFriends)
    var friendKey: FriendKey = friendList(friendListIndex)
    while (friendKey.key == grandParent.key) {
      friendListIndex = r.nextInt(numOfFriends)
      friendKey = friendList(friendListIndex)
    }
    friendKey
  }
}
