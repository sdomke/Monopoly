package com.monopoly.domke.sebastian.monopoly.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnectionService;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.MessageParser;
import com.monopoly.domke.sebastian.monopoly.helper.NsdHelper;
import com.monopoly.domke.sebastian.monopoly.helper.PlayerMessageInterpreter;

import org.json.JSONObject;

public class MainMenuActivity extends AppCompatActivity {

    private NsdHelper mNsdClient;
    public DatabaseHandler datasource;
    public Spiel neuesSpiel;

    private PlayerMessageInterpreter playerMessageInterpreter;
    public MessageParser messageParser;
    public RelativeLayout spielBeitretenRelativeLayout;

    public boolean mServiceBound = false;
    public GameConnectionService mGameConnectionService;

    public static final String TAG = "NsdGame";

    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_account_balance_black_24dp);
        setSupportActionBar(toolbar);


        Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
        getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        getApplicationContext().startService(gameConnectionServiceIntent);

        datasource = new DatabaseHandler(this);

        playerMessageInterpreter = new PlayerMessageInterpreter(this);
        messageParser = new MessageParser();
        spielBeitretenRelativeLayout = (RelativeLayout) findViewById(R.id.spielBeitretenButtonLayout);

        spielBeitretenRelativeLayout.setEnabled(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_INTENT);
        LocalBroadcastManager.getInstance(MainMenuActivity.this).registerReceiver(messageReceiver, filter);

        mNsdClient = new NsdHelper(getApplicationContext(), this);
        mNsdClient.initializeDiscoveryListener();
        mNsdClient.initializeResolveListener();

        Log.d(TAG, "onCreate");
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
                        playerMessageInterpreter.decideWhatToDoWithTheMassage(message);

                    }catch (Exception e){
                        Log.d(TAG, "Keine passende Nachricht");
                        //Toast.makeText(getApplicationContext(), "Keine passende Nachricht", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void neuesSpiel(View view) {

        if(mServiceBound) {
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().stopService(gameConnectionServiceIntent);
            getApplicationContext().unbindService(mServiceConnection);
        }

        Intent intent = new Intent(this, NeuesSpielActivity.class);
        startActivity(intent);

    }

    public void spielBeitreten(View view) {

        if(mServiceBound) {
            getApplicationContext().unbindService(mServiceConnection);
        }

        Intent intent = new Intent(this, SpielBeitretenActivity.class);
        intent.putExtra("spiel_datum", neuesSpiel.getSpielDatum());
        startActivity(intent);
    }

    public void spielLaden(View view) {

        if(mServiceBound) {
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().stopService(gameConnectionServiceIntent);
            getApplicationContext().unbindService(mServiceConnection);
        }

        Intent intent = new Intent(this, SpielLadenActivity.class);
        startActivity(intent);

    }

    public void einstellungen(View view) {

        Intent intent = new Intent(this, EinstellungenActivity.class);
        startActivity(intent);

    }

    public void highscore(View view) {

        Intent intent = new Intent(this, HighscoreActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send) {
            JSONObject messageContent = new JSONObject();

            if(mNsdClient.mServiceResolved) {
                GameMessage requestJoinGameMessage = new GameMessage(GameMessage.MessageHeader.requestJoinGame, messageContent);

                String jsonString = messageParser.messageToJsonString(requestJoinGameMessage);

                mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                Toast.makeText(getApplicationContext(), "Einladung zum Spiel angefordert", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Es muss erst ein neues Spiel gestartet werden, bevor du dich damit verbinden kannst", Toast.LENGTH_SHORT).show();
            }

        }

        return super.onOptionsItemSelected(item);
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

        Log.d(TAG, "onResume");

        Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
        getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        getApplicationContext().startService(gameConnectionServiceIntent);

        spielBeitretenRelativeLayout.setEnabled(false);
        if (mNsdClient != null) {
            mNsdClient.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
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
            Log.d(TAG, "onServiceConnected");
            GameConnectionService.MyBinder myBinder = (GameConnectionService.MyBinder) service;
            mGameConnectionService = myBinder.getService();
            mServiceBound = true;
        }
    };

}
