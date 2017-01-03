package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Intent;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage.MessageHeader;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.MainMenuActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;

import java.util.ArrayList;


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
                    Log.e("MessageInterpreter", "Spieler noch nicht angelegt: " + e.toString());

                    neuesSpiel = new Spiel(gameMessage.getMessageContent().getString("game_date"));
                    neuesSpiel.setSpielerStartkapital(gameMessage.getMessageContent().getInt("game_seed_capital"));
                    neuesSpiel.setSpielerAnzahl(gameMessage.getMessageContent().getInt("game_player_count"));
                    neuesSpiel.setFreiParken(0);
                    neuesSpiel.setWaehrung(gameMessage.getMessageContent().getString("game_currency"));
                }

                mainMenuActivity.datasource.addNewMonopolyGame(neuesSpiel);
                mainMenuActivity.neuesSpiel = neuesSpiel;
                RelativeLayout spielBeitretenRelativeLayout = (RelativeLayout) mainMenuActivity.findViewById(R.id.spielBeitretenButtonLayout);

                spielBeitretenRelativeLayout.setEnabled(true);

                Toast.makeText(mainMenuActivity.getApplicationContext(), "Spieler eingeladen!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getInvitationMessage: " + e.toString());
                Toast.makeText(mainMenuActivity.getApplicationContext(), "Spieler nicht eingeladen!!!", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spiel gestartet!", Toast.LENGTH_SHORT).show();

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

                Toast.makeText(spielStartActivity.getApplicationContext(), "Spiel beendet!", Toast.LENGTH_SHORT).show();

                spielStartActivity.startActivity(intent);

            }catch(Exception e){
                Log.e("MessageInterpreter", "getGameEndMessage: " + e.toString());
                Toast.makeText(spielStartActivity.getApplicationContext(), "Spiel nicht beendet!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void getSendMoneyMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = gameMessage.getMessageContent().getInt("payment");

                Spieler senderPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("sender_imei"));

                Spieler receiverPlayer = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("receiver_imei"));

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

    public void getReceiveMoneyFromBankMessage(GameMessage gameMessage){
        if(spielStartActivity != null){
            try {
                Spiel aktuellesSpiel = spielStartActivity.aktuellesSpiel;

                int payment = gameMessage.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                player.setSpielerKapital(player.getSpielerKapital() + payment);

                spielStartActivity.databaseHandler.updateSpieler(player);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld von der Bank erhalten!", Toast.LENGTH_SHORT).show();
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

                int payment = gameMessage.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                player.setSpielerKapital(player.getSpielerKapital() - payment);

                spielStartActivity.databaseHandler.updateSpieler(player);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld an die Bank gezahlt!", Toast.LENGTH_SHORT).show();
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

                int payment = gameMessage.getMessageContent().getInt("payment");

                Spieler player = spielStartActivity.databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), gameMessage.getMessageContent().getString("player_imei"));

                player.setSpielerKapital(player.getSpielerKapital() - payment);
                aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + payment);

                spielStartActivity.databaseHandler.updateSpieler(player);
                spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld in die Mitte gezahlt!", Toast.LENGTH_SHORT).show();
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

                player.setSpielerKapital(player.getSpielerKapital() + aktuellesSpiel.getFreiParken());
                aktuellesSpiel.setFreiParken(0);

                spielStartActivity.databaseHandler.updateSpieler(player);
                spielStartActivity.databaseHandler.updateSpiel(aktuellesSpiel);

                Toast.makeText(spielStartActivity.getApplicationContext(), "Geld aus der Mitte erhalten!", Toast.LENGTH_SHORT).show();
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

                spielBeitretenActivity.spieler_adapter.add(neuerSpieler);
                spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler beigetreten!", Toast.LENGTH_SHORT).show();
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

                spielBeitretenActivity.datasource.deleteSpieler(neuerSpieler.getSpielerIMEI(), neuerSpieler.getIdMonopolySpiel());

                for (int i=0; i < spielBeitretenActivity.spieler_adapter.getCount(); i++) {

                    Spieler spielerToDelete = spielBeitretenActivity.spieler_adapter.getItem(i);

                    if(spielerToDelete.getSpielerIMEI().equals(neuerSpieler.getSpielerIMEI())){
                        spielBeitretenActivity.spieler_adapter.objects.remove(i);
                        spielBeitretenActivity.spieler_adapter.notifyDataSetChanged();
                    }
                }

                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler hat die Gamelobby verlassen!", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Log.e("MessageInterpreter", "getExitGameMessage: " + e.toString());
                Toast.makeText(spielBeitretenActivity.getApplicationContext(), "Spieler hat die Gamelobby nicht verlassen!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}