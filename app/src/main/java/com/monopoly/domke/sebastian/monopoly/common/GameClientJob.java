package com.monopoly.domke.sebastian.monopoly.common;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

/**
 * Created by Basti on 22.11.2017.
 */

public class GameClientJob extends Job {

    public static final String TAG = "job_game_client_tag";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        return null;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(GameClientJob.TAG)
                .startNow()
                .build()
                .schedule();
    }
}
