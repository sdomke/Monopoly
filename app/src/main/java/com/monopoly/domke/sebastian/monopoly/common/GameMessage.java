package com.monopoly.domke.sebastian.monopoly.common;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Basti-Laptop on 19.12.2016.
 */

public class GameMessage {

    MessageHeader messageHeader;
    JSONObject messageContent;

    public enum MessageHeader{
        invitation,
        gameStart,
        gameEnd,
        gameAbort,
        sendMoney,
        receiveMoneyFromBank,
        receiveFreiParken,
        sendMoneyToBank,
        sendMoneyToFreiParken,
        joinGame,
        exitGame,
        leaveGame,
        requestJoinGame,
        requestCurrentGameLobby,
        updateGameLobby
    }

    public GameMessage(){
    }

    public GameMessage(MessageHeader messageHeader, JSONObject messageContent){
        this.messageHeader = messageHeader;
        this.messageContent = messageContent;
    }

    public void setMessageHeader(MessageHeader messageHeader){
        this.messageHeader = messageHeader;
    }

    public MessageHeader getMessageHeader(){
        return messageHeader;
    }

    public void setMessageContent(JSONObject messageContent){
        this.messageContent = messageContent;
    }

    public JSONObject getMessageContent(){
        return messageContent;
    }

}
