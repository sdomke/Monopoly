package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class GameStatusAdapter extends ArrayAdapter<Spieler>{

	// declaring our ArrayList of items
	public ArrayList<Spieler> objects;
	View view;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public GameStatusAdapter(Context context, int textViewResourceId, ArrayList<Spieler> objects) {
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
			view = inflater.inflate(R.layout.list_item_spieluebersicht, null);
		}

 		
		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Spieler i = objects.get(position);

		if (i != null) {

			TextView spielItemName = (TextView) view.findViewById(R.id.spielItemNameView);
			TextView spielItemCapital = (TextView) view.findViewById(R.id.spielItemCapitalView);

			if (spielItemName != null){
				spielItemName.setBackgroundColor(ContextCompat.getColor(getContext(), i.getSpielerFarbe()));
				//spielItemName.setText(i.getSpielerName().charAt(0));
			}
			if (spielItemCapital != null){
				spielItemCapital.setText(String.valueOf(i.getSpielerKapital()));
			}

		}

		// the view must be returned to our activity
		return view;

	}

}
