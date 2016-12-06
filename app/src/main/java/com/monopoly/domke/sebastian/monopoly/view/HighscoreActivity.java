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
import com.monopoly.domke.sebastian.monopoly.helper.MonopolySpieleAdapter;

import java.util.ArrayList;

public class HighscoreActivity extends AppCompatActivity {

    private DatabaseHandler datasource;
    private MonopolySpieleAdapter spiele_adapter;
    private ListView highscoreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datasource = new DatabaseHandler(this);

        highscoreListView = (ListView) findViewById(R.id.highscoreListeListView);

        //ArrayList<Spieler> values = datasource.getAllSpieler();

        //spiele_adapter = new MonopolySpieleAdapter(this, R.layout.list_item_highscoreliste, values);
        //highscoreListView.setAdapter(spiele_adapter);
    }

}
