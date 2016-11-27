package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.monopoly.domke.sebastian.monopoly.R;

public class SpielBeitretenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_beitreten);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void spielLobbyBeitreten(View view) {

        Intent intent = new Intent(this, SpielStartActivity.class);
        startActivity(intent);

    }

}
