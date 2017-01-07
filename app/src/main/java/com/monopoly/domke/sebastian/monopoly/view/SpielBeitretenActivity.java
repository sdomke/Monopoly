package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.GamelobbySpielerAdapter;
import com.monopoly.domke.sebastian.monopoly.helper.HostMessageInterpreter;
import com.monopoly.domke.sebastian.monopoly.helper.MessageParser;
import com.monopoly.domke.sebastian.monopoly.helper.NsdHelper;
import com.monopoly.domke.sebastian.monopoly.helper.PlayerMessageInterpreter;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SpielBeitretenActivity extends AppCompatActivity {

    public DatabaseHandler datasource;
    public GamelobbySpielerAdapter spieler_adapter;
    public Spieler eigenerSpieler;
    private String eigenerSpielerName = "Spieler";
    private int eigenerSpielerFarbe = R.color.weiß_spieler_farbe;
    public ListView gamelobbyListView;
    public Spiel aktuellesSpiel;
    private String ipAdresseEigenerSpieler;
    private String imeiEigenerSpieler;
    private WifiManager wifiManager;

    public NsdHelper mNsdClient;
    public NsdHelper mNsdServer;
    public GameConnection mGameConnection;
    private Handler mUpdateHandler;
    public static final String TAG = "NsdGame";

    boolean neuesSpiel = false;

    private PlayerMessageInterpreter playerMessageInterpreter;
    private HostMessageInterpreter hostMessageInterpreter;
    public MessageParser messageParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_beitreten);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    public void init(){
        datasource = new DatabaseHandler(this);

        FloatingActionButton spielStartenFB = (FloatingActionButton) findViewById(R.id.spielStartenFloatingButton);

        messageParser = new MessageParser();
        Intent intent = getIntent();

        String spielDatum = intent.getStringExtra("spiel_datum");
        neuesSpiel = intent.getBooleanExtra("neues_spiel", false);

        aktuellesSpiel = datasource.getSpielByDatum(spielDatum);

        //Toast.makeText(getApplicationContext(),"" + neuesSpiel, Toast.LENGTH_SHORT).show();

        mUpdateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();

                try {
                    GameMessage message = messageParser.jsonStringToMessage(msg.getData().getString("msg"));

                    if (!neuesSpiel) {
                        Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                        playerMessageInterpreter.decideWhatToDoWithTheMassage(message);
                    } else {
                        Toast.makeText(getApplicationContext(), "Host Message Handler", Toast.LENGTH_SHORT).show();
                        hostMessageInterpreter.decideWhatToDoWithTheMassage(message);
                    }
                }catch (Exception e){
                    Log.d(TAG, "Keine passende Nachricht");
                    Toast.makeText(getApplicationContext(), "Keine passende Nachricht", Toast.LENGTH_SHORT).show();
                }
            }
        };



        /*mNsdServer = new NsdServer(this);
        //mNsdServer.initializeNsd();
        mNsdServer.initializeRegistrationListener();*/

        if(!neuesSpiel) {
            spielStartenFB.hide();

            mGameConnection = (GameConnection) getApplicationContext();
            mNsdClient = (NsdHelper) getApplicationContext();

            //mGameConnection = new GameConnection(mUpdateHandler);

            //mNsdClient = new NsdHelper(this, mGameConnection);
            //mNsdClient.initializeDiscoveryListener();
            //mNsdClient.initializeResolveListener();

            //Toast.makeText(getApplicationContext(),"Client", Toast.LENGTH_SHORT).show();
            playerMessageInterpreter = new PlayerMessageInterpreter(this);
            //advertiseGame();
            //connectToGame();
        }
        else{
            mGameConnection = new GameConnection(mUpdateHandler);

            mNsdServer = new NsdHelper(this);
            //mNsdServer.initializeNsd();
            mNsdServer.initializeRegistrationListener();
           /* mNsdServer.initializeDiscoveryListener();
            mNsdServer.initializeResolveListener();*/
            //Toast.makeText(getApplicationContext(),"Host", Toast.LENGTH_SHORT).show();
            hostMessageInterpreter = new HostMessageInterpreter(this);

            advertiseGame();


            //connectToGame();
        }

        WifiManager manager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);
        ipAdresseEigenerSpieler = intToInetAddress(manager.getDhcpInfo().serverAddress).getHostAddress();

        imeiEigenerSpieler = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.d(TAG, "IMEI: " + imeiEigenerSpieler);
        Log.d(TAG, "IP: " + ipAdresseEigenerSpieler);

        try{
            eigenerSpieler = datasource.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), imeiEigenerSpieler);
        }catch(Exception e){
            Log.d(TAG, "Spieler nicht gefunden");

            eigenerSpieler = new Spieler(imeiEigenerSpieler, aktuellesSpiel.getSpielID());
            eigenerSpieler.setSpielerName(eigenerSpielerName);
            eigenerSpieler.setSpielerFarbe(eigenerSpielerFarbe);
            eigenerSpieler.setSpielerKapital(aktuellesSpiel.getSpielerStartkapital());
            eigenerSpieler.setSpielerIpAdresse(ipAdresseEigenerSpieler);
        }

        spielStartenFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!gamelobbyListView.getAdapter().isEmpty()) {

                    ArrayList<String> aktivePlayerList = new ArrayList<String>();

                    for (Spieler spieler : spieler_adapter.objects) {
                        aktivePlayerList.add(spieler.getSpielerIMEI());
                    }

                    Intent intent = new Intent(getApplicationContext(), SpielStartActivity.class);
                    intent.putStringArrayListExtra("aktive_spieler", aktivePlayerList);
                    intent.putExtra("aktuellesSpielID", aktuellesSpiel.getSpielID());
                    intent.putExtra("neues_spiel", true);

                    JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

                    GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.gameStart, messageContent);

                    String jsonString = messageParser.messageToJsonString(startGameMessage);

                    mGameConnection.sendMessage(jsonString);

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
        ImageView farbeGruenImageView = (ImageView) findViewById(R.id.gegenspieler3ButtonView);
        ImageView farbeBlauImageView = (ImageView) findViewById(R.id.gegenspieler2ButtonView);
        ImageView farbeRotImageView = (ImageView) findViewById(R.id.gegenspieler1ButtonView);
        ImageView farbeGrauImageView = (ImageView) findViewById(R.id.spielerFarbeGrauButtonView);
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
                R.layout.list_item_spieler, values, this);
        gamelobbyListView.setAdapter(spieler_adapter);
    }

    public void spielLobbyBeitreten(View view) {

        GamelobbySpielerAdapter adapter = (GamelobbySpielerAdapter) gamelobbyListView.getAdapter();

        EditText eigenerSpielerNameEditText = (EditText) findViewById(R.id.meinSpielerNameEditText);

        if(eigenerSpielerNameEditText.getText().toString().length() != 0) {
            eigenerSpieler.setSpielerName(eigenerSpielerNameEditText.getText().toString());
        }

        adapter.add(eigenerSpieler);

        try{
            datasource.getSpielerBySpielIdAndSpielerIMEI(eigenerSpieler.getIdMonopolySpiel(), eigenerSpieler.getSpielerIMEI());
            datasource.updateSpieler(eigenerSpieler);
            Log.d(TAG, "Spieler gefunden");
        }catch(Exception e){
            Log.d(TAG, "Spieler nicht gefunden");

            datasource.addSpieler(eigenerSpieler);
        }

        JSONObject messageContent = messageParser.playerStatusToJson(eigenerSpieler, aktuellesSpiel);

        GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.joinGame, messageContent);

        String jsonString = messageParser.messageToJsonString(requestJoinGameMessage);

        mGameConnection.sendMessage(jsonString);

        //Todo Auswahl der SpielerName und SpielerFarbe ausgrauen solange in Spiellobby und nicht doppelt beitreten

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate themenu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.beitreten_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.send) {

            JSONObject messageContent = messageParser.invitePlayerToJson(eigenerSpieler, aktuellesSpiel);

            GameMessage invitationGameMessage = new GameMessage(GameMessage.MessageHeader.invitation, messageContent);

            String jsonString = messageParser.messageToJsonString(invitationGameMessage);

            mGameConnection.sendMessage(jsonString);

            Toast.makeText(getApplicationContext(), "invitationMessage send", Toast.LENGTH_SHORT).show();
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

    public void advertiseGame() {
        // Register service
        if(mGameConnection.getLocalPort() > -1) {
            mNsdServer.registerService(mGameConnection.getLocalPort());

            Toast.makeText(getApplicationContext(), "Service erstellt: " + mGameConnection.getLocalPort(), Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }


    @Override
    protected void onPause() {
        if (mNsdClient != null) {
            mNsdClient.stopDiscovery();
        }
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdClient != null) {
            mNsdClient.discoverServices();
        }
    }

/*
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop ausgeführt");
        if(mGameConnection != null){
            mGameConnection.tearDown();
        }

        if(mGameConnection != null){
            mGameConnection.tearDown();
        }
        if(mNsdServer != null) {
            mNsdServer.tearDown();
        }
        super.onStop();
    }*/

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy SpielBeitreten");
        if(mNsdServer != null) {
            mNsdServer.tearDown();
        }
        mGameConnection.tearDown();
        super.onDestroy();
    }
}
