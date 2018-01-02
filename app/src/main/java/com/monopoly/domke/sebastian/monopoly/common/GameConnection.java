package com.monopoly.domke.sebastian.monopoly.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
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
import java.util.ArrayList;

/**
 * Created by Basti on 12.12.2017.
 */

public class GameConnection {

    public GameClient mGameClient;
    public GameServer mGameServer;

    public ArrayList<GameClient> gameClients;

    public Context mContext;

    private int mPort = -1;

    private final String SERVER_TAG = "GameConnection";

    public GameConnection(Context context) {
        gameClients = new ArrayList<>();
        this.mContext = context;
        mGameServer = new GameServer();
    }

    public void tearDownGameServer() {

        Log.d(SERVER_TAG, "tearDownGameServer");

        if(mGameServer != null) {
            mGameServer.tearDown();
        }
    }

    public void tearDownGameClient() {

        Log.d(SERVER_TAG, "tearDownGameClient");

        if(mGameClient != null) {
            mGameClient.tearDown();
        }
    }

    public void connectToServerBySocket(Socket clientSocket) {
        mGameClient = new GameClient(clientSocket);
        gameClients.add(mGameClient);
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

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
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
            try {
                mThread.interrupt();
            } catch (Exception e) {
                Log.e(SERVER_TAG, "Error when interrupting server thread.");
            }
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
                    setLocalPort(mServerSocket.getLocalPort());

                    Log.d(SERVER_TAG, "Server socket Port: " + mServerSocket.getLocalPort() + " Adress:" + mServerSocket.getInetAddress());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(SERVER_TAG, "ServerSocket Created, awaiting connection");

                        Socket newClientSocket = mServerSocket.accept();
                        Log.d(SERVER_TAG, "Connected.");
                        Log.d(SERVER_TAG, "GameClient == null -> connectToServer(" + newClientSocket.getInetAddress() + ", " + newClientSocket.getPort() + ")");

                        connectToServerBySocket(newClientSocket);
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

        private Socket mClientSocket;

        private final String CLIENT_TAG = "GameClient";

        private Thread mRecThread;

        public GameClient(Socket clientSocket) {
            this.mClientSocket = clientSocket;

            Log.d(CLIENT_TAG, "Create GameClientConstructor - mClientSocket.Address: " + mClientSocket.getInetAddress() + " | mClientSocket.Port: " + mClientSocket.getPort());

            broadcaster = LocalBroadcastManager.getInstance(mContext);

            mRecThread = new Thread(new GameClient.ReceivingThread());
            mRecThread.start();
        }


        public synchronized void broadcastMessages(String msg, boolean local) {
            Log.d(CLIENT_TAG, "Broadcast message: " + msg);

            if (!local) {
                Intent messageReceivedIntent = new Intent(BROADCAST_INTENT);
                messageReceivedIntent.putExtra(BROADCAST_INTENT_EXTRA, msg);
                broadcaster.sendBroadcast(messageReceivedIntent);
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mClientSocket.getInputStream()));
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

            while(gameClients.listIterator().hasNext()) {

                GameClient nextGameClient = gameClients.listIterator().next();

                try {
                    nextGameClient.mRecThread.interrupt();

                } catch (Exception e) {
                    Log.d(CLIENT_TAG, "Error when interrupting RecordingThread");
                }
            }
        }

        public void sendMessage(String msg) {
            try {
                //Socket socket = getSocket();
                Socket socket = mClientSocket;
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(mClientSocket.getOutputStream())), true);
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
