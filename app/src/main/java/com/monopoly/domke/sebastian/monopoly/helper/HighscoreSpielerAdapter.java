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

public class HighscoreSpielerAdapter extends ArrayAdapter<Spieler>{

	// declaring our ArrayList of items
	private ArrayList<Spieler> objects;
	View view;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public HighscoreSpielerAdapter(Context context, int textViewResourceId, ArrayList<Spieler> objects) {
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
			view = inflater.inflate(R.layout.list_item_highscoreliste, null);
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

			TextView spielerName = (TextView) view.findViewById(R.id.spielerNameHighscoreDataItem);
			TextView spielerKapital = (TextView) view.findViewById(R.id.spielerKapitalHighscoreDataItem);
			TextView spielerRanking = (TextView) view.findViewById(R.id.spielerRankingHighscoreDataItem);
			ImageView spielerFarbe = (ImageView) view.findViewById(R.id.spielerIconHighscoreItem);

			if (spielerName != null){
				spielerName.setText(i.getSpielerName());
			}

			if (spielerKapital != null){
				spielerKapital.setText(Double.toString(i.getSpielerKapital()) + " M$");
			}

			if (spielerRanking != null){
				spielerRanking.setText(Integer.toString(position+1)+"#");
			}

			if (spielerFarbe != null){
				spielerFarbe.setBackgroundColor(ContextCompat.getColor(getContext(), i.getSpielerFarbe()));
			}


		}

		// the view must be returned to our activity
		return view;

	}

}
