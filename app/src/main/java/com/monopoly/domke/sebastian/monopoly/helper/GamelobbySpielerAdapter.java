package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

	private SharedPreferences sharedPreferences = null;

	public static final String SHARED_PREF = "SHARED_PREF";
	public static final String SHARED_PREF_IP_ADRESS = "SHARED_PREF_IP_ADRESS";
	public static final String SHARED_PREF_PORT = "SHARED_PREF_PORT";

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public GamelobbySpielerAdapter(Context context, int textViewResourceId, ArrayList<Spieler> objects, SpielBeitretenActivity spielBeitretenActivity) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
		this.spielBeitretenActivity = spielBeitretenActivity;
		sharedPreferences = context.getSharedPreferences(SHARED_PREF, 0); // 0 - for private mode
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
				//ToDo Löschen-Button von EigenerSpieler wird nicht mehr angezeigt wenn zuvor ein GegenSpieler gelöscht wurde

				Spieler i = objects.get(position);
				spielBeitretenActivity.datasource.deleteSpieler(i.getSpielerIMEI(), spielBeitretenActivity.aktuellesSpiel.getSpielID());
				objects.remove(position);

				GamelobbySpielerAdapter.super.notifyDataSetChanged();

				JSONObject messageContent = spielBeitretenActivity.messageParser.playerStatusToJson(spielBeitretenActivity.eigenerSpieler, spielBeitretenActivity.aktuellesSpiel);

				GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.exitGame, messageContent);

				String jsonString = spielBeitretenActivity.messageParser.messageToJsonString(requestJoinGameMessage);

				String ipAdress = sharedPreferences.getString(SHARED_PREF_IP_ADRESS, null);
				int port = sharedPreferences.getInt(SHARED_PREF_PORT, -1);

				spielBeitretenActivity.mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

				Toast.makeText(getContext(), "Du hast die Spiellobby verlassen!", Toast.LENGTH_SHORT).show();

				//SendMessageJob.scheduleSendMessageJob(ipAdress, port, jsonString);

				//spielBeitretenActivity.mGameConnection.sendMessage(jsonString);

				RelativeLayout spielLobbyBeitretenButtonLayout = (RelativeLayout) spielBeitretenActivity.findViewById(R.id.spielLobbyBeitretenButtonLayout);
				spielLobbyBeitretenButtonLayout.setEnabled(true);
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
			if(!i.getSpielerIMEI().equals(spielBeitretenActivity.eigenerSpieler.getSpielerIMEI())){
				ImageView spielerEntfernenItem = (ImageView) view.findViewById(R.id.spielerEntfernenItem);
				if (spielerEntfernenItem != null){
					spielerEntfernenItem.setVisibility(View.INVISIBLE);
				}
			}
		}

		// the view must be returned to our activity
		return view;

	}

}
