package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.helper.NsdHelper;

import java.io.Serializable;

public class MainMenuActivity extends AppCompatActivity {

    private NsdHelper mNsdHelper;
    private Handler mUpdateHandler;
    private GameConnection mGameConnection;

    private RelativeLayout spielBeitretenRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spielBeitretenRelativeLayout = (RelativeLayout) findViewById(R.id.spielBeitretenButtonLayout);

        spielBeitretenRelativeLayout.setEnabled(false);

        mUpdateHandler = new Handler();

        mGameConnection = new GameConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        mNsdHelper.discoverServices();


    }

    public void neuesSpiel(View view) {

        Bundle bundle = new Bundle();

        bundle.putSerializable("GameConnection", mGameConnection);
        bundle.putSerializable("NsdHelper", mNsdHelper);

        Intent intent = new Intent(this, NeuesSpielActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    //todo ausblenden solange keine Einladung
    public void spielBeitreten(View view) {

        Intent intent = new Intent(this, SpielBeitretenActivity.class);
        intent.putExtra("GameConnection", mGameConnection);
        intent.putExtra("NsdHelper", mNsdHelper);
        startActivity(intent);

    }

    public void spielLaden(View view) {

        Intent intent = new Intent(this, SpielLadenActivity.class);
        intent.putExtra("GameConnection", mGameConnection);
        intent.putExtra("NsdHelper", mNsdHelper);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mGameConnection.tearDown();
        super.onDestroy();
    }
}
