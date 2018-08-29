package com.monopoly.domke.sebastian.monopoly.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnectionService;
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

    public boolean mServiceBound = false;
    public GameConnectionService mGameConnectionService;

    public DatabaseHandler datasource;
    public GamelobbySpielerAdapter spieler_adapter;
    public Spieler eigenerSpieler;
    private String eigenerSpielerName = "Spieler";
    private int eigenerSpielerFarbe = R.color.weiß_spieler_farbe;
    public ListView gamelobbyListView;
    public Spiel aktuellesSpiel;
    private String ipAdresseEigenerSpieler;
    private String imeiEigenerSpieler;

    public NsdHelper mNsdServer;
    public Handler mUpdateHandler;
    public static final String TAG = "NsdGame";

    boolean neuesSpiel = false;
    boolean spielLaden = false;

    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    private SharedPreferences sharedPreferences = null;

    public static final String SHARED_PREF = "SHARED_PREF";
    public static final String SHARED_PREF_IP_ADRESS = "SHARED_PREF_IP_ADRESS";
    public static final String SHARED_PREF_PORT = "SHARED_PREF_PORT";

    private PlayerMessageInterpreter playerMessageInterpreter;
    private HostMessageInterpreter hostMessageInterpreter;
    public MessageParser messageParser;

    private ArrayList<ImageView> colorImageViewLayouts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_beitreten);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        if(!neuesSpiel && !spielLaden) {

            new Thread(new Runnable() {
                public void run() {

                    try {

                        while(!mServiceBound){
                            Thread.sleep(1000);
                        }

                        JSONObject messageContent = new JSONObject();

                        GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.requestCurrentGameLobby, messageContent);

                        String jsonString = messageParser.messageToJsonString(requestJoinGameMessage);

                        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void init(){
        datasource = new DatabaseHandler(this);

        FloatingActionButton spielStartenFB = (FloatingActionButton) findViewById(R.id.spielStartenFloatingButton);

        messageParser = new MessageParser();
        Intent intent = getIntent();

        String spielDatum = intent.getStringExtra("spiel_datum");
        neuesSpiel = intent.getBooleanExtra("neues_spiel", false);
        spielLaden = intent.getBooleanExtra("spiel_laden", false);

        aktuellesSpiel = datasource.getSpielByDatum(spielDatum);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_INTENT);
        LocalBroadcastManager.getInstance(SpielBeitretenActivity.this).registerReceiver(messageReceiver, filter);

        sharedPreferences = this.getSharedPreferences(SHARED_PREF, 0); // 0 - for private mode

        if(!neuesSpiel && !spielLaden) {
            spielStartenFB.hide();
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            playerMessageInterpreter = new PlayerMessageInterpreter(this);
        }
        else{
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getApplicationContext().startService(gameConnectionServiceIntent);

            mNsdServer = new NsdHelper(this);
            mNsdServer.initializeRegistrationListener();
            hostMessageInterpreter = new HostMessageInterpreter(this);
            advertiseGame();
        }

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        ipAdresseEigenerSpieler = intToInetAddress(manager.getDhcpInfo().ipAddress).getHostAddress();

        imeiEigenerSpieler = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.d(TAG, "IMEI: " + imeiEigenerSpieler);
        Log.d(TAG, "SpielID: " + aktuellesSpiel.getSpielID());

        try{
            eigenerSpieler = datasource.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), imeiEigenerSpieler);
            Log.d(TAG, "Spieler gefunden Init");
            RelativeLayout spielLobbyBeitretenButtonLayout = (RelativeLayout) findViewById(R.id.spielLobbyBeitretenButtonLayout);
            spielLobbyBeitretenButtonLayout.setEnabled(false);
        }catch(Exception e){
            Log.d(TAG, "Spieler nicht gefunden init");

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

                    ArrayList<String> aktivePlayerList = new ArrayList<>();

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

                    mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                    mNsdServer.tearDown();

                    if(mServiceBound) {
                        getApplicationContext().unbindService(mServiceConnection);

                        LocalBroadcastManager.getInstance(SpielBeitretenActivity.this).unregisterReceiver(messageReceiver);
                    }

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
                if(meinNameEditText.getText().toString().length() != 0) {
                    eigenerSpieler.setSpielerName(meinNameEditText.getText().toString());
                }
                else{
                    eigenerSpieler.setSpielerName("Spieler");
                }

                datasource.updateSpieler(eigenerSpieler);
                //spieler_adapter.notifyDataSetChanged();

            }
        });
        meinNameEditText.clearFocus();

        final ImageView meineEinstellungenImageView = (ImageView) findViewById(R.id.meineEinstellungenView);

        final ImageView farbeGelbImageView = (ImageView) findViewById(R.id.spielerFarbeGelbButtonView);
        final ImageView farbeGruenImageView = (ImageView) findViewById(R.id.gegenspieler3ButtonView);
        final ImageView farbeBlauImageView = (ImageView) findViewById(R.id.gegenspieler2ButtonView);
        final ImageView farbeRotImageView = (ImageView) findViewById(R.id.gegenspieler1ButtonView);
        final ImageView farbeGrauImageView = (ImageView) findViewById(R.id.spielerFarbeGrauButtonView);
        final ImageView farbeSchwarzImageView = (ImageView) findViewById(R.id.spielerFarbeWeißButtonView);

        colorImageViewLayouts.add(farbeGelbImageView);
        colorImageViewLayouts.add(farbeGruenImageView);
        colorImageViewLayouts.add(farbeBlauImageView);
        colorImageViewLayouts.add(farbeRotImageView);
        colorImageViewLayouts.add(farbeGrauImageView);
        colorImageViewLayouts.add(farbeSchwarzImageView);

        farbeGelbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.gelb_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                //meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gelb_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_gelb_background);
                changeActivatedButtonState(farbeGelbImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeGruenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.gruen_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gruen_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_dunkel_gruen_background);
                changeActivatedButtonState(farbeGruenImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeBlauImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.blau_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                //meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blau_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_dunkel_blau_background);
                changeActivatedButtonState(farbeBlauImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeRotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.rot_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                //meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rot_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_rot_background);
                changeActivatedButtonState(farbeRotImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeGrauImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.grau_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
               // meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grau_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_braun_background);
                changeActivatedButtonState(farbeGrauImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        farbeSchwarzImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerFarbe(R.color.weiß_spieler_farbe);
                datasource.updateSpieler(eigenerSpieler);
                //meineEinstellungenImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.weiß_spieler_farbe));
                meineEinstellungenImageView.setBackgroundResource(R.drawable.layout_circle_grau_background);
                changeActivatedButtonState(farbeSchwarzImageView);
                //spieler_adapter.notifyDataSetChanged();
            }
        });

        gamelobbyListView = (ListView) findViewById(R.id.gameLobbyList);

        ArrayList<Spieler> values = datasource.getAllSpielerBySpielID(aktuellesSpiel.getSpielID());

        spieler_adapter = new GamelobbySpielerAdapter(this,
                R.layout.list_item_spieler, values, this);
        gamelobbyListView.setAdapter(spieler_adapter);

    }

    public void changeActivatedButtonState(ImageView selectedImageView) {

        for (ImageView imageView: colorImageViewLayouts) {
            if(imageView == selectedImageView) {
                selectedImageView.setActivated(true);
            } else {
                imageView.setActivated(false);
            }
        }

    }

    public BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BROADCAST_INTENT:
                    String msg = intent.getStringExtra(BROADCAST_INTENT_EXTRA);
                    try {
                        GameMessage message = messageParser.jsonStringToMessage(msg);

                        //Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                        if (!neuesSpiel) {
                            //Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                            playerMessageInterpreter.decideWhatToDoWithTheMassage(message);
                        } else {
                            //Toast.makeText(getApplicationContext(), "Host Message Handler", Toast.LENGTH_SHORT).show();
                            hostMessageInterpreter.decideWhatToDoWithTheMassage(message);
                        }

                    }catch (Exception e){
                        Log.d(TAG, "Keine passende Nachricht");
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), "Keine passende Nachricht", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void spielLobbyBeitreten(View view) {

        RelativeLayout spielLobbyBeitretenButtonLayout = (RelativeLayout) findViewById(R.id.spielLobbyBeitretenButtonLayout);

        GamelobbySpielerAdapter adapter = (GamelobbySpielerAdapter) gamelobbyListView.getAdapter();

        EditText eigenerSpielerNameEditText = (EditText) findViewById(R.id.meinSpielerNameEditText);

        if(eigenerSpielerNameEditText.getText().toString().length() != 0) {
            eigenerSpieler.setSpielerName(eigenerSpielerNameEditText.getText().toString());
        }

        adapter.add(eigenerSpieler);

        Log.d(TAG, "SpiellobbyBeitreten");
        Log.d(TAG, "IMEI: " + imeiEigenerSpieler);
        Log.d(TAG, "SpielID: " + aktuellesSpiel.getSpielID());

        try{
            datasource.getSpielerBySpielIdAndSpielerIMEI(eigenerSpieler.getIdMonopolySpiel(), eigenerSpieler.getSpielerIMEI());
            Log.d(TAG, "Spieler gefunden beitreten");
            datasource.updateSpieler(eigenerSpieler);
            Log.d(TAG, "Spieler update erfolgreich");
        }catch(Exception e){
            Log.d(TAG, "Spieler nicht gefunden beitreten");

            datasource.addSpieler(eigenerSpieler);
        }

        JSONObject messageContent = messageParser.playerStatusToJson(eigenerSpieler, aktuellesSpiel);

        GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.joinGame, messageContent);

        String jsonString = messageParser.messageToJsonString(requestJoinGameMessage);

        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

        spielLobbyBeitretenButtonLayout.setEnabled(false);

        Toast.makeText(getApplicationContext(), "Du bist der Spiellobby beigetreten!", Toast.LENGTH_SHORT).show();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(eigenerSpielerNameEditText.getWindowToken(), 0);
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

            mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

            Toast.makeText(getApplicationContext(), "Spieler wurden eingeladen", Toast.LENGTH_SHORT).show();
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

        new Thread(new Runnable() {
            public void run() {

                while (!mServiceBound){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Register service
                if(mGameConnectionService.mGameConnection.getLocalPort() > -1) {
                    mNsdServer.registerService(mGameConnectionService.mGameConnection.getLocalPort());
                    //Toast.makeText(getApplicationContext(), "Service erstellt: " + port, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "ServerSocket isn't bound.");
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");
        if(mNsdServer != null) {
            mNsdServer.tearDown();
        }
        super.onDestroy();
    }

    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mGameConnectionService = null;
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GameConnectionService.MyBinder myBinder = (GameConnectionService.MyBinder) service;
            mGameConnectionService = myBinder.getService();
            mServiceBound = true;
        }
    };

    @Override
    public void onBackPressed() {

        if(neuesSpiel) {
            JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

            GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.gameAbort, messageContent);

            String jsonString = messageParser.messageToJsonString(startGameMessage);

            mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

            datasource.deleteSpiel(aktuellesSpiel.getSpielDatum());

            Toast.makeText(getApplicationContext(), "Spiel wird geschlossen...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), NeuesSpielActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if(mServiceBound) {

                mGameConnectionService.mGameConnection.tearDownGameServer();
                mGameConnectionService.mGameConnection.tearDownGameClient();

                Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
                getApplicationContext().stopService(gameConnectionServiceIntent);
                getApplicationContext().unbindService(mServiceConnection);

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                broadcastManager.unregisterReceiver(messageReceiver);
            }

            startActivity(intent);
        }
        else if(spielLaden) {
            JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

            GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.gameAbort, messageContent);

            String jsonString = messageParser.messageToJsonString(startGameMessage);

            mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

            Toast.makeText(getApplicationContext(), "Spiel wird geschlossen...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), SpielLadenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if(mServiceBound) {

                mGameConnectionService.mGameConnection.tearDownGameServer();
                mGameConnectionService.mGameConnection.tearDownGameClient();

                Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
                getApplicationContext().stopService(gameConnectionServiceIntent);
                getApplicationContext().unbindService(mServiceConnection);

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                broadcastManager.unregisterReceiver(messageReceiver);

            }

            startActivity(intent);
        }
        else {

            JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

            GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.leaveGame, messageContent);

            String jsonString = messageParser.messageToJsonString(startGameMessage);

            mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

            datasource.deleteSpiel(aktuellesSpiel.getSpielDatum());

            if(mServiceBound) {

                mGameConnectionService.mGameConnection.tearDownGameServer();
                mGameConnectionService.mGameConnection.tearDownGameClient();

                Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
                getApplicationContext().stopService(gameConnectionServiceIntent);
                getApplicationContext().unbindService(mServiceConnection);

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                broadcastManager.unregisterReceiver(messageReceiver);
            }

            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        }

    }
}
