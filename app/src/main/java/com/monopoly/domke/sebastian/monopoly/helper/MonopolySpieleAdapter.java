package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MonopolySpieleAdapter extends ArrayAdapter<Spiel>{

	// declaring our ArrayList of items
	private ArrayList<Spiel> objects;
	ImageView spielEntfernen;
	ImageView spielLaden;
	View view;
	SharedPreferences sharedPreferences = null;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public MonopolySpieleAdapter(Context context, int textViewResourceId, ArrayList<Spiel> objects) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
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
			view = inflater.inflate(R.layout.list_item_monopoly_spiel, null);
		}
		
		/*
		View parentRow = (View) view.getParent();
		ListView listView = (ListView) parentRow.getParent();
		final int fischPosition = listView.getPositionForView(parentRow);
		*/
		spielEntfernen = (ImageView) view.findViewById(R.id.monopolySpielEntfernenItem);

		spielEntfernen.setOnClickListener(new View.OnClickListener() {
			 public void onClick(View v) {

					objects.remove(position);
				 	MonopolySpieleAdapter.super.notifyDataSetChanged();
			 }
		});

		spielLaden = (ImageView) view.findViewById(R.id.monopolySpielLadenItem);

		spielLaden.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				sharedPreferences = getContext().getSharedPreferences("monopoly", getContext().MODE_PRIVATE);

				Spiel i = objects.get(position);

				sharedPreferences.edit().putString("monopolySpielDatum", i.getSpielDatum()).commit();

				Intent intent = new Intent(getContext(), SpielBeitretenActivity.class);
				intent.putExtra("SpielLaden", "true");
				getContext().startActivity(intent);
			}
		});
 		
		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Spiel i = objects.get(position);

		if (i != null) {

			TextView spielDatum = (TextView) view.findViewById(R.id.monopolySpielDatumDataItem);
			TextView spielerAnzahl = (TextView) view.findViewById(R.id.monopolySpielSpielerAnzahlDataItem);
			TextView startkapital = (TextView) view.findViewById(R.id.monopolySpielStartkapitalDataItem);

			if (spielDatum != null){
				spielDatum.setText(i.getSpielDatum());
			}
			if (spielerAnzahl != null){
				spielerAnzahl.setText("Spieleranzahl: " + i.getSpielerAnzahl());
			}
			if (startkapital != null){
				startkapital.setText("Startkapital:" + i.getSpielerStartkapital());
			}
		}

		// the view must be returned to our activity
		return view;

	}
}
