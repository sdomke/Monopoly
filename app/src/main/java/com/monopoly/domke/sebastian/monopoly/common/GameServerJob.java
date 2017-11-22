package com.monopoly.domke.sebastian.monopoly.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Basti on 22.11.2017.
 */

public class GameServerJob extends Job {

    public static final String TAG = "job_game_server_tag";
    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    private LocalBroadcastManager broadcaster;
    public GameServer mGameServer;
    public GameClient mGameClient;

    public Socket mSocket;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        broadcaster = LocalBroadcastManager.getInstance(getContext());

        mGameServer = new GameServer();
        return Result.SUCCESS;
    }

    public void scheduleJob() {

        new JobRequest.Builder(GameServerJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

    public void tearDown() {

        Log.d(TAG, "tearDown");

        if(mGameServer != null) {
            mGameServer.tearDown();
        }

        if(mGameClient != null) {
            mGameClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        mGameClient = new GameClient(address, port);
    }

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

    private class GameServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;
        private final String SERVER_TAG = "GameServer";

        public GameServer() {
            mThread = new Thread(new GameServer.ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(SERVER_TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);

                    Log.d(SERVER_TAG, "Server socket Port: " + mServerSocket.getLocalPort() + " Adress:" + mServerSocket.getInetAddress());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(SERVER_TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(SERVER_TAG, "Connected.");

                        Log.d(SERVER_TAG, "GameClient == null -> connectToServer(" +  mSocket.getInetAddress() + ", " + mSocket.getPort() + ")");
                        int port = mSocket.getPort();
                        InetAddress address = mSocket.getInetAddress();
                        connectToServer(address, port);
                    }
                } catch (IOException e) {
                    Log.e(SERVER_TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class GameClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "GameClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public GameClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating GameClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new GameClient.SendingThread());
            mSendThread.start();
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