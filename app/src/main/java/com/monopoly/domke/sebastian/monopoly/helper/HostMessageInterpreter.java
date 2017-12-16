package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage.MessageHeader;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity.TAG;


/**
 * Created by Basti on 22.12.2016.
 */

//Todo Sound und Vibrationsfeedback

public class HostMessageInterpreter {

    private SpielBeitretenActivity spielBeitretenActivity;
    private SpielStartActivity spielStartActivity;
    private Spieler playerToDelete;

    private SharedPreferences sharedPreferences = null;

    public static final String SHARED_PREF = "SHARED_PREF";
    public static final String SHARED_PREF_IP_ADRESS = "SHARED_PREF_IP_ADRESS";
    public static final String SHARED_PREF_PORT = "SHARED_PREF_PORT";

    public HostMessageInterpreter(SpielBeitretenActivity activity){

        sharedPreferences = activity.getApplicationContext().getSharedPreferences(SHARED_PREF, 0); // 0 - for private mode
        this.spielBeitretenActivity = activity;
    }

    public HostMessageInterpreter(SpielStartActivity activity){

        sharedPreferences = activity.getApplicationContext().getSharedPreferences(SHARED_PREF, 0); // 0 - for private mode
        this.spielStartActivity = activity;
    }

    public void decideWhatToDoWithTheMassage(GameMessage gameMessage){

        MessageHeader messageHeader = gameMessage.getMessageHeader();

        switch(messageHeader){
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

            case requestJoinGame:
                getRequestJoinGameMessage();
                break;
        }
    }

    public void getRequestJoinGameMessage(){
        if(spielBeitretenActivity != null){
            try {
                MessageParser messageParser = new MessageParser();

                Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;
                Spieler eigenerSpieler = spielBeitretenActivity.eigenerSpieler;

                JSONObject messageContent = messageParser.invitePlayerToJson(eigenerSpieler, aktuellesSpiel);

                GameMessage invitePlayerGameMessage = new GameMessage(MessageHeader.invitation, messageContent);

                String jsonString = messageParser.messageToJsonString(invitePlayerGameMessage);

                spielBeitretenActivity.mGameConnectionService.mGameConnection.sendMessage(jsonString);

                //spielBeitretenActivity.mGameConnection.sendMessage(jsonString);
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "RequestJoinGame erhalten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getRequestJoinGameMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "RequestJoinGame nicht erhalten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getSendMoneyMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
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
    }

    public void getReceiveMoneyFromBankMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
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
    }

    public void getSendMoneyToBankMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
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
    }

    public void getSendMoneyToFreiParkenMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
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
    }

    public void getReceiveFreiParkenMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
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
    }

    public void getJoinGameMessage(GameMessage gameMessage){
        if(spielBeitretenActivity != null){
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

                playerToDelete = neuerSpieler;

                spielBeitretenActivity.spieler_adapter.add(neuerSpieler);
                spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), neuerSpieler.getSpielerName() +  " ist der Gamelobby beigetreten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getJoinGameMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht beigetreten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getExitGameMessage(GameMessage gameMessage){
        if(spielBeitretenActivity != null){
            try {
                Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;

                Spieler neuerSpieler;

                neuerSpieler = spielBeitretenActivity.datasource.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                spielBeitretenActivity.datasource.deleteSpieler(neuerSpieler.getSpielerIMEI(), aktuellesSpiel.getSpielID());

                for (int i=0; i < spielBeitretenActivity.spieler_adapter.getCount(); i++) {

                    Spieler spielerToDelete = spielBeitretenActivity.spieler_adapter.getItem(i);

                    if(spielerToDelete.getSpielerIMEI().equals(neuerSpieler.getSpielerIMEI())){
                        spielBeitretenActivity.spieler_adapter.objects.remove(i);
                        spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();
                    }
                }

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), neuerSpieler.getSpielerName() + " hat die Gamelobby verlassen!", Toast.LENGTH_SHORT).show();
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
