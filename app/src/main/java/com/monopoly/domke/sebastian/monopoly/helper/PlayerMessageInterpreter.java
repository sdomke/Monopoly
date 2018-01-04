package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnectionService;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage.MessageHeader;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.MainMenuActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;

import org.json.JSONException;

import java.util.ArrayList;

import static com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity.TAG;


/**
 * Created by Basti on 22.12.2016.
 */

//Todo Sound und Vibrationsfeedback
public class PlayerMessageInterpreter {

    private SpielBeitretenActivity spielBeitretenActivity;
    private SpielStartActivity spielStartActivity;
    private MainMenuActivity mainMenuActivity;

    public PlayerMessageInterpreter(MainMenuActivity activity){

        this.mainMenuActivity = activity;
    }

    public PlayerMessageInterpreter(SpielBeitretenActivity activity){

        this.spielBeitretenActivity = activity;
    }

    public PlayerMessageInterpreter(SpielStartActivity activity){

        this.spielStartActivity = activity;
    }

    public void decideWhatToDoWithTheMassage(GameMessage gameMessage){

        MessageHeader messageHeader = gameMessage.getMessageHeader();

        switch(messageHeader){
            case invitation:
                getInvitationMessage(gameMessage);
                break;

            case gameStart:
                getGameStartMessage();
                break;

            case gameEnd:
                getGameEndMessage();
                break;

            case gameAbort:
                getGameAbortMessage();
                break;

            case sendMoney:
                getSendMoneyMessage(gameMessage);
                break;

            case receiveMoneyFromBank:
                getReceiveMoneyFromBankMessage(gameMessage);
                break;

            case receiveFreiParken:
                getReceiveFreiParkenMessage(gameMessage);
                break;

            case sendMoneyToBank:
                getSendMoneyToBankMessage(gameMessage);
                break;

            case sendMoneyToFreiParken:
                getSendMoneyToFreiParkenMessage(gameMessage);
                break;

            case joinGame:
                getJoinGameMessage(gameMessage);
                break;

            case exitGame:
                getExitGameMessage(gameMessage);
                break;
        }
    }

    public void getInvitationMessage(GameMessage gameMessage){
        if(mainMenuActivity != null){
            try {

                Spiel neuesSpiel;

                try{
                    neuesSpiel = mainMenuActivity.datasource.getSpielByDatum(gameMessage.getMessageContent().getString("game_date"));
                }catch(Exception e){
                    Log.e("MessageInterpreter", "Spiel noch nicht angelegt: " + e.toString());

                    neuesSpiel = new Spiel(gameMessage.getMessageContent().getString("game_date"));
                    neuesSpiel.setSpielerStartkapital(gameMessage.getMessageContent().getDouble("game_seed_capital"));
                    neuesSpiel.setSpielerAnzahl(gameMessage.getMessageContent().getInt("game_player_count"));
                    neuesSpiel.setFreiParken(0);
                    neuesSpiel.setWaehrung(gameMessage.getMessageContent().getString("game_currency"));
                    mainMenuActivity.datasource.addNewMonopolyGame(neuesSpiel);
                }

                mainMenuActivity.neuesSpiel = neuesSpiel;
                RelativeLayout spielBeitretenRelativeLayout = (RelativeLayout) mainMenuActivity.findViewById(R.id.spielBeitretenButtonLayout);

                spielBeitretenRelativeLayout.setEnabled(true);

                //Toast.makeText(mainMenuActivity.getApplicationContext(), "Spieler eingeladen!", Toast.LENGTH_SHORT).show();
                Toast.makeText(mainMenuActivity.getApplicationContext(), "Mit Spiel verbunden und du kannst nun beitreten", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getInvitationMessage: " + e.toString());
                //Toast.makeText(mainMenuActivity.getApplicationContext(), "Spieler nicht eingeladen!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getGameStartMessage(){
        if(spielBeitretenActivity != null){
            try {

                ArrayList<String> aktivePlayerList = new ArrayList<String>();

                for (Spieler spieler : spielBeitretenActivity.spieler_adapter.objects) {
                    aktivePlayerList.add(spieler.getSpielerIMEI());
                }

                Intent intent = new Intent(spielBeitretenActivity.getApplicationContext(), SpielStartActivity.class);
                intent.putStringArrayListExtra("aktive_spieler", aktivePlayerList);
                intent.putExtra("aktuellesSpielID", spielBeitretenActivity.aktuellesSpiel.getSpielID());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spiel wird gestartet...", Toast.LENGTH_SHORT).show();

                if(spielBeitretenActivity.mServiceBound) {
                    spielBeitretenActivity.getApplicationContext().unbindService(spielBeitretenActivity.mServiceConnection);
                }

                spielBeitretenActivity.startActivity(intent);

            }catch(Exception e){
                Log.e("MessageInterpreter", "getGameStartMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spiel nicht gestartet!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getGameEndMessage(){
        if(spielStartActivity != null){
            try {

                Intent intent = new Intent(spielStartActivity.getApplicationContext(), MainMenuActivity.class);
                intent.putExtra("spiel_beendet", "Spiel beendet");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Spiel wird beendet...", Toast.LENGTH_SHORT).show();

                if(spielStartActivity.mServiceBound) {
                    Intent gameConnectionServiceIntent = new Intent(spielStartActivity.getApplicationContext(), GameConnectionService.class);
                    spielStartActivity.getApplicationContext().stopService(gameConnectionServiceIntent);
                    spielStartActivity.getApplicationContext().unbindService(spielStartActivity.mServiceConnection);
                }
                spielStartActivity.startActivity(intent);

            }catch(Exception e){
                Log.e("MessageInterpreter", "Spiel beendet: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Spiel nicht beendet!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getGameAbortMessage(){
        if(spielBeitretenActivity != null) {

            try {

                spielBeitretenActivity.datasource.deleteSpiel(spielBeitretenActivity.aktuellesSpiel.getSpielDatum());

                Intent intent = new Intent(spielBeitretenActivity.getApplicationContext(), MainMenuActivity.class);
                intent.putExtra("spiel_beendet", "Spiel beendet");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spiel wird geschlossen...", Toast.LENGTH_SHORT).show();

                if(spielBeitretenActivity.mServiceBound) {
                    Intent gameConnectionServiceIntent = new Intent(spielBeitretenActivity.getApplicationContext(), GameConnectionService.class);
                    spielBeitretenActivity.getApplicationContext().stopService(gameConnectionServiceIntent);
                    spielBeitretenActivity.getApplicationContext().unbindService(spielBeitretenActivity.mServiceConnection);
                }

                spielBeitretenActivity.startActivity(intent);

            }catch(Exception e){
                Log.e("MessageInterpreter", "Spiel nicht beendet: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spiel nicht geschlossen!!!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(mainMenuActivity != null) {
            try {

                if(mainMenuActivity.mServiceBound) {
                    Intent gameConnectionServiceIntent = new Intent(mainMenuActivity.getApplicationContext(), GameConnectionService.class);
                    mainMenuActivity.getApplicationContext().stopService(gameConnectionServiceIntent);
                    mainMenuActivity.getApplicationContext().unbindService(mainMenuActivity.mServiceConnection);
                }

                mainMenuActivity.spielBeitretenRelativeLayout.setEnabled(false);

                Intent gameConnectionServiceIntent = new Intent(mainMenuActivity.getApplicationContext(), GameConnectionService.class);
                mainMenuActivity.getApplicationContext().bindService(gameConnectionServiceIntent, mainMenuActivity.mServiceConnection, Context.BIND_AUTO_CREATE);
                mainMenuActivity.getApplicationContext().startService(gameConnectionServiceIntent);

                Toast.makeText(mainMenuActivity.getApplicationContext(), "Spiel wird geschlossen...", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "Spiel nicht beendet: " + e.toString());
                Toast.makeText(mainMenuActivity.getApplicationContext(), "Spiel nicht geschlossen!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void getSendMoneyMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("sender_imei").equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                        double payment = gameMessage.getMessageContent().getDouble("payment");

                        Spieler senderPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("sender_imei"));

                        Spieler receiverPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("receiver_imei"));

                        senderPlayer.setSpielerKapital(roundDouble(senderPlayer.getSpielerKapital() - payment));
                        receiverPlayer.setSpielerKapital(roundDouble(receiverPlayer.getSpielerKapital() + payment));

                        spielStartActivity.databaseHandler.updateSpieler(senderPlayer);
                        spielStartActivity.databaseHandler.updateSpieler(receiverPlayer);

                        updatePlayerCredit(senderPlayer, receiverPlayer);

                        Toast.makeText(spielStartActivity.getApplicationContext(), senderPlayer.getSpielerName() + " hat " + receiverPlayer.getSpielerName() + " " + payment + " M$ überwiesen!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                        Toast.makeText(spielStartActivity.getApplicationContext(), "Überweisung nicht erfolgreich!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Überweisung nicht erfolgreich!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getReceiveMoneyFromBankMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                        double payment = gameMessage.getMessageContent().getDouble("payment");

                        Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                        player.setSpielerKapital(roundDouble(player.getSpielerKapital() + payment));

                        spielStartActivity.databaseHandler.updateSpieler(player);

                        updateGegenSpielerCredit(player);

                        Toast.makeText(spielStartActivity.getApplicationContext(), player.getSpielerName() + " hat " + payment + " M$ von der Bank erhalten!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getReceiveMoneyFromBankMessage: " + e.toString());
                        Toast.makeText(spielStartActivity.getApplicationContext(), "Geld von der Bank nicht erhalten!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getReceiveMoneyFromBankMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld von der Bank nicht erhalten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getSendMoneyToBankMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                        double payment = gameMessage.getMessageContent().getDouble("payment");

                        Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                        player.setSpielerKapital(roundDouble(player.getSpielerKapital() - payment));

                        spielStartActivity.databaseHandler.updateSpieler(player);

                        updateGegenSpielerCredit(player);

                        Toast.makeText(spielStartActivity.getApplicationContext(), player.getSpielerName() + " hat " + payment + " M$ an die Bank gezahlt!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getSendMoneyToBankMessage: " + e.toString());
                        Toast.makeText(spielStartActivity.getApplicationContext(), "Geld an die Bank nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyToBankMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld an die Bank nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getSendMoneyToFreiParkenMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                        double payment = gameMessage.getMessageContent().getDouble("payment");

                        Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                        player.setSpielerKapital(roundDouble(player.getSpielerKapital() - payment));
                        aktuellesSpiel.setFreiParken(roundDouble(aktuellesSpiel.getFreiParken() + payment));

                        spielStartActivity.databaseHandler.updateSpieler(player);
                        spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                        updateGegenSpielerCredit(player);
                        updateAktuellesSpiel(aktuellesSpiel);

                        Toast.makeText(spielStartActivity.getApplicationContext(), player.getSpielerName() + " hat " + payment + " M$ in die Mitte gezahlt!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getSendMoneyToFreiParkenMessage: " + e.toString());
                        Toast.makeText(spielStartActivity.getApplicationContext(), "Geld in die Mitte nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyToFreiParkenMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld in die Mitte nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getReceiveFreiParkenMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                        Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                        player.setSpielerKapital(roundDouble(player.getSpielerKapital() + aktuellesSpiel.getFreiParken()));
                        Toast.makeText(spielStartActivity.getApplicationContext(), player.getSpielerName() + " hat " + aktuellesSpiel.getFreiParken() + " M$ aus der Mitte erhalten!", Toast.LENGTH_SHORT).show();
                        aktuellesSpiel.setFreiParken(0);

                        spielStartActivity.databaseHandler.updateSpieler(player);
                        spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                        updateGegenSpielerCredit(player);
                        updateAktuellesSpiel(aktuellesSpiel);

                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getReceiveFreiParkenMessage: " + e.toString());
                        Toast.makeText(spielStartActivity.getApplicationContext(), "Geld aus der Mitte nicht erhalten!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getReceiveFreiParkenMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld aus der Mitte nicht erhalten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getJoinGameMessage(GameMessage gameMessage){
        if(spielBeitretenActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielBeitretenActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;

                        Spieler neuerSpieler;

                        try{
                            neuerSpieler = spielBeitretenActivity.datasource.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                            neuerSpieler.setSpielerName(gameMessage.getMessageContent().getString("player_name"));
                            neuerSpieler.setSpielerFarbe(gameMessage.getMessageContent().getInt("player_color"));
                            neuerSpieler.setSpielerIpAdresse(gameMessage.getMessageContent().getString("player_ip_adress"));

                            spielBeitretenActivity.datasource.updateSpieler(neuerSpieler);
                        }catch(Exception e){
                            Log.e("MessageInterpreter", "Spieler noch nicht angelegt: " + e.toString());

                            neuerSpieler = new Spieler(gameMessage.getMessageContent().getString("player_imei"), aktuellesSpiel.getSpielID());
                            neuerSpieler.setSpielerName(gameMessage.getMessageContent().getString("player_name"));
                            neuerSpieler.setSpielerKapital(aktuellesSpiel.getSpielerStartkapital());
                            neuerSpieler.setSpielerFarbe(gameMessage.getMessageContent().getInt("player_color"));
                            neuerSpieler.setSpielerIpAdresse(gameMessage.getMessageContent().getString("player_ip_adress"));

                            spielBeitretenActivity.datasource.addSpieler(neuerSpieler);
                        }

                        spielBeitretenActivity.spieler_adapter.add(neuerSpieler);
                        spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();

                        Toast.makeText(spielBeitretenActivity.getApplicationContext(), neuerSpieler.getSpielerName() +  " ist der Spiellobby beigetreten!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Log.e("MessageInterpreter", "getJoinGameMessage: " + e.toString());
                        Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht beigetreten!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getJoinGameMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht beigetreten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getExitGameMessage(GameMessage gameMessage){
        if(spielBeitretenActivity != null){
            try {
                if (!gameMessage.getMessageContent().getString("player_imei").equals(spielBeitretenActivity.eigenerSpieler.getSpielerIMEI())) {
                    try {
                        Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;

                        Spieler neuerSpieler;

                        neuerSpieler = spielBeitretenActivity.datasource.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                        spielBeitretenActivity.datasource.deleteSpieler(neuerSpieler.getSpielerIMEI(), aktuellesSpiel.getSpielID());

                        for (int i = 0; i < spielBeitretenActivity.spieler_adapter.getCount(); i++) {

                            Spieler spielerToDelete = spielBeitretenActivity.spieler_adapter.getItem(i);

                            if (spielerToDelete.getSpielerIMEI().equals(neuerSpieler.getSpielerIMEI())) {
                                spielBeitretenActivity.spieler_adapter.objects.remove(i);
                                spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();
                            }
                        }

                        Toast.makeText(spielBeitretenActivity.getApplicationContext(), neuerSpieler.getSpielerName() + " hat die Spiellobby verlassen!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("MessageInterpreter", "getExitGameMessage: " + e.toString());
                        Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler hat die Gamelobby nicht verlassen!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch(Exception e){
                Log.e("MessageInterpreter", "getExitGameMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler hat die Gamelobby nicht verlassen!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public double roundDouble(Double betrag){
        return Math.round(betrag*1000)/1000.0;
    }

    public void updateEigenerSpielerCredit(Spieler eigenerSpieler){

        spielStartActivity.eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital());

        double eigenerSpielerKapital = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(spielStartActivity.aktuellesSpiel.getSpielID(), spielStartActivity.eigenerSpieler.getSpielerIMEI()).getSpielerKapital();
        spielStartActivity.aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpielerKapital));
    }

    public void updateAktuellesSpiel(Spiel aktuellesSpiel){

        spielStartActivity.aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken());
    }

    public void updatePlayerCredit(Spieler sender, Spieler receiver){

        ArrayList<Spieler> gegenSpielerListe = spielStartActivity.gegenspielerListe;

        if(receiver.getSpielerIMEI().equals(spielStartActivity.eigenerSpieler.getSpielerIMEI())) {

            updateEigenerSpielerCredit(receiver);

            for(int i = 0; i < gegenSpielerListe.size(); i++) {

                Spieler gegenSpieler = gegenSpielerListe.get(i);

                if(gegenSpielerListe.get(i).getSpielerIMEI().equals(sender.getSpielerIMEI())){
                    gegenSpieler.setSpielerKapital(sender.getSpielerKapital());

                    spielStartActivity.gegenspielerListe.set(i, gegenSpieler);
                    break;
                }
            }
        } else {

            for (int i = 0; i < gegenSpielerListe.size(); i++) {

                Spieler gegenSpieler = gegenSpielerListe.get(i);

                if (gegenSpielerListe.get(i).getSpielerIMEI().equals(sender.getSpielerIMEI())) {
                    gegenSpieler.setSpielerKapital(sender.getSpielerKapital());

                    spielStartActivity.gegenspielerListe.set(i, gegenSpieler);
                }

                if (gegenSpielerListe.get(i).getSpielerIMEI().equals(receiver.getSpielerIMEI())) {
                    gegenSpieler.setSpielerKapital(receiver.getSpielerKapital());

                    spielStartActivity.gegenspielerListe.set(i, gegenSpieler);
                }
            }
        }
    }

    public void updateGegenSpielerCredit(Spieler receiver) {

        ArrayList<Spieler> gegenSpielerListe = spielStartActivity.gegenspielerListe;

        for (int i = 0; i < gegenSpielerListe.size(); i++) {

            Spieler gegenSpieler = gegenSpielerListe.get(i);

            if (gegenSpielerListe.get(i).getSpielerIMEI().equals(receiver.getSpielerIMEI())) {
                gegenSpieler.setSpielerKapital(receiver.getSpielerKapital());

                spielStartActivity.gegenspielerListe.set(i, gegenSpieler);
                break;
            }
        }
    }
}
