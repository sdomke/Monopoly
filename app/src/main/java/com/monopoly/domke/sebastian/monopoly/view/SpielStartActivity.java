package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;

public class SpielStartActivity extends AppCompatActivity {

    String eigenerSpielerIP;
    int aktuellesSpielID;
    Spiel aktuellesSpiel;
    Spieler eigenerSpieler;

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHandler = new DatabaseHandler(this);

        Intent intent = getIntent();

        eigenerSpielerIP = intent.getStringExtra("eigenerSpielerIpAdresse");
        aktuellesSpielID = intent.getIntExtra("aktuellesSpielID", 0);

        eigenerSpieler = databaseHandler.getSpielerBySpielIdAndSpielerIp(aktuellesSpielID, eigenerSpielerIP);

        init();

    }

    public void init(){
        TextView aktuellesKapitalEigenerSpielerTextView = (TextView) findViewById(R.id.deinkapitalTextView);
        aktuellesKapitalEigenerSpielerTextView.setText(eigenerSpieler.getSpielerKapital() + " M$");

    }

}
