package com.monopoly.domke.sebastian.monopoly.helper;

import android.content.Context;

import com.monopoly.domke.sebastian.monopoly.common.Message;
import com.monopoly.domke.sebastian.monopoly.common.Message.MessageHeader;


/**
 * Created by Basti on 22.12.2016.
 */

public class PlayerMessageInterpreter {

    private Context mContext;

    public PlayerMessageInterpreter(Context context){

        this.mContext = context;
    }

    public void decideWhatToDoWithTheMassage(Message message){

        MessageHeader messageHeader = message.getMessageHeader();

        switch(messageHeader){
            case invitation:

                break;

            case gameStart: case gameEnd:

                break;

            case sendMoney:

                break;

            case receiveMoneyFromBank: case receiveFreiParken: case sendMoneyToBank: case sendMoneyToFreiParken:

                break;

            case joinGame: case exitGame:

                break;
        }
    }

    public void getInvitation(Message message){

    }
}
