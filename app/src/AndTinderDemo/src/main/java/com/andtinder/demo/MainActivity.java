/**
 * AndTinder v0.1 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 *
 * TAndTinder is a native library for Android that provide a
 * Tinder card like effect. A card can be constructed using an
 * image and displayed with animation effects, dismiss-to-like
 * and dismiss-to-unlike, and use different sorting mechanisms.
 *
 * AndTinder is compatible with API Level 13 and upwards
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */

package com.andtinder.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;


public class MainActivity extends Activity {

    /**
     * This variable is the container that will host our cards
     */
	private CardContainer mCardContainer;
	private int likeCount = 0;
	private int dislikeCount = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mainlayout);

		mCardContainer = (CardContainer) findViewById(R.id.layoutview);

		//Resources r = getResources();

		SimpleCardStackAdapter adapter = new SimpleCardStackAdapter(this);
		//adapter.add(new CardModel("Contact name 1", "Message 1 goes here"));
		//adapter.add(new CardModel("Contact name 2", "Message 2 goes here"));
		//adapter.add(new CardModel("Contact name 3", "Message 3 goes here"));
		//adapter.add(new CardModel("Contact name 4", "Message 4 goes here"));
		//adapter.add(new CardModel("Contact name 5", "Message 5 goes here"));
		//adapter.add(new CardModel("Contact name 6", "Message 6 goes here"));

		// ---------------
        CardModel cardModel = new CardModel("Contact name 1", "Message 1 goes here");
        cardModel.setOnClickListener(new CardModel.OnClickListener() {
			@Override
			public void OnClickListener() {
				Log.i("Swipeable Cards", "I am pressing the card");
			}
		});
        cardModel.setOnCardDismissedListener(new CardModel.OnCardDismissedListener() {
			@Override
			public void onLike() {
				Log.i("Swipeable Cards", "I like the card");
				likeCount += 1;
				Toast toast = Toast.makeText(getApplicationContext(), "likes:" + likeCount, Toast.LENGTH_SHORT);
				toast.show();
			}

			@Override
			public void onDislike() {
				Log.i("Swipeable Cards", "I dislike the card");
				dislikeCount += 1;
				Toast toast = Toast.makeText(getApplicationContext(), "dislikes:" + dislikeCount, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
        adapter.add(cardModel);
		// ----------------
		// -------
		cardModel = new CardModel("Contact name 2", "Message 2 goes here");
		cardModel.setOnClickListener(new CardModel.OnClickListener() {
			@Override
			public void OnClickListener() {
				Log.i("Swipeable Cards", "I am pressing the card");
			}
		});
		cardModel.setOnCardDismissedListener(new CardModel.OnCardDismissedListener() {
			@Override
			public void onLike() {
				Log.i("Swipeable Cards", "I like the card");
				likeCount += 1;
				Toast toast = Toast.makeText(getApplicationContext(), "likes:" + likeCount, Toast.LENGTH_SHORT);
				toast.show();
			}

			@Override
			public void onDislike() {
				Log.i("Swipeable Cards", "I dislike the card");
				dislikeCount += 1;
				Toast toast = Toast.makeText(getApplicationContext(), "dislikes:" + dislikeCount, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		adapter.add(cardModel);
		// ----

		mCardContainer.setAdapter(adapter);
	}
}
