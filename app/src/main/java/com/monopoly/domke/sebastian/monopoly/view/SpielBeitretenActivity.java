package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.GamelobbySpielerAdapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SpielBeitretenActivity extends AppCompatActivity {

    private DatabaseHandler datasource;
    private GamelobbySpielerAdapter spieler_adapter;
    private Spieler eigenerSpieler;
    private String eigenerSpielerName = "Spieler";
    private int eigenerSpielerFarbe = R.color.weiß_spieler_farbe;
    private ListView gamelobbyListView;
    private SharedPreferences sharedPreferences = null;
    private Spiel aktuellesSpiel;
    private String ipAdresseHost;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_beitreten);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Todo Intent von NeuesSpiel-, MainMenu- oder SpielLaden-Activity mit den Spieldaten

        sharedPreferences = getSharedPreferences("monopoly", MODE_PRIVATE);

        datasource = new DatabaseHandler(this);

        aktuellesSpiel = datasource.getSpielByDatum(sharedPreferences.getString("monopolySpielDatum", "Kein Spiel erzeugt"));

        Log.i("SpielDatum", sharedPreferences.getString("monopolySpielDatum", "Kein Spiel erzeugt"));

        //ipAdresseHost = intToInetAddress(wifiManager.getDhcpInfo().serverAddress).getHostAddress();

        //Todo Eigene IPAdresse und MacAdresse auslesen

        ipAdresseHost = "192.168.43.1";

        //Todo Überprüfen ob es schon einen Spieler mit der MacAdresse passend zum Spiel gibt (dann wiederherstellen)
        eigenerSpieler = new Spieler(ipAdresseHost, aktuellesSpiel.getSpielID());
        eigenerSpieler.setSpielerName(eigenerSpielerName);
        eigenerSpieler.setSpielerFarbe(eigenerSpielerFarbe);
        eigenerSpieler.setSpielerKapital(aktuellesSpiel.getSpielerStartkapital());

        //Todo FloatingButton nur für SpielHost sichtbar machen
        FloatingActionButton spielStartenFB = (FloatingActionButton) findViewById(R.id.spielStartenFloatingButton);
        spielStartenFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!gamelobbyListView.getAdapter().isEmpty()) {

                    //Todo Spiel gestartet Nachricht an andere Spieler

                    //Todo Intent mit den MacAdressen aller aktiven Spieler und Datum des Spiels
                    Intent intent = new Intent(getApplicationContext(), SpielStartActivity.class);
                    intent.putExtra("eigenerSpielerIpAdresse", eigenerSpieler.getSpielerIpAdresse());
                    intent.putExtra("aktuellesSpielID", aktuellesSpiel.getSpielID());
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Das Spiel kann nicht gestartet werden, da bisher keine Spieler beigetreten sind", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final EditText meinNameEditText = (EditText) findViewById(R.id.meinSpielerNameEditText);
        meinNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                eigenerSpieler.setSpielerName(meinNameEditText.getText().toString());
                datasource.updateSpieler(eigenerSpieler);
                spieler_adapter.notifyDataSetChanged();

            }
        });

        meinNameEditText.clearFocus();

        final ImageView meineEinstellungenImageView = (ImageView) findViewById(R.id.meineEinstellungenView);

        ImageView farbeGelbImageView = (ImageView) findViewById(R.id.spielerFarbeGelbButtonView);
        ImageView farbeGruenImageView = (ImageView) findViewById(R.id.spielerFarbeGruenButtonView);
        ImageView farbeBlauImageView = (ImageView) findViewById(R.id.spielerFarbeBlauButtonView);
        ImageView farbeRotImageView = (ImageView) findViewById(R.id.spielerFarbeRotButtonView);
        ImageView farbeGrauImageView = (ImageView) findViewById(R.id.spielerFarbeGruenButtonView);
        ImageView farbeSchwarzImageView = (ImageView) findViewById(R.id.spielerFarbeWeißButtonView);

        farbeGelbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.gelb_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gelb_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeGruenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.gruen_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gruen_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeBlauImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.blau_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blau_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeRotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.rot_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rot_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeGrauImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.grau_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grau_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeSchwarzImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.weiß_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.weiß_spieler_farbe));
                spieler_adapter.notifyDataSetChanged();
            }
        });

        gamelobbyListView = (ListView) findViewById(R.id.gameLobbyList);

        ArrayList<Spieler> values = datasource.getAllSpielerBySpielID(aktuellesSpiel.getSpielID());

        spieler_adapter = new GamelobbySpielerAdapter(this,
                R.layout.list_item_spieler, values);
        gamelobbyListView.setAdapter(spieler_adapter);

        try {
            datasource.getSpielerBySpielIdAndSpielerIp(aktuellesSpiel.getSpielID(), ipAdresseHost);
        }catch (Exception e){
            datasource.addSpieler(eigenerSpieler);
        }
            eigenerSpieler = datasource.getSpielerBySpielIdAndSpielerIp(aktuellesSpiel.getSpielID(), ipAdresseHost);
    }

    public void spielLobbyBeitreten(View view) {

        GamelobbySpielerAdapter adapter = (GamelobbySpielerAdapter) gamelobbyListView.getAdapter();

        EditText eigenerSpielerNameEditText = (EditText) findViewById(R.id.meinSpielerNameEditText);

        if(eigenerSpielerNameEditText.getText().toString().length() != 0) {
            eigenerSpieler.setSpielerName(eigenerSpielerNameEditText.getText().toString());
        }

        adapter.add(eigenerSpieler);
        datasource.updateSpieler(eigenerSpieler);

        //Todo Spiel beigetreten Nachricht an andere Spieler

        //Todo Auswahl der SpielerName und SpielerFarbe ausgrauen solange in Spiellobby

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate themenu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.beitreten_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.spieler_einladen_action) {

            Toast.makeText(getApplicationContext(), "Einladung an Spieler gesendet", Toast.LENGTH_SHORT).show();
            //Todo Spieler einladen Nachricht an andere Spieler
        }

        return true;
    }

    public InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    //Todo Gegenspieler initiieren
    public void gegenspielerInit(){

    }

}
