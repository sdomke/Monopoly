package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spiel {

    private int spielID;
    private int spielerAnzahl;
    private String spielDatum;
    private int spielerStartkapital;
    private String waehrung;
    private int freiParkenWert;

    public void setSpielID(int spielID){

        this.spielID = spielID;
    }

    public int getSpielID(){

        return this.spielID;
    }

    public void setSpielerAnzahl(int spielerAnzahl){

        this.spielerAnzahl = spielerAnzahl;
    }

    public int getSpielerAnzahl(){

        return this.spielerAnzahl;
    }

    public void setSpielDatum(String spielDatum){

        this.spielDatum = spielDatum;
    }

    public String getSpielDatum(){

        return this.spielDatum;
    }

    public void setWaehrung(String waehrung){

        this.waehrung = waehrung;
    }

    public String getWaehrung(){

        return this.waehrung;
    }

    public void setSpielerStartkapital(int spielerStartkapital){

        this.spielerStartkapital = spielerStartkapital;
    }

    public int getSpielerStartkapital(){

        return this.spielerStartkapital;
    }

    public void setFreiParken(int freiParkenWert){

        this.freiParkenWert = freiParkenWert;
    }

    public int getFreiParken(){

        return this.freiParkenWert;
    }
}
