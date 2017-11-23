package com.monopoly.domke.sebastian.monopoly.common;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Basti on 22.11.2017.
 */

public class GameClientJob extends Job {

    private LocalBroadcastManager broadcaster;
    public Socket mSocket;

    public static final String TAG = "job_game_client_tag";
    private static final String EXTRA_IP_ADRESS = "EXTRA_IP_ADRESS";
    private static final String EXTRA_PORT = "EXTRA_PORT";
    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        String ipAdress = params.getExtras().getString(EXTRA_IP_ADRESS, "No Adress");
        int port = params.getExtras().getInt(EXTRA_PORT, -1);

        try {
            new GameClient(InetAddress.getByName(ipAdress), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public static void scheduleGameClientJob(String ipAdress, int port) {

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString(EXTRA_IP_ADRESS, ipAdress);
        extras.putInt(EXTRA_PORT, port);

        new JobRequest.Builder(GameClientJob.TAG)
                .setExtras(extras)
                .startNow()
                .build()
                .schedule();
    }

    private class GameClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "GameClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public GameClient(InetAddress address, int port) {

            broadcaster = LocalBroadcastManager.getInstance(getContext());

            Log.d(CLIENT_TAG, "Creating GameClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new GameClient.SendingThread());
            mSendThread.start();
        }

/*        public void tearDown() {

            Log.d(TAG, "tearDown");

            if(mGameClient != null) {
                mGameClient.tearDown();
            }
        }*/

        private synchronized void setSocket(Socket socket) {
            Log.d(TAG, "setSocket being called.");
            if (socket == null) {
                Log.d(TAG, "Setting a null socket.");
            }
            if (mSocket != null) {
                if (mSocket.isConnected()) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        Log.d(TAG, "Can't close socket");
                        e.printStackTrace();
                    }
                }
            }
            mSocket = socket;

            Log.d(TAG, "Client socket Port: " + mSocket.getPort() + " Adress:" + mSocket.getInetAddress());
        }

        public Socket getSocket() {
            return mSocket;
        }

        public synchronized void broadcastMessages(String msg) {
            Log.d(TAG, "Broadcast message: " + msg);

            Intent messageReceivedIntent = new Intent(BROADCAST_INTENT);
            messageReceivedIntent.putExtra(BROADCAST_INTENT_EXTRA, msg);
            broadcaster.sendBroadcast(messageReceivedIntent);
        }

        class SendingThread implements Runnable {

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    }
                    else {
                        Log.d(CLIENT_TAG, "Client-side socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new GameClient.ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing client-side socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing client-side socket failed, IOE.", e);
                }

            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            getSocket().getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
                            // ToDo Broadcastreceiver aufrufen
                            broadcastMessages(messageStr);
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Client-side Server loop error: ", e);
                }
            }
        }

        public void tearDown() {
            try {
                mSendThread.interrupt();
            }catch(Exception e){
                Log.d(CLIENT_TAG, "Error when interrupting SendThread");
            }
            try {
                mRecThread.interrupt();
            }catch(Exception e){
                Log.d(CLIENT_TAG, "Error when interrupting RecordingThread");
            }

            try {
                getSocket().getInputStream().close();
            }catch(Exception e){
                Log.d(CLIENT_TAG, "Error when closing inputStream");
            }

            try {
                getSocket().getOutputStream().close();
            }catch(Exception e){
                Log.d(CLIENT_TAG, "Error when interrupting RecordingThread");
            }

            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing client socket.");
            }
        }
    }
}
