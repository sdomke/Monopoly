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
	private static final int dbVersion = 2;

	// Database Name
	private static final String dbName = "MonopolyAppDB";

	// site induction table name
	private static final String monopolySpielTable = "TableMonopolySpiel";

	// monopoly spiel table column name
	private static final String colSpielerId = "SpielerId";
	private static final String colSpielerName = "SpielerName";
	private static final String colSpielerFarbe = "SpielerFarbe";
	private static final String colSpielerKapital = "SpielerKapital";
	private static final String colSpielerIpAdresse = "SpielerIpAdresse";
	private static final String colFreiParken = "FreiParken";
	private static final String colIdMonopolySpielListe = "IdMonopolySpielListe";

	// training courses Table name
	private static final String spielListeTable = "TableSpielListe";

	// training courses Table column name
	private static final String colMonopolySpielListeId = "MonopolySpielListeId";
	private static final String colMonopolySpielListeDatum = "MonopolySpielListeDatum";
	private static final String colMonopolySpielListeSpielerAnzahl = "MonopolySpielListeSpielerAnzahl";
	private static final String colMonopolySpielListeStartkapital = "MonopolySpielListeStartkapital";
	private static final String colMonopolySpielListeWaehrung = "MonopolySpielListeWaehrung";

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

		String createMonoploySpielTABLE = "CREATE TABLE " + monopolySpielTable
				+ "(" + colSpielerId + " INTEGER PRIMARY KEY,"
				+ colSpielerName + " TEXT,"
                + colSpielerFarbe + " INTEGER,"
                + colSpielerKapital + " INTEGER,"
				+ colFreiParken + " INTEGER, "
				+ colSpielerIpAdresse + " TEXT, "
                + colIdMonopolySpielListe + " INTEGER NOT NULL,"
				+ " FOREIGN KEY (" + colIdMonopolySpielListe + ") REFERENCES " + spielListeTable + " (" + colMonopolySpielListeId + ")" + " ON DELETE CASCADE" + ")";

		String createSpielListeTABLE = "CREATE TABLE "
				+ spielListeTable + "(" + colMonopolySpielListeId + " INTEGER PRIMARY KEY,"
                + colMonopolySpielListeDatum + " TEXT,"
				+ colMonopolySpielListeSpielerAnzahl + " INTEGER,"
                + colMonopolySpielListeStartkapital + " INTEGER,"
				+ colMonopolySpielListeWaehrung + " TEXT" + ")";

		db.execSQL(createMonoploySpielTABLE);
		db.execSQL(createSpielListeTABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + monopolySpielTable);
		db.execSQL("DROP TABLE IF EXISTS " + spielListeTable);

		// Create tables again
		onCreate(db);
	}
	
	// Delete database
	public void deleteTables() {
		// Drop table if existed
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + monopolySpielTable);
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

        // Inserting Row
        db.insert(spielListeTable, null, values);
        db.close(); // Closing database connection
    }

	// get game by date
	public Spiel getSpielByDatum(String datum) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(spielListeTable, new String[] { colMonopolySpielListeId,
						colMonopolySpielListeDatum, colMonopolySpielListeSpielerAnzahl, colMonopolySpielListeStartkapital, colMonopolySpielListeWaehrung}, colMonopolySpielListeDatum + "=?",
				new String[] { String.valueOf(datum) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Spiel spiel = new Spiel();
		spiel.setSpielID(Integer.parseInt(cursor.getString(0)));
		spiel.setSpielDatum(cursor.getString(1));
		spiel.setSpielerAnzahl(cursor.getInt(2));
		spiel.setSpielerStartkapital(cursor.getInt(3));
		spiel.setWaehrung(cursor.getString(4));

		// return spiel
		cursor.close();
		db.close();
		return spiel;
	}

	public void addSpieler(Spieler spieler) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colSpielerName, spieler.getSpielerName());
		values.put(colSpielerFarbe, spieler.getSpielerFarbe());
		values.put(colSpielerKapital, spieler.getSpielerKapital());
		values.put(colSpielerIpAdresse, spieler.getSpielerIpAdresse());
		values.put(colIdMonopolySpielListe, spieler.getIdMonopolySpiel());

		// Inserting Row
		db.insert(monopolySpielTable, null, values);
		db.close(); // Closing database connection
	}

	// get spieler by spielID and ipAdress
	public Spieler getSpielerBySpielIdAndSpielerIp(int spielID, String spielerIpAdress) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(monopolySpielTable, new String[] { colSpielerId,
						colSpielerName, colSpielerFarbe, colSpielerKapital, colSpielerIpAdresse, colIdMonopolySpielListe}, colSpielerIpAdresse + "=? and " + colIdMonopolySpielListe + "=?",
				new String[] { String.valueOf(spielerIpAdress), String.valueOf(spielID) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Spieler spieler = new Spieler(cursor.getString(4), cursor.getInt(5));
		spieler.setSpielerID(Integer.parseInt(cursor.getString(0)));
		spieler.setSpielerName(cursor.getString(1));
		spieler.setSpielerFarbe(cursor.getInt(2));
		spieler.setSpielerKapital(cursor.getInt(3));

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
		values.put(colIdMonopolySpielListe, spieler.getIdMonopolySpiel());

		// updating row
		int row = db.update(monopolySpielTable, values, colSpielerId + " = ?",
				new String[] { String.valueOf(spieler.getSpielerID()) });
		db.close(); // Closing database connection
	}

	// Getting All spieler
	public ArrayList<Spieler> getAllSpieler(int spielListeID) {
		ArrayList<Spieler> spielerListe = new ArrayList<Spieler>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + monopolySpielTable + " WHERE " + colIdMonopolySpielListe + " LIKE " + spielListeID;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Spieler spieler = new Spieler(cursor.getString(4), cursor.getInt(5));
				spieler.setSpielerID(Integer.parseInt(cursor.getString(0)));
				spieler.setSpielerName(cursor.getString(1));
				spieler.setSpielerFarbe(cursor.getInt(2));
				spieler.setSpielerKapital(cursor.getInt(3));

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
				spiel.setSpielerStartkapital(cursor.getInt(3));
				spiel.setWaehrung(cursor.getString(4));

				// Adding contact to list
				spieleListe.add(spiel);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return spieler list
		return spieleListe;
	}

	/*
	public // Adding new siteInduction
	void addSiteInduction(SiteInduction siteInduction) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colSiteName, siteInduction.getSiteName());
		values.put(colSiteInductionComplete,
				siteInduction.getSiteInductionComplete());
		values.put(colSiteInductionRequired,
				siteInduction.getSiteInductionRequired());
		values.put(colInductionHeldUntil, siteInduction.getInductionHeldUntil());
		values.put(colSitePosition, siteInduction.getSitePosition());

		// Inserting Row
		db.insert(siteInductionTable, null, values);
		db.close(); // Closing database connection
	}

	// Getting single siteInduction
	public SiteInduction getSiteInduction(String siteName) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(siteInductionTable, new String[] {
				colSiteInductionID, colSiteName, colSiteInductionComplete,
				colSiteInductionRequired, colInductionHeldUntil,
				colSitePosition }, colSiteName + "=?",
				new String[] { String.valueOf(siteName) }, null, null, null,
				null);
		if (cursor != null)
			cursor.moveToFirst();

		SiteInduction siteInduction = new SiteInduction(cursor.getInt(0),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), cursor.getString(5));

		// return siteInduction
		return siteInduction;
	}

	// Getting All siteInductions
	public ArrayList<SiteInduction> getAllSiteInductions() {
		ArrayList<SiteInduction> siteInductionArrayList = new ArrayList<SiteInduction>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + siteInductionTable;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				SiteInduction siteInduction = new SiteInduction();
				siteInduction.setSiteInductionId(cursor.getInt(0));
				siteInduction.setSiteName(cursor.getString(1));
				siteInduction.setSiteInductionComplete(cursor.getString(2));
				siteInduction.setSiteInductionRequired(cursor.getString(3));
				siteInduction.setInductionHeldUntil(cursor.getString(4));
				siteInduction.setSitePosition(cursor.getString(5));
				// Adding siteInduction to list
				siteInductionArrayList.add(siteInduction);
			} while (cursor.moveToNext());
		}

		// return siteInduction list
		return siteInductionArrayList;
	}

	// Getting All sites
	public ArrayList<String> getAllSites() {
		ArrayList<String> siteArrayList = new ArrayList<String>();
		// Select All Query
		String selectQuery = "SELECT " + colSiteName + " FROM "
				+ siteInductionTable + ";";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				String siteName = new String();
				siteName = cursor.getString(0);
				// Adding siteInduction to list
				siteArrayList.add(siteName);
			} while (cursor.moveToNext());
		}

		// return siteInduction list
		return siteArrayList;
	}

	// Adding new trainingCourse
	public void addTrainingCourse(TrainingCourses trainingCourses) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(colCourseName, trainingCourses.getCourseName());
		values.put(colRequired, trainingCourses.getRequired());
		values.put(colCompletedDate, trainingCourses.getCompletedDate());
		values.put(colExpireDate, trainingCourses.getExpireDate());
		values.put(colRenewableDate, trainingCourses.getRenewableDate());
		values.put(colCertificateLocation,
				trainingCourses.getCertificateLocation());
		values.put(colCourseDescription, trainingCourses.getCourseDescription());
		values.put(colRefreshPeriodMonth,
				trainingCourses.getRefreshPeriodMonth());
		values.put(colTrainingCourseCountry,
				trainingCourses.getTrainingCourseCountry());

		// Inserting Row
		db.insert(trainingCoursesTable, null, values);
		db.close(); // Closing database connection
	}

	// Getting single trainingCourse
	public TrainingCourses getTrainingCourse(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(trainingCoursesTable, new String[] {
				colTrainingCoursesId, colCourseName, colRequired,
				colCompletedDate, colExpireDate, colRenewableDate,
				colCertificateLocation, colCourseDescription,
				colRefreshPeriodMonth, colTrainingCourseCountry },
				colTrainingCoursesId + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		TrainingCourses trainingCourses = new TrainingCourses(cursor.getInt(0),
				cursor.getString(1), cursor.getString(2), cursor.getString(3),
				cursor.getString(4), cursor.getString(5), cursor.getString(6),
				cursor.getString(7), cursor.getString(8), cursor.getString(9));

		// return category
		return trainingCourses;
	}

	// Getting All training courses
	public ArrayList<TrainingCourses> getAllTrainingCourses() {
		ArrayList<TrainingCourses> trainingCoursesArrayList = new ArrayList<TrainingCourses>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + trainingCoursesTable;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				TrainingCourses trainingCourses = new TrainingCourses();
				trainingCourses.setTrainingCoursesId(cursor.getInt(0));
				trainingCourses.setCourseName(cursor.getString(1));
				trainingCourses.setRequired(cursor.getString(2));
				trainingCourses.setCompletedDate(cursor.getString(3));
				trainingCourses.setExpireDate(cursor.getString(4));
				trainingCourses.setRenewableDate(cursor.getString(5));
				trainingCourses.setCertificateLocation(cursor.getString(6));
				trainingCourses.setCourseDescription(cursor.getString(7));
				trainingCourses.setRefreshPeriodMonth(cursor.getString(8));
				trainingCourses.setTrainingCourseCountry(cursor.getString(9));
				// Adding siteInduction to list
				trainingCoursesArrayList.add(trainingCourses);
			} while (cursor.moveToNext());
		}

		// return siteInduction list
		return trainingCoursesArrayList;
	}

	// Getting site inductions Count
	public int getSiteInductionsCount() {
		String countQuery = "SELECT * FROM " + siteInductionTable;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int ergebnis = cursor.getCount();
		cursor.close();

		// return count
		return ergebnis;
	}

	// Getting training courses Count
	public int getTrainingCoursesCount() {
		String countQuery = "SELECT * FROM " + trainingCoursesTable;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int ergebnis = cursor.getCount();
		cursor.close();

		// return count
		return ergebnis;
	}
	*/
}
