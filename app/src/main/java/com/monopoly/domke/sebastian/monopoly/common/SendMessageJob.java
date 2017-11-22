package com.monopoly.domke.sebastian.monopoly.common;

import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Basti on 22.11.2017.
 */

public class SendMessageJob extends Job {

    public static final String TAG = "job_send_message_tag";
    private final String CLIENT_TAG = "GameClient";
    private LocalBroadcastManager broadcaster;

    private static final String EXTRA_IP_ADRESS = "EXTRA_IP_ADRESS";
    private static final String EXTRA_PORT = "EXTRA_PORT";
    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public Socket mSocket;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        String ipAdress = params.getExtras().getString(EXTRA_IP_ADRESS, "No Adress");
        int port = params.getExtras().getInt(EXTRA_PORT, -1);
        String message = params.getExtras().getString(EXTRA_IP_ADRESS, "No Message");

        try {
            setSocket(new Socket(InetAddress.getByName(ipAdress), port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(message);

        return Result.SUCCESS;
    }

    public void scheduleJob(InetAddress address, int port, String message) {

        String ipAdress = address.getHostAddress();

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString(EXTRA_IP_ADRESS, ipAdress);
        extras.putString(EXTRA_MESSAGE, message);
        extras.putInt(EXTRA_PORT, port);

        new JobRequest.Builder(SendMessageJob.TAG)
                .setExtras(extras)
                .startNow()
                .build()
                .schedule();
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

    public void sendMessage(final String msg) {

        new Thread(new Runnable() {
            public void run() {
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
                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Unknown Host", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "I/O Exception", e);
                } catch (Exception e) {
                    Log.d(CLIENT_TAG, "Error3", e);
                }
                Log.d(CLIENT_TAG, "Client sent message: " + msg);
            }
        }).start();
    }
}
