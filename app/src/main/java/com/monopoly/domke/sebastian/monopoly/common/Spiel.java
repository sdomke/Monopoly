package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spiel {

    private int spielerAnzahl;
    private String spielDatum;
    private int spielerStartkapital;

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

    public void setSpielerStartkapital(int spielerStartkapital){
        this.spielerStartkapital = spielerStartkapital;
    }

    public int getSpielerStartkapital(){
        return this.spielerStartkapital;
    }
}
