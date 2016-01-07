package chat.tox.antox.fragments

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Toast
import chat.tox.antox.R
import chat.tox.antox.activities.EvalActivity

/**
 * Created by Ann on 12/24/15.
 */
class EvalFragment extends android.support.v4.app.Fragment with View.OnClickListener{
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
}