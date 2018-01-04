package com.monopoly.domke.sebastian.monopoly.common;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Basti on 15.12.2017.
 */

public class GameConnectionService extends Service {

    private static String LOG_TAG = "GameConnectionService";
    private IBinder mBinder;
    public GameConnection mGameConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "in onCreate");
        mGameConnection = new GameConnection(getApplicationContext());
        mBinder = new MyBinder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "in onDestroy");

        mGameConnection.gameClientsArrayList = null;
        mGameConnection = null;
    }

    public class MyBinder extends Binder {
        public GameConnectionService getService() {
            return GameConnectionService.this;
        }
    }
}
