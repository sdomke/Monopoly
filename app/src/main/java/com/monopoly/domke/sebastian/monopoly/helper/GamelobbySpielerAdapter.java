package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class GamelobbySpielerAdapter extends ArrayAdapter<Spieler>{

	// declaring our ArrayList of items
	public ArrayList<Spieler> objects;
	ImageView spielerEntfernen;
	View view;
	SpielBeitretenActivity spielBeitretenActivity;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public GamelobbySpielerAdapter(Context context, int textViewResourceId, ArrayList<Spieler> objects, SpielBeitretenActivity spielBeitretenActivity) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
		this.spielBeitretenActivity = spielBeitretenActivity;
	}

	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
	public View getView(final int position, View convertView, final ViewGroup parent){

		//imageLoader.clearCache();
		
		// assign the view we are converting to a local variable
		view = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item_spieler, null);
		}
		
		/*
		View parentRow = (View) view.getParent();
		ListView listView = (ListView) parentRow.getParent();
		final int fischPosition = listView.getPositionForView(parentRow);
		*/
		spielerEntfernen = (ImageView) view.findViewById(R.id.spielerEntfernenItem);

		spielerEntfernen.setOnClickListener(new View.OnClickListener() {
			 public void onClick(View v) {

		 		Spieler i = objects.get(position);
			 	spielBeitretenActivity.datasource.deleteSpieler(i.getSpielerIMEI(), spielBeitretenActivity.aktuellesSpiel.getSpielID());

				objects.remove(position);
				GamelobbySpielerAdapter.super.notifyDataSetChanged();

				 JSONObject messageContent = spielBeitretenActivity.messageParser.playerStatusToJson(spielBeitretenActivity.eigenerSpieler, spielBeitretenActivity.aktuellesSpiel);

				 GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.exitGame, messageContent);

				 String jsonString = spielBeitretenActivity.messageParser.messageToJsonString(requestJoinGameMessage);

				 spielBeitretenActivity.mGameConnection.sendMessage(jsonString);
			 }
		});
 		
		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Spieler i = objects.get(position);

		if (i != null) {

			TextView spielerName = (TextView) view.findViewById(R.id.spielerNameDataItem);
			ImageView spielerFarbe = (ImageView) view.findViewById(R.id.list_image);

			if (spielerName != null){
				spielerName.setText(i.getSpielerName());
			}
			if (spielerFarbe != null){
				spielerFarbe.setBackgroundColor(ContextCompat.getColor(getContext(), i.getSpielerFarbe()));
			}
		}

		// the view must be returned to our activity
		return view;

	}

}
