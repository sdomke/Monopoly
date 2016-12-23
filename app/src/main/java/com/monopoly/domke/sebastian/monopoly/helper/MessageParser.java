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
            JSONArray jArray = new JSONArray(jsonString);

            newGameMessage.setMessageHeader((GameMessage.MessageHeader) jArray.getJSONObject(0).get("message_header"));
            newGameMessage.setMessageContent((JSONObject) jArray.getJSONObject(1).get("message_content"));

        }catch(JSONException e){
            Log.e("JsonException", e.toString());
        }

        return newGameMessage;
    }
}
