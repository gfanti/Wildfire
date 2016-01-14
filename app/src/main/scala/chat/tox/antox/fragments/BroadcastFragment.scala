package chat.tox.antox.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import chat.tox.antox.R
import chat.tox.antox.activities.BroadcastActivity

/**
 * Created by Ann on 1/13/16.
 */
class BroadcastFragment extends android.support.v4.app.Fragment with View.OnClickListener{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_broadcast, container, false)
    val button = rootView.findViewById(R.id.broadcastFragButton)
    button.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        val intent: Intent = new Intent(getActivity, classOf[BroadcastActivity])
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
}