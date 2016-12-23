package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.MessageParser;
import com.monopoly.domke.sebastian.monopoly.helper.NsdHelper;
import com.monopoly.domke.sebastian.monopoly.helper.PlayerMessageInterpreter;

import org.json.JSONObject;

import java.io.Serializable;

public class MainMenuActivity extends AppCompatActivity {

    private NsdHelper mNsdHelper;
    private Handler mUpdateHandler;
    public GameConnection mGameConnection;
    public DatabaseHandler datasource;
    public Spiel neuesSpiel;
    private SharedPreferences sharedPreferences;

    private PlayerMessageInterpreter playerMessageInterpreter;
    public MessageParser messageParser;
    private RelativeLayout spielBeitretenRelativeLayout;

    public static final String TAG = "NsdGame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datasource = new DatabaseHandler(this);

        playerMessageInterpreter = new PlayerMessageInterpreter(this);
        messageParser = new MessageParser();

        sharedPreferences = getSharedPreferences("monopoly", MODE_PRIVATE);

        /*
        sharedPreferences.edit().putBoolean("service_discovered", false).commit();

        spielBeitretenRelativeLayout = (RelativeLayout) findViewById(R.id.spielBeitretenButtonLayout);

        if(!sharedPreferences.getBoolean("service_discovered", false)) {
            spielBeitretenRelativeLayout.setEnabled(false);
        }*/

        spielBeitretenRelativeLayout = (RelativeLayout) findViewById(R.id.spielBeitretenButtonLayout);
        spielBeitretenRelativeLayout.setEnabled(false);

        mUpdateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();

                GameMessage message = messageParser.jsonStringToMessage(msg.getData().getString("msg"));

                playerMessageInterpreter.decideWhatToDoWithTheMassage(message);
            }
        };

        mGameConnection = new GameConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(getApplicationContext(), this);
        mNsdHelper.initializeNsd();

    }

    public void neuesSpiel(View view) {
        Intent intent = new Intent(this, NeuesSpielActivity.class);
        startActivity(intent);

    }

    public void spielBeitreten(View view) {
        Intent intent = new Intent(this, SpielBeitretenActivity.class);
        intent.putExtra("spiel_datum", neuesSpiel.getSpielDatum());
        startActivity(intent);
    }

    public void spielLaden(View view) {

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {
            Toast.makeText(getApplicationContext(), "Überprüfen, ob ein Spiel erstellt wurde", Toast.LENGTH_SHORT).show();

            connectToGame();

            JSONObject messageContent = new JSONObject();

            GameMessage invitePlayerGameMessage = new GameMessage(GameMessage.MessageHeader.requestJoinGame, messageContent);

            String jsonString = messageParser.messageToJsonString(invitePlayerGameMessage);

            mGameConnection.sendMessage(jsonString);
            Toast.makeText(getApplicationContext(), "invitePlayerGameMessage send", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mGameConnection.tearDown();
        super.onDestroy();
    }

    public void connectToGame() {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mGameConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

}
