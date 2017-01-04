package com.monopoly.domke.sebastian.monopoly.common;

/**
 * Created by Basti on 25.11.2016.
 */

public class Spieler {

    private int spielerID;
    private String spielerName;
    private int spielerFarbe;
    private double spielerKapital;
    private String spielerIpAdresse;
    private String spielerIMEI;
    private int idMonopolySpiel;

    public Spieler (String spielerIMEI, int idMonopolySpiel){
        this.spielerIMEI = spielerIMEI;
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

    public void setSpielerKapital(double spielerKapital){

        this.spielerKapital = spielerKapital;
    }

    public double getSpielerKapital(){

        return this.spielerKapital;
    }

    public void setSpielerIpAdresse(String spielerIpAdresse){

        this.spielerIpAdresse = spielerIpAdresse;
    }

    public String getSpielerIpAdresse(){

        return this.spielerIpAdresse;
    }

    public void setSpielerIMEI(String spielerIMEI){

        this.spielerIMEI = spielerIMEI;
    }

    public String getSpielerIMEI(){

        return this.spielerIMEI;
    }

    public void setIdMonopolySpiel(int idMonopolySpiel){

        this.idMonopolySpiel = idMonopolySpiel;
    }

    public int getIdMonopolySpiel(){

        return this.idMonopolySpiel;
    }
}
