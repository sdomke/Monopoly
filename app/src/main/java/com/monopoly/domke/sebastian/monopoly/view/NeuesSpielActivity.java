package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;

public class NeuesSpielActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DatabaseHandler databaseHandler;
    private Spiel neuesSpiel;
    private int spielerAnzahl = 2;
    private int startkapital = 12000000;
    private String spielDatum;
    private Spinner spinner;
    private String waehrung;

    private SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neues_spiel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("monopoly", MODE_PRIVATE);

        databaseHandler = new DatabaseHandler(this);
        neuesSpiel = new Spiel();

        neuesSpiel.setSpielerAnzahl(spielerAnzahl);

        neuesSpiel.setSpielerStartkapital(startkapital);

        ImageView spielerHinzuButton = (ImageView) findViewById(R.id.spielerHinzuButton);
        ImageView spielerEntfernenButton = (ImageView) findViewById(R.id.spielerEntfernenButton);

        spielerHinzuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spielerAnzahlErhoehen();
            }
        });

        spielerEntfernenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spielerAnzahlVerringern();
            }
        });

        spinner = (Spinner) findViewById(R.id.waehrungSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.waehrungenArray, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void neuesSpielErstellen(View view) {

        EditText startKapitelEditText = (EditText) findViewById(R.id.startKapitelEditText);

        java.util.Date datumErzeugenJetzt = new java.util.Date();
        spielDatum = (String) DateFormat.format("dd.MM.yyyy HH:mm:ss", datumErzeugenJetzt);
        neuesSpiel.setSpielDatum(spielDatum);
        neuesSpiel.setFreiParken(0);

        if(startKapitelEditText.getText().toString().length() != 0) {
            String tmpStartKapital = startKapitelEditText.getText().toString();

            //ToDO Catch exception
            neuesSpiel.setSpielerStartkapital(Integer.parseInt(tmpStartKapital));
        }

        databaseHandler.addNewMonopolyGame(neuesSpiel);

        sharedPreferences.edit().putString("monopolySpielDatum", spielDatum).commit();

        Intent intent = new Intent(this, SpielBeitretenActivity.class);
        startActivity(intent);

    }

    public void spielerAnzahlErhoehen(){

        ImageView spielerAnzahlImageView;

        switch (neuesSpiel.getSpielerAnzahl()){

            case 1:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl2View);
                spielerAnzahlImageView.setVisibility(View.VISIBLE);
                neuesSpiel.setSpielerAnzahl(2);
                break;

            case 2:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl3View);
                spielerAnzahlImageView.setVisibility(View.VISIBLE);
                neuesSpiel.setSpielerAnzahl(3);
                break;

            case 3:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl4View);
                spielerAnzahlImageView.setVisibility(View.VISIBLE);
                neuesSpiel.setSpielerAnzahl(4);
                break;

            case 4:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl5View);
                spielerAnzahlImageView.setVisibility(View.VISIBLE);
                neuesSpiel.setSpielerAnzahl(5);
                break;

            case 5:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl6View);
                spielerAnzahlImageView.setVisibility(View.VISIBLE);
                neuesSpiel.setSpielerAnzahl(6);
                break;
        }
    }

    public void spielerAnzahlVerringern(){
        ImageView spielerAnzahlImageView;

        switch (neuesSpiel.getSpielerAnzahl()){

            case 2:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl2View);
                spielerAnzahlImageView.setVisibility(View.INVISIBLE);
                neuesSpiel.setSpielerAnzahl(1);
                break;

            case 3:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl3View);
                spielerAnzahlImageView.setVisibility(View.INVISIBLE);
                neuesSpiel.setSpielerAnzahl(2);
                break;

            case 4:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl4View);
                spielerAnzahlImageView.setVisibility(View.INVISIBLE);
                neuesSpiel.setSpielerAnzahl(3);
                break;

            case 5:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl5View);
                spielerAnzahlImageView.setVisibility(View.INVISIBLE);
                neuesSpiel.setSpielerAnzahl(4);
                break;

            case 6:
                spielerAnzahlImageView = (ImageView) findViewById(R.id.spielerAnzahl6View);
                spielerAnzahlImageView.setVisibility(View.INVISIBLE);
                neuesSpiel.setSpielerAnzahl(5);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        neuesSpiel.setWaehrung(parent.getItemAtPosition(position).toString());

        EditText startKapitelEditText = (EditText) findViewById(R.id.startKapitelEditText);
        startKapitelEditText.setHint(neuesSpiel.getWaehrung());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        neuesSpiel.setWaehrung("Monopoly-Dollar");
    }
}
