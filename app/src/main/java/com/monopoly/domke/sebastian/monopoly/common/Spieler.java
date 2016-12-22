package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spieler {

    private int spielerID;
    private String spielerName;
    private int spielerFarbe;
    private int spielerKapital;
    private String spielerIpAdresse;
    private String spielerMacAdresse;
    private int idMonopolySpiel;

    public Spieler (String spielerMacAdresse, int idMonopolySpiel){
        this.spielerMacAdresse = spielerMacAdresse;
        this.idMonopolySpiel = idMonopolySpiel;
    }

    public void setSpielerID(int spielerID){

        this.spielerID = spielerID;
    }

    public int getSpielerID(){

        return this.spielerID;
    }

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

    public void setSpielerIpAdresse(String spielerIpAdresse){

        this.spielerIpAdresse = spielerIpAdresse;
    }

    public String getSpielerIpAdresse(){

        return this.spielerIpAdresse;
    }

    public void setSpielerMacAdresse(String spielerMacAdresse){

        this.spielerMacAdresse = spielerMacAdresse;
    }

    public String getSpielerMacAdresse(){

        return this.spielerMacAdresse;
    }

    public void setIdMonopolySpiel(int idMonopolySpiel){

        this.idMonopolySpiel = idMonopolySpiel;
    }

    public int getIdMonopolySpiel(){

        return this.idMonopolySpiel;
    }
}
