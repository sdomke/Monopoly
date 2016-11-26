package com.monopoly.domke.sebastian.monopoly.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int dbVersion = 1;

	// Database Name
	private static final String dbName = "MonopolyAppDB";

	// site induction table name
	private static final String monopolySpielTable = "TableMonopolySpiel";

	// monopoly spiel table column name
	private static final String colMonopolySpielerId = "MonopolySpielId";
	private static final String colSpielerName = "SpielerName";
	private static final String colSpielerFarbe = "SpielerFarbe";
	private static final String colSpielerKapital = "SpielerKapital";
	private static final String colFreiParken = "FreiParken";
	private static final String colIdMonopolySpielListe = "IdMonopolySpielListe";

	// training courses Table name
	private static final String spielListeTable = "TableSpielListe";

	// training courses Table column name
	private static final String colMonopolySpielListeId = "MonopolySpielListeId";
	private static final String colMonopolySpielListeDatum = "MonopolySpielListeDatum";
	private static final String colMonopolySpielListeSpielerAnzahl = "MonopolySpielListeSpielerAnzahl";
	private static final String colMonopolySpielListeStartkapital = "MonopolySpielListeStartkapital";

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
				+ "(" + colMonopolySpielerId + " INTEGER PRIMARY KEY,"
				+ colSpielerName + " TEXT," + colSpielerFarbe
				+ " INTEGER," + colSpielerKapital + " INTEGER,"
				+ colFreiParken + " INTEGER, " + colIdMonopolySpielListe + " INTEGER NOT NULL" + " FOREIGN KEY (" + colMonopolySpielListeId + ") REFERENCES " + spielListeTable + " (" + colIdMonopolySpielListe + ")" + " ON DELETE CASCADE" + ")";

		String createSpielListeTABLE = "CREATE TABLE "
				+ spielListeTable + "(" + colMonopolySpielListeId
				+ " INTEGER PRIMARY KEY," + colMonopolySpielListeDatum + " TEXT,"
				+ colMonopolySpielListeSpielerAnzahl + " INTEGER," + colMonopolySpielListeStartkapital + " INTEGER" + ")";

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

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

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
