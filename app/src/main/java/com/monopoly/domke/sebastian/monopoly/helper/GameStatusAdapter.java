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
import com.monopoly.domke.sebastian.monopoly.common.Spieler;

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
			ImageView spielVerlauf = (ImageView) view.findViewById(R.id.verlauf);

			if (spielItemName != null){
				spielItemName.setBackgroundColor(ContextCompat.getColor(getContext(), i.getSpielerFarbe()));

				switch (i.getSpielerFarbe()) {
					case R.color.gruen_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_dunkel_gruen);
						break;
					case R.color.gelb_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_gelb);
						break;
					case R.color.blau_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_dunkel_blau);
						break;
					case R.color.rot_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_rot);
						break;
					case R.color.grau_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_braun);
						break;
					case R.color.weiß_spieler_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_grau);
						break;
					case R.color.mitte_farbe: spielItemName.setBackgroundResource(R.drawable.layout_circle_hell_blau);
						break;
					default: spielItemName.setBackgroundResource(R.drawable.layout_circle_transparent);
						break;
				}

				spielItemName.setText(String.valueOf(i.getSpielerName().charAt(0)));
			}
			if (spielItemCapital != null){
				spielItemCapital.setText(String.valueOf(i.getSpielerKapital()));
			}

			if(spielVerlauf != null) {
				if(i.getHistory() == 0) {
					spielVerlauf.setBackgroundResource(R.drawable.layout_selector_circle_orange);
					spielVerlauf.setImageResource(R.drawable.baseline_trending_flat_white_24);
				} else if(i.getHistory() == 1) {
					spielVerlauf.setBackgroundResource(R.drawable.layout_selector_circle_hell_gruen);
					spielVerlauf.setImageResource(R.drawable.baseline_trending_up_white_24);
				} else if(i.getHistory() == 2) {
					spielVerlauf.setBackgroundResource(R.drawable.layout_selector_circle_rot);
					spielVerlauf.setImageResource(R.drawable.baseline_trending_down_white_24);
				}
			}

		}

		// the view must be returned to our activity
		return view;

	}

}
