package com.monopoly.domke.sebastian.monopoly.helper;

import android.app.Activity;
import android.content.Context;

import com.monopoly.domke.sebastian.monopoly.common.Message;
import com.monopoly.domke.sebastian.monopoly.common.Message.MessageHeader;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.view.MainMenuActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielBeitretenActivity;
import com.monopoly.domke.sebastian.monopoly.view.SpielStartActivity;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Basti on 22.12.2016.
 */

public class HostMessageInterpreter {

    private SpielBeitretenActivity spielBeitretenActivity;
    private SpielStartActivity spielStartActivity;

    public HostMessageInterpreter(SpielBeitretenActivity activity){

        this.spielBeitretenActivity = activity;
    }

    public HostMessageInterpreter(SpielStartActivity activity){

        this.spielStartActivity = activity;
    }

    public void decideWhatToDoWithTheMassage(Message message){

        MessageHeader messageHeader = message.getMessageHeader();

        switch(messageHeader){
            case sendMoney:

                break;

            case receiveMoneyFromBank: case receiveFreiParken: case sendMoneyToBank: case sendMoneyToFreiParken:

                break;

            case joinGame: case exitGame:

                break;

            case requestJoinGame:
                getRequestJoinGameMessage(message);
                break;
        }
    }

    public void getRequestJoinGameMessage(Message message){
        if(spielBeitretenActivity != null){
            MessageParser messageParser = new MessageParser();

            Spiel aktuellesSpiel = spielBeitretenActivity.aktuellesSpiel;
            Spieler eigenerSpieler = spielBeitretenActivity.eigenerSpieler;

            JSONObject messageContent = messageParser.invitePlayerToJson(eigenerSpieler, aktuellesSpiel);

            Message invitePlayerMessage = new Message(MessageHeader.invitation, messageContent);

            String jsonString = messageParser.messageToJsonString(invitePlayerMessage);

            spielBeitretenActivity.mGameConnection.sendMessage(jsonString);
        }
    }
}
