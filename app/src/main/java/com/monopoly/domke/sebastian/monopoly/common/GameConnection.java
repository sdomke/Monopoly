package com.monopoly.domke.sebastian.monopoly.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Basti on 12.12.2017.
 */

public class GameConnection {

    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    public GameClient mGameClient;
    public GameServer mGameServer;

    public Socket mSocket;
    public Context mContext;

    private final String SERVER_TAG = "GameConnection";

    public GameConnection(Context context) {

        this.mContext = context;
        mGameServer = new GameServer();
    }

    public void tearDown() {

        Log.d(SERVER_TAG, "tearDown");

        if(mGameServer != null) {
            mGameServer.tearDown();
        }

        if(mGameClient != null) {
            mGameClient.tearDown();
        }
    }

    public void tearDownGameClient() {

        Log.d(SERVER_TAG, "tearDownGameClient");

        if(mGameClient != null) {
            mGameClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        mGameClient = new GameClient(address, port);
    }

    public void sendMessage(final String msg) {
        if (mGameClient != null) {
            new Thread(new Runnable() {
                public void run() {
                    mGameClient.sendMessage(msg);
                }
            }).start();


        }
    }

    private synchronized void setSocket(Socket socket) {

        Log.d(SERVER_TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(SERVER_TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.d(SERVER_TAG, "Can't close socket");
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
        Log.d(SERVER_TAG, "Client socket Port: " + mSocket.getPort() + " Adress:" + mSocket.getInetAddress());
    }

    public Socket getSocket() {
        return mSocket;
    }

    private class GameServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;
        private final String SERVER_TAG = "GameServer";

        public GameServer() {
            mThread = new Thread(new ServerThread());
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

            public static final String SHARED_PREF = "SHARED_PREF";
            public static final String SHARED_PREF_IP_ADRESS = "SHARED_PREF_IP_ADRESS";
            public static final String SHARED_PREF_PORT = "SHARED_PREF_PORT";

            private SharedPreferences sharedPreferences = null;
            private SharedPreferences.Editor editor;

            @Override
            public void run() {

                sharedPreferences = mContext.getSharedPreferences(SHARED_PREF, 0); // 0 - for private mode
                editor = sharedPreferences.edit();

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);

                    editor.putInt(SHARED_PREF_PORT, mServerSocket.getLocalPort());
                    editor.apply();

                    Log.d(SERVER_TAG, "Server socket Port: " + mServerSocket.getLocalPort() + " Adress:" + mServerSocket.getInetAddress());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(SERVER_TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(SERVER_TAG, "Connected.");

                        Log.d(SERVER_TAG, "GameClient == null -> connectToServer(" + mSocket.getInetAddress() + ", " + mSocket.getPort() + ")");
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

        private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
        private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

        private LocalBroadcastManager broadcaster;

        private InetAddress mAddress;
        private int port;

        private final String CLIENT_TAG = "GameClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public GameClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating GameClient");
            this.mAddress = address;
            this.port = port;

            broadcaster = LocalBroadcastManager.getInstance(mContext);

            mSendThread = new Thread(new GameClient.SendingThread());
            mSendThread.start();
        }


        public synchronized void broadcastMessages(String msg, boolean local) {
            Log.d(CLIENT_TAG, "Broadcast message: " + msg);

            if (!local) {
                Intent messageReceivedIntent = new Intent(BROADCAST_INTENT);
                messageReceivedIntent.putExtra(BROADCAST_INTENT_EXTRA, msg);
                broadcaster.sendBroadcast(messageReceivedIntent);
            }
        }

        class SendingThread implements Runnable {

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress.getHostAddress(), port));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
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
                            broadcastMessages(messageStr, false);
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
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error when interrupting SendThread");
            }
            try {
                mRecThread.interrupt();
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error when interrupting RecordingThread");
            }

            try {
                getSocket().getInputStream().close();
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error when closing inputStream");
            }

            try {
                getSocket().getOutputStream().close();
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error when interrupting RecordingThread");
            }

            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing client socket.");
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();

                broadcastMessages(msg, true);
                //updateMessages(msg, true);
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }

    }
}
