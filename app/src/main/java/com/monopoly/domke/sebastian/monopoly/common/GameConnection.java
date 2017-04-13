package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 12.12.2016.
 */

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameConnection {

    public Handler mUpdateHandler;
    public GameServer mGameServer;
    public GameClient mGameClient;

    private static final String TAG = "GameConnection";

    public Socket mSocket;
    private int mPort = -1;

    public GameConnection(Handler handler) {
        mUpdateHandler = handler;
        mGameServer = new GameServer(handler);
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


    public synchronized void updateMessages(String msg, boolean local) {
        Log.d(TAG, "Updating message: " + msg);

        /*if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }*/

        if (!local) {
            Bundle messageBundle = new Bundle();
            messageBundle.putString("msg", msg);

            Message message = new Message();
            message.setData(messageBundle);
            mUpdateHandler.sendMessage(message);
        }
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
                    // TODO(alexlucas): Auto-generated catch block
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

    private class GameServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;
        private final String SERVER_TAG = "GameServer";

        public GameServer(Handler handler) {
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

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

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

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing client-side socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing client-side socket failed, IOE.", e);
                }

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Client-side Message sending loop interrupted, exiting");
                    }
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
                            updateMessages(messageStr, false);
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
                updateMessages(msg, true);
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

