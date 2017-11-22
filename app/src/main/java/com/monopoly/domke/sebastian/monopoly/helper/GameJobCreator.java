package com.monopoly.domke.sebastian.monopoly.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.monopoly.domke.sebastian.monopoly.common.GameClientJob;
import com.monopoly.domke.sebastian.monopoly.common.GameServerJob;
import com.monopoly.domke.sebastian.monopoly.common.SendMessageJob;

/**
 * Created by Basti on 22.11.2017.
 */

public class GameJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case GameClientJob.TAG:
                return new GameClientJob();
            case GameServerJob.TAG:
                return new GameServerJob();
            case SendMessageJob.TAG:
                return new SendMessageJob();
            default:
                return null;
        }
    }
}
