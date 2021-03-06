package com.monopoly.domke.sebastian.monopoly.helper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.view.MainMenuActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;

/**
 * Created by Basti on 12.12.2016.
 */

public class NsdHelper {

    Context mContext;

    private SharedPreferences sharedPreferences = null;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdClientGame";
    public String mServiceName = "MonopolyGameClient";
    public String mMonopolyGameServiceName = "MonopolyGameServer";

    NsdServiceInfo mService;

    GameConnection gameConnection;
    MainMenuActivity mainMenuActivity;
    SpielBeitretenActivity spielBeitretenActivity;
    SpielStartActivity spielStartActivity;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

    }

    public NsdHelper(Context context, MainMenuActivity mainMenuActivity) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.mainMenuActivity = mainMenuActivity;
    }

    public NsdHelper(Context context, SpielBeitretenActivity spielBeitretenActivity) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.spielBeitretenActivity = spielBeitretenActivity;
    }

    public NsdHelper(Context context, SpielStartActivity spielStartActivity) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.spielStartActivity = spielStartActivity;
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mMonopolyGameServiceName)){
                    Log.d(TAG, "Service found");
                    //Toast.makeText(mContext, "Spiel gefunden", Toast.LENGTH_SHORT).show();
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }

                mService = serviceInfo;

                //ToDo Client Socket as a service and only if mService.getHost() has a IP

                if (mService != null) {
                    Log.d(TAG, "Connecting.");
                    if(mainMenuActivity != null && mainMenuActivity.mGameConnection == null) {
                        mainMenuActivity.mGameConnection = new GameConnection( mainMenuActivity.mUpdateHandler);
                        mainMenuActivity.mGameConnection.connectToServer(mService.getHost(),
                                mService.getPort());
                    }
                    else if(spielBeitretenActivity != null && spielBeitretenActivity.mGameConnection == null) {
                        spielBeitretenActivity.mGameConnection = new GameConnection( spielBeitretenActivity.mUpdateHandler);
                        spielBeitretenActivity.mGameConnection.connectToServer(mService.getHost(),
                                mService.getPort());
                    }
                    else if(spielStartActivity != null) {
                        spielStartActivity.mGameConnection = new GameConnection( spielStartActivity.mUpdateHandler);
                        spielStartActivity.mGameConnection.connectToServer(mService.getHost(),
                                mService.getPort());
                    }

                    Toast.makeText(mContext, "Mit Spiel verbunden", Toast.LENGTH_SHORT).show();
                    //ToDo Send message for requestJoinGame to server
                } else {
                    Log.d(TAG, "No service to connect to!");
                }
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mMonopolyGameServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        try {
            mNsdManager.unregisterService(mRegistrationListener);
        }catch(Exception e){
            Log.e(TAG, "Unregister service failed");
        }
    }
}
