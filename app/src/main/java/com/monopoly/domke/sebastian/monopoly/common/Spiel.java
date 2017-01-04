package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spiel {

    private int spielID;
    private int spielerAnzahl;
    private String spielDatum;
    private double spielerStartkapital;
    private String waehrung;
    private double freiParkenWert;

    public Spiel(){
    }

    public Spiel(String spielDatum){
        this.spielDatum = spielDatum;
    }

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

    public void setSpielerStartkapital(double spielerStartkapital){

        this.spielerStartkapital = spielerStartkapital;
    }

    public double getSpielerStartkapital(){

        return this.spielerStartkapital;
    }

    public void setFreiParken(double freiParkenWert){

        this.freiParkenWert = freiParkenWert;
    }

    public double getFreiParken(){

        return this.freiParkenWert;
    }
}
