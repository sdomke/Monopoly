package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.monopoly.domke.sebastian.monoploy.R;

public class NeuesSpielActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neues_spiel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void neuesSpielErstellen(View view) {

        Intent intent = new Intent(this, SpielBeitretenActivity.class);
        startActivity(intent);

    }

}
