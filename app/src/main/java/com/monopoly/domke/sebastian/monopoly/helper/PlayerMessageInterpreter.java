package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Message;
import com.monopoly.domke.sebastian.monopoly.common.Message.MessageHeader;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.MainMenuActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;


/**
 * Created by Basti on 22.12.2016.
 */

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

    public void decideWhatToDoWithTheMassage(Message message){

        MessageHeader messageHeader = message.getMessageHeader();

        switch(messageHeader){
            case invitation:

                break;

            case gameStart:

                break;

            case gameEnd:

                break;

            case sendMoney:
                getSendMoneyMessage(message);
                break;

            case receiveMoneyFromBank:
                getReceiveMoneyFromBankMessage(message);
                break;

            case receiveFreiParken:
                getReceiveFreiParkenMessage(message);
                break;

            case sendMoneyToBank:
                getSendMoneyToBankMessage(message);
                break;

            case sendMoneyToFreiParken:
                getSendMoneyToFreiParkenMessage(message);
                break;

            case joinGame:
                getJoinGameMessage(message);
                break;

            case exitGame:
                getExitGameMessage(message);
                break;
        }
    }

    public void getInvitationMessage(Message message){
        if(mainMenuActivity != null){
            try {

                try{
                    mainMenuActivity.neuesSpiel = mainMenuActivity.datasource.getSpielByDatum(message.getMessageContent().getString("game_date"));
                }catch(Exception e){
                    Log.e("MessageInterpreter", "Spieler noch nicht angelegt: " + e.toString());

                    mainMenuActivity.neuesSpiel = new Spiel(message.getMessageContent().getString("game_date"));
                    mainMenuActivity.neuesSpiel.setSpielerStartkapital(message.getMessageContent().getInt("game_seed_capital"));
                    mainMenuActivity.neuesSpiel.setSpielerAnzahl(message.getMessageContent().getInt("game_player_count"));
                    mainMenuActivity.neuesSpiel.setFreiParken(0);
                    mainMenuActivity.neuesSpiel.setWaehrung(message.getMessageContent().getString("game_currency"));
                }
                RelativeLayout spielBeitretenRelativeLayout = (RelativeLayout) mainMenuActivity.findViewById(R.id.spielBeitretenButtonLayout);

                spielBeitretenRelativeLayout.setEnabled(true);

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler eingeladen!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht eingeladen!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void getSendMoneyMessage(Message message){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = message.getMessageContent().getInt("payment");

                Spieler senderPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("sender_mac_adress"));

                Spieler receiverPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("receiver_mac_adress"));

                senderPlayer.setSpielerKapital(senderPlayer.getSpielerKapital() - payment);
                receiverPlayer.setSpielerKapital(senderPlayer.getSpielerKapital() + payment);

                spielStartActivity.databaseHandler.updateSpieler(senderPlayer);
                spielStartActivity.databaseHandler.updateSpieler(receiverPlayer);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Überweisung erfolgreich!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Überweisung nicht erfolgreich!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getReceiveMoneyFromBankMessage(Message message){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = message.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));

                player.setSpielerKapital(player.getSpielerKapital() + payment);

                spielStartActivity.databaseHandler.updateSpieler(player);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld von der Bank erhalten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld von der Bank nicht erhalten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getSendMoneyToBankMessage(Message message){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = message.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));

                player.setSpielerKapital(player.getSpielerKapital() - payment);

                spielStartActivity.databaseHandler.updateSpieler(player);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld an die Bank gezahlt!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld an die Bank nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getSendMoneyToFreiParkenMessage(Message message){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = message.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));

                player.setSpielerKapital(player.getSpielerKapital() - payment);
                aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + payment);

                spielStartActivity.databaseHandler.updateSpieler(player);
                spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld in die Mitte gezahlt!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld in die Mitte nicht gezahlt!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getReceiveFreiParkenMessage(Message message){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));

                player.setSpielerKapital(player.getSpielerKapital() + aktuellesSpiel.getFreiParken());
                aktuellesSpiel.setFreiParken(0);

                spielStartActivity.databaseHandler.updateSpieler(player);
                spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld aus der Mitte erhalten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld aus der Mitte nicht erhalten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getJoinGameMessage(Message message){
        if(spielBeitretenActivity != null){
            try {
                Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;

                Spieler neuerSpieler;

                try{
                    neuerSpieler = spielBeitretenActivity.datasource.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));
                }catch(Exception e){
                    Log.e("MessageInterpreter", "Spieler noch nicht angelegt: " + e.toString());

                    neuerSpieler = new Spieler(message.getMessageContent().getString("player_mac_adress"), aktuellesSpiel.getSpielID());
                    neuerSpieler.setSpielerName(message.getMessageContent().getString("player_name"));
                    neuerSpieler.setSpielerKapital(aktuellesSpiel.getSpielerStartkapital());
                    neuerSpieler.setSpielerFarbe(message.getMessageContent().getInt("player_color"));
                    neuerSpieler.setSpielerIpAdresse(message.getMessageContent().getString("player_ip_adress"));

                    spielBeitretenActivity.datasource.addSpieler(neuerSpieler);
                }

                spielBeitretenActivity.spieler_adapter.add(neuerSpieler);
                spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler beigetreten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht beigetreten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getExitGameMessage(Message message){
        if(spielBeitretenActivity != null){
            try {
                Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;

                Spieler neuerSpieler;

                neuerSpieler = spielBeitretenActivity.datasource.getSpielerBySpielIdAndSpielerMac(aktuellesSpiel.getSpielID(), message.getMessageContent().getString("player_mac_adress"));

                spielBeitretenActivity.datasource.deleteSpieler(neuerSpieler.getSpielerMacAdresse(), neuerSpieler.getIdMonopolySpiel());

                spielBeitretenActivity.spieler_adapter.remove(neuerSpieler);
                spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler beigetreten!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getSendMoneyMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler nicht beigetreten!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
