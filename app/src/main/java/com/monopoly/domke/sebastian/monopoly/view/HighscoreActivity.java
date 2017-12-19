package com.monopoly.domke.sebastian.monopoly.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.GamelobbySpielerAdapter;
import com.monopoly.domke.sebastian.monopoly.helper.HighscoreSpielerAdapter;
import com.monopoly.domke.sebastian.monopoly.helper.MonopolySpieleAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class HighscoreActivity extends AppCompatActivity {

    private DatabaseHandler datasource;
    private HighscoreSpielerAdapter spieler_adapter;
    private ListView highscoreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datasource = new DatabaseHandler(this);

        highscoreListView = (ListView) findViewById(R.id.highscoreListeListView);

        ArrayList<Spieler> values = datasource.getAllSpieler();

        arrayListeSortieren(values);

        if(values.size() > 10){
            values.subList(0, 9);
        }

        spieler_adapter = new HighscoreSpielerAdapter(this, R.layout.list_item_highscoreliste, values);
        highscoreListView.setAdapter(spieler_adapter);
    }

    public void arrayListeSortieren(ArrayList<Spieler> values){
        Collections.sort(values, new Comparator<Spieler>() {

            @Override
            public int compare(Spieler lhs, Spieler rhs) {

                double kapitalEins = lhs.getSpielerKapital();
                double kapitalZwei = rhs.getSpielerKapital();


                if(kapitalEins < kapitalZwei){
                    return 1;
                }

                else if(kapitalEins > kapitalZwei){

                    return -1;
                }
                else{
                    return 0;
                }
            }
        });
    }

}
