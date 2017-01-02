package com.monopoly.domke.sebastian.monopoly.helper;

import android.util.Log;

import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Basti-Laptop on 19.12.2016.
 */

public class MessageParser {

    public JSONObject invitePlayerToJson(Spieler eigenerSpieler, Spiel aktuellesSpiel){

        JSONObject json = new JSONObject();

        try {
            json.put("host_ip_adress", eigenerSpieler.getSpielerIpAdresse());
            json.put("host_mac_adress", eigenerSpieler.getSpielerMacAdresse());
            json.put("game_date", aktuellesSpiel.getSpielDatum());
            json.put("game_player_count", aktuellesSpiel.getSpielerAnzahl());
            json.put("game_seed_capital", aktuellesSpiel.getSpielerStartkapital());
            json.put("game_currency", aktuellesSpiel.getWaehrung());
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }

    public JSONObject gameStatusToJson(Spieler eigenerSpieler, Spiel aktuellesSpiel){

        JSONObject json = new JSONObject();

        try {
            json.put("host_ip_adress", eigenerSpieler.getSpielerIpAdresse());
            json.put("host_mac_adress", eigenerSpieler.getSpielerMacAdresse());
            json.put("game_date", aktuellesSpiel.getSpielDatum());
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }

    public JSONObject moneyTransactionToPlayerToJson(Spieler eigenerSpieler, Spieler gegenSpieler, int payment){

        JSONObject json = new JSONObject();

        try {
            json.put("sender_name", eigenerSpieler.getSpielerName());
            json.put("sender_mac_adress", eigenerSpieler.getSpielerMacAdresse());
            json.put("receiver_name", gegenSpieler.getSpielerName());
            json.put("receiver_mac_adress", gegenSpieler.getSpielerMacAdresse());
            json.put("payment", payment);
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }

    public JSONObject moneyTransactionToJson(Spieler eigenerSpieler, int value){

        JSONObject json = new JSONObject();

        try {
            json.put("player_name", eigenerSpieler.getSpielerName());
            json.put("player_mac_adress", eigenerSpieler.getSpielerMacAdresse());
            json.put("payment", value);
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }

    public JSONObject playerStatusToJson(Spieler eigenerSpieler, Spiel aktuellesSpiel){

        JSONObject json = new JSONObject();

        try {
            json.put("player_name", eigenerSpieler.getSpielerName());
            json.put("player_ip_adress", eigenerSpieler.getSpielerIpAdresse());
            json.put("player_mac_adress", eigenerSpieler.getSpielerMacAdresse());
            json.put("player_color", eigenerSpieler.getSpielerFarbe());
            json.put("player_capital", eigenerSpieler.getSpielerKapital());
            json.put("game_date", aktuellesSpiel.getSpielDatum());
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }

    public JSONObject requestToJoinGameToJson(Spieler eigenerSpieler){

        JSONObject json = new JSONObject();

        try {
            json.put("player_ip", eigenerSpieler.getSpielerIpAdresse());
            json.put("player_mac_adress", eigenerSpieler.getSpielerMacAdresse());
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return json;
    }


    public String messageToJsonString(GameMessage gameMessage){

        JSONObject json = new JSONObject();
        String jsonString="";

        try {
            json.put("message_header", gameMessage.getMessageHeader());
            json.put("message_content", gameMessage.getMessageContent());

            jsonString = json.toString();
        }
        catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return jsonString;

    }

    public GameMessage jsonStringToMessage(String jsonString){

        GameMessage newGameMessage = new GameMessage();

        try {
            JSONObject jObject = new JSONObject(jsonString);

            newGameMessage.setMessageHeader(getMessageHeader(jObject.get("message_header").toString()));
            newGameMessage.setMessageContent((JSONObject) jObject.get("message_content"));

        }catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return newGameMessage;
    }

    public GameMessage.MessageHeader getMessageHeader(String messageHeader){
        switch (messageHeader){
            case "invitation":
                return GameMessage.MessageHeader.invitation;

            case "gameStart":
                return GameMessage.MessageHeader.gameStart;

            case "gameEnd":
                return GameMessage.MessageHeader.gameEnd;

            case "sendMoney":
                return GameMessage.MessageHeader.sendMoney;

            case "receiveMoneyFromBank":
                return GameMessage.MessageHeader.receiveMoneyFromBank;

            case "receiveFreiParken":
                return GameMessage.MessageHeader.receiveFreiParken;

            case "sendMoneyToBank":
                return GameMessage.MessageHeader.sendMoneyToBank;

            case "sendMoneyToFreiParken":
                return GameMessage.MessageHeader.sendMoneyToFreiParken;

            case "joinGame":
                return GameMessage.MessageHeader.joinGame;

            case "exitGame":
                return GameMessage.MessageHeader.exitGame;

            case "requestJoinGame":
                return GameMessage.MessageHeader.requestJoinGame;

            default:
                return null;

        }
    }
}
