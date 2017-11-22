package com.monopoly.domke.sebastian.monopoly;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.monopoly.domke.sebastian.monopoly.helper.GameJobCreator;

/**
 * Created by Basti on 22.11.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new GameJobCreator());
    }
}
