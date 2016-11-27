package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spieler {

    private String spielerName;
    private int spielerFarbe;
    private int spielerKapital;

    public void setSpielerName(String spielerName){

        this.spielerName = spielerName;
    }

    public String getSpielerName(){
        return this.spielerName;
    }

    public void setSpielerFarbe(int spielerFarbe){

        this.spielerFarbe = spielerFarbe;
    }

    public int getSpielerFarbe(){

        return this.spielerFarbe;
    }

    public void setSpielerKapital(int spielerKapital){

        this.spielerKapital = spielerKapital;
    }

    public int getSpielerKapital(){

        return this.spielerKapital;
    }

}
