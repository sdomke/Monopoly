package com.monopoly.domke.sebastian.monopoly.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.MonopolySpieleAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class SpielLadenActivity extends AppCompatActivity {

    private DatabaseHandler datasource;
    private MonopolySpieleAdapter spiele_adapter;
    private ListView monopolySpieleListView;
    public static final String TAG = "SpielLadenGame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_laden);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datasource = new DatabaseHandler(this);

        monopolySpieleListView = (ListView) findViewById(R.id.monopolySpieleListeListView);

        ArrayList<Spiel> values = datasource.getAllSpiele();

        arrayListeSortieren(values);

        spiele_adapter = new MonopolySpieleAdapter(this,
                R.layout.list_item_monopoly_spiel, values, datasource);
        monopolySpieleListView.setAdapter(spiele_adapter);
    }

    public void arrayListeSortieren(ArrayList<Spiel> values){
        Collections.sort(values, new Comparator<Spiel>() {

            @Override
            public int compare(Spiel lhs, Spiel rhs) {

                String datumEins = lhs.getSpielDatum();
                String datumZwei = rhs.getSpielDatum();

                Log.d(TAG, "DatumEins: " + datumEins + " DatumZwei: " + datumZwei);

                SimpleDateFormat datumFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);

                Date dateOne = null;
                try {
                    dateOne = datumFormat.parse(datumEins);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Date dateTwo = null;
                try {
                    dateTwo = datumFormat.parse(datumZwei);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if(dateOne.compareTo(dateTwo) < 0){
                    return 1;
                }

                else if(dateOne.compareTo(dateTwo) > 0){

                    return -1;
                }
                else{
                    return 0;
                }
            }
        });
    }

}
