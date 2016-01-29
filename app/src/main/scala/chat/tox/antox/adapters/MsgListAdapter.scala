package chat.tox.antox.adapters

import java.util

import android.app.Fragment
import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ImageView, TextView, BaseAdapter}
import chat.tox.antox.R
import chat.tox.antox.tox.ToxSingleton
import chat.tox.antox.utils.MsgItem

/**
 * Created by Ann on 1/19/16.
 */
class MsgListAdapter(val context: Context, val list: util.ArrayList[MsgItem]) extends BaseAdapter {
  override def getItemId(position: Int): Long = position
  override def getCount: Int = list.size()
  override def getView(position:Int, convertView: View, parent: ViewGroup): View = {
    var mConvertView: View = convertView
    val mHolder = new MsgViewHolder
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    if (convertView == null) {
      val rootView: View = inflater.inflate(R.layout.list_msg_item, null)
      //mHolder.msgStatusView = rootView.findViewById(R.id.msgStatus).asInstanceOf[ImageView]
      mHolder.msgStringView = rootView.findViewById(R.id.msgString).asInstanceOf[TextView]
      rootView.setTag(mHolder)

      mConvertView = rootView
    }
    val holder = mConvertView.getTag.asInstanceOf[MsgViewHolder]
    holder.msgStringView.setText(list.get(position).asInstanceOf[MsgItem].msg)
    if (list.get(position).asInstanceOf[MsgItem].isMine) {
      holder.msgStringView.setBackgroundColor(R.color.green_light)
    }
    else {
      holder.msgStringView.setBackgroundColor(R.color.white)
    }

    //holder.msgStringView.setBackgroundColor(R.color.abc_background_cache_hint_selector_material_dark)
    mConvertView
  }

  override def getItem(position: Int): AnyRef = list.get(position)

  override def hasStableIds = true
}

class MsgViewHolder {
  //var msgStatusView: ImageView = _
  var msgStringView: TextView = _
}