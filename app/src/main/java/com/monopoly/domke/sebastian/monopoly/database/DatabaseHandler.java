package com.monopoly.domke.sebastian.monopoly.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int dbVersion = 6;

	// Database Name
	private static final String dbName = "MonopolyAppDB";

	// site induction table name
	private static final String monopolySpielerTable = "TableMonopolySpieler";

	// monopoly spiel table column name
	private static final String colSpielerId = "SpielerId";
	private static final String colSpielerName = "SpielerName";
	private static final String colSpielerFarbe = "SpielerFarbe";
	private static final String colSpielerKapital = "SpielerKapital";
	private static final String colSpielerIpAdresse = "SpielerIpAdresse";
	private static final String colSpielerIMEI = "SpielerIMEI";
	private static final String colIdMonopolySpielListe = "IdMonopolySpielListe";

	// training courses Table name
	private static final String spielListeTable = "TableSpielListe";

	// training courses Table column name
	private static final String colMonopolySpielListeId = "MonopolySpielListeId";
	private static final String colMonopolySpielListeDatum = "MonopolySpielListeDatum";
	private static final String colMonopolySpielListeSpielerAnzahl = "MonopolySpielListeSpielerAnzahl";
	private static final String colMonopolySpielListeStartkapital = "MonopolySpielListeStartkapital";
	private static final String colMonopolySpielListeWaehrung = "MonopolySpielListeWaehrung";
	private static final String colMonopolySpielFreiParken = "FreiParken";

	public DatabaseHandler(Context context) {

		super(context, dbName, null, dbVersion);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {

		String createMonoploySpielerTABLE = "CREATE TABLE " + monopolySpielerTable
				+ "(" + colSpielerId + " INTEGER PRIMARY KEY,"
				+ colSpielerName + " TEXT,"
                + colSpielerFarbe + " INTEGER,"
                + colSpielerKapital + " DOUBLE,"
				+ colSpielerIpAdresse + " TEXT,"
				+ colSpielerIMEI + " TEXT,"
                + colIdMonopolySpielListe + " INTEGER NOT NULL,"
				+ " FOREIGN KEY (" + colIdMonopolySpielListe + ") REFERENCES " + spielListeTable + " (" + colMonopolySpielListeId + ")" + " ON DELETE CASCADE" + ")";

		String createSpielListeTABLE = "CREATE TABLE "
				+ spielListeTable + "(" + colMonopolySpielListeId + " INTEGER PRIMARY KEY,"
                + colMonopolySpielListeDatum + " TEXT,"
				+ colMonopolySpielListeSpielerAnzahl + " INTEGER,"
                + colMonopolySpielListeStartkapital + " DOUBLE,"
				+ colMonopolySpielListeWaehrung + " TEXT,"
				+ colMonopolySpielFreiParken + " DOUBLE"+ ")";

		db.execSQL(createMonoploySpielerTABLE);
		db.execSQL(createSpielListeTABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + monopolySpielerTable);
		db.execSQL("DROP TABLE IF EXISTS " + spielListeTable);

		// Create tables again
		onCreate(db);
	}
	
	// Delete database
	public void deleteTables() {
		// Drop table if existed
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + monopolySpielerTable);
		db.execSQL("DROP TABLE IF EXISTS " + spielListeTable);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onConfigure(SQLiteDatabase db){

		db.setForeignKeyConstraintsEnabled(true);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */
	// Adding new game
    public void addNewMonopolyGame(Spiel spielEintellungen) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(colMonopolySpielListeSpielerAnzahl, spielEintellungen.getSpielerAnzahl());
        values.put(colMonopolySpielListeStartkapital,
                spielEintellungen.getSpielerStartkapital());
        values.put(colMonopolySpielListeDatum,
                spielEintellungen.getSpielDatum());
		values.put(colMonopolySpielListeWaehrung,
				spielEintellungen.getWaehrung());
		values.put(colMonopolySpielFreiParken, 0);

        // Inserting Row
        db.insert(spielListeTable, null, values);
        db.close(); // Closing database connection
    }

	// Delete game
	public void deleteSpiel(String datum) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(spielListeTable, colMonopolySpielListeDatum + "=?",
				new String[] { String.valueOf(datum) });
		db.close();
	}

	// get game by date
	public Spiel getSpielByDatum(String datum) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(spielListeTable, new String[] { colMonopolySpielListeId,
						colMonopolySpielListeDatum, colMonopolySpielListeSpielerAnzahl, colMonopolySpielListeStartkapital, colMonopolySpielListeWaehrung, colMonopolySpielFreiParken}, colMonopolySpielListeDatum + "=?",
				new String[] { String.valueOf(datum) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Spiel spiel = new Spiel();
		spiel.setSpielID(Integer.parseInt(cursor.getString(0)));
		spiel.setSpielDatum(cursor.getString(1));
		spiel.setSpielerAnzahl(cursor.getInt(2));
		spiel.setSpielerStartkapital(cursor.getDouble(3));
		spiel.setWaehrung(cursor.getString(4));
		spiel.setFreiParken(cursor.getDouble(5));

		// return spiel
		cursor.close();
		db.close();
		return spiel;
	}

	// get game by id
	public Spiel getSpielByID(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(spielListeTable, new String[] { colMonopolySpielListeId,
						colMonopolySpielListeDatum, colMonopolySpielListeSpielerAnzahl, colMonopolySpielListeStartkapital, colMonopolySpielListeWaehrung, colMonopolySpielFreiParken}, colMonopolySpielListeId + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Spiel spiel = new Spiel();
		spiel.setSpielID(Integer.parseInt(cursor.getString(0)));
		spiel.setSpielDatum(cursor.getString(1));
		spiel.setSpielerAnzahl(cursor.getInt(2));
		spiel.setSpielerStartkapital(cursor.getDouble(3));
		spiel.setWaehrung(cursor.getString(4));
		spiel.setFreiParken(cursor.getDouble(5));

		// return spiel
		cursor.close();
		db.close();
		return spiel;
	}

	//update game
	public void updateSpiel(Spiel spiel) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colMonopolySpielListeDatum, spiel.getSpielDatum());
		values.put(colMonopolySpielListeSpielerAnzahl, spiel.getSpielerAnzahl());
		values.put(colMonopolySpielListeStartkapital, spiel.getSpielerStartkapital());
		values.put(colMonopolySpielListeWaehrung, spiel.getWaehrung());
		values.put(colMonopolySpielFreiParken, spiel.getFreiParken());

		// updating row
		int row = db.update(spielListeTable, values, colMonopolySpielListeDatum + " = ?",
				new String[] { String.valueOf(spiel.getSpielDatum()) });
		db.close(); // Closing database connection
	}

	public void addSpieler(Spieler spieler) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colSpielerName, spieler.getSpielerName());
		values.put(colSpielerFarbe, spieler.getSpielerFarbe());
		values.put(colSpielerKapital, spieler.getSpielerKapital());
		values.put(colSpielerIpAdresse, spieler.getSpielerIpAdresse());
		values.put(colSpielerIMEI, spieler.getSpielerIMEI());
		values.put(colIdMonopolySpielListe, spieler.getIdMonopolySpiel());

		// Inserting Row
		db.insert(monopolySpielerTable, null, values);
		db.close(); // Closing database connection
	}

	// Delete game
	public void deleteSpieler(String spielerIMEI, int spielID) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(monopolySpielerTable, colSpielerIMEI + "=? and " + colIdMonopolySpielListe + "=?",
				new String[] { String.valueOf(spielerIMEI), String.valueOf(spielID) });
		db.close();
	}

	// get spieler by spielID and ipAdress
	public Spieler getSpielerBySpielIdAndSpielerIMEI(int spielID, String spielerIMEI) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(monopolySpielerTable, new String[] { colSpielerId,
						colSpielerName, colSpielerFarbe, colSpielerKapital, colSpielerIpAdresse, colSpielerIMEI, colIdMonopolySpielListe}, colSpielerIMEI + "=? and " + colIdMonopolySpielListe + "=?",
				new String[] { String.valueOf(spielerIMEI), String.valueOf(spielID) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Spieler spieler = new Spieler(cursor.getString(5), cursor.getInt(6));
		spieler.setSpielerID(Integer.parseInt(cursor.getString(0)));
		spieler.setSpielerName(cursor.getString(1));
		spieler.setSpielerFarbe(cursor.getInt(2));
		spieler.setSpielerKapital(cursor.getDouble(3));
		spieler.setSpielerIpAdresse(cursor.getString(4));

		// return spieler
		cursor.close();
		db.close();
		return spieler;
	}

	public void updateSpieler(Spieler spieler) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colSpielerName, spieler.getSpielerName());
		values.put(colSpielerFarbe, spieler.getSpielerFarbe());
		values.put(colSpielerKapital, spieler.getSpielerKapital());
		values.put(colSpielerIpAdresse, spieler.getSpielerIpAdresse());
		values.put(colSpielerIMEI, spieler.getSpielerIMEI());
		values.put(colIdMonopolySpielListe, spieler.getIdMonopolySpiel());

		// updating row
		int row = db.update(monopolySpielerTable, values, colSpielerIMEI + "=? and " + colIdMonopolySpielListe + "=?",
				new String[] { String.valueOf(spieler.getSpielerIMEI()), String.valueOf(spieler.getIdMonopolySpiel()) } );
		db.close(); // Closing database connection
	}

	// Getting All spieler by spiel ID
	public ArrayList<Spieler> getAllSpielerBySpielID(int spielListeID) {
		ArrayList<Spieler> spielerListe = new ArrayList<Spieler>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + monopolySpielerTable + " WHERE " + colIdMonopolySpielListe + " LIKE " + spielListeID;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Spieler spieler = new Spieler(cursor.getString(4), cursor.getInt(6));
				spieler.setSpielerID(Integer.parseInt(cursor.getString(0)));
				spieler.setSpielerName(cursor.getString(1));
				spieler.setSpielerFarbe(cursor.getInt(2));
				spieler.setSpielerKapital(cursor.getDouble(3));
				spieler.setSpielerIMEI(cursor.getString(5));

				// Adding contact to list
				spielerListe.add(spieler);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return spieler list
		return spielerListe;
	}

	// Getting All spieler
	public ArrayList<Spieler> getAllSpieler() {
		ArrayList<Spieler> spielerListe = new ArrayList<Spieler>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + monopolySpielerTable;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Spieler spieler = new Spieler(cursor.getString(4), cursor.getInt(6));
				spieler.setSpielerID(Integer.parseInt(cursor.getString(0)));
				spieler.setSpielerName(cursor.getString(1));
				spieler.setSpielerFarbe(cursor.getInt(2));
				spieler.setSpielerKapital(cursor.getDouble(3));
				spieler.setSpielerIMEI(cursor.getString(5));

				// Adding contact to list
				spielerListe.add(spieler);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return spieler list
		return spielerListe;
	}

	// Getting All spiele
	public ArrayList<Spiel> getAllSpiele() {
		ArrayList<Spiel> spieleListe = new ArrayList<Spiel>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + spielListeTable;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Spiel spiel = new Spiel();
				spiel.setSpielID(Integer.parseInt(cursor.getString(0)));
				spiel.setSpielDatum(cursor.getString(1));
				spiel.setSpielerAnzahl(cursor.getInt(2));
				spiel.setSpielerStartkapital(cursor.getDouble(3));
				spiel.setWaehrung(cursor.getString(4));
				spiel.setFreiParken(cursor.getDouble(5));

				// Adding contact to list
				spieleListe.add(spiel);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return spieler list
		return spieleListe;
	}
}
