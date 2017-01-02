package com.monopoly.domke.sebastian.monopoly.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.MonopolySpieleAdapter;

import java.util.ArrayList;

public class SpielLadenActivity extends AppCompatActivity {

    private DatabaseHandler datasource;
    private MonopolySpieleAdapter spiele_adapter;
    private ListView monopolySpieleListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_laden);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datasource = new DatabaseHandler(this);

        monopolySpieleListView = (ListView) findViewById(R.id.monopolySpieleListeListView);

        ArrayList<Spiel> values = datasource.getAllSpiele();

        spiele_adapter = new MonopolySpieleAdapter(this,
                R.layout.list_item_monopoly_spiel, values, datasource);
        monopolySpieleListView.setAdapter(spiele_adapter);
    }

}
