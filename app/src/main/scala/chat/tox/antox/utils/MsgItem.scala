package chat.tox.antox.utils

import chat.tox.antox.wrapper.{ToxKey, ContactKey, FriendKey}
import java.sql.Timestamp

/**
 * Created by Ann on 1/19/16.
 */
class MsgItem (message:String, selfSent:Boolean, sendTime: Timestamp) {
  var msg = message
  var isMine = selfSent
  var time = sendTime
}
