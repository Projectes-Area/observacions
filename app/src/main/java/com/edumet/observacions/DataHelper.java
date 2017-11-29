package com.edumet.observacions;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES_PARAMETRES =
            "CREATE TABLE " + Database.Observacions.TABLE_NAME + " (" +
                    Database.Observacions._ID + " INTEGER PRIMARY KEY," +
                    Database.Observacions.COLUMN_NAME_ID_EDUMET + " TEXT," +
                    Database.Observacions.COLUMN_NAME_DIA + " TEXT," +
                    Database.Observacions.COLUMN_NAME_HORA + " TEXT," +
                    Database.Observacions.COLUMN_NAME_LATITUD + " TEXT," +
                    Database.Observacions.COLUMN_NAME_LONGITUD + " TEXT," +
                    Database.Observacions.COLUMN_NAME_FENOMEN + " TEXT," +
                    Database.Observacions.COLUMN_NAME_DESCRIPCIO + " TEXT," +
                    Database.Observacions.COLUMN_NAME_PATH + " TEXT," +
                    Database.Observacions.COLUMN_NAME_PATH_ENVIA + " TEXT," +
                    Database.Observacions.COLUMN_NAME_ENVIAT + " TEXT)";

    private static final String SQL_CREATE_ENTRIES_ESTACIONS =
            "CREATE TABLE " + Database.Estacions.TABLE_NAME + " (" +
                    Database.Estacions._ID + " INTEGER PRIMARY KEY," +
                    Database.Estacions.COLUMN_NAME_ID_EDUMET + " TEXT," +
                    Database.Estacions.COLUMN_NAME_CODI + " TEXT," +
                    Database.Estacions.COLUMN_NAME_NOM + " TEXT," +
                    Database.Estacions.COLUMN_NAME_POBLACIO + " TEXT," +
                    Database.Estacions.COLUMN_NAME_LATITUD + " TEXT," +
                    Database.Estacions.COLUMN_NAME_LONGITUD + " TEXT," +
                    Database.Estacions.COLUMN_NAME_ALTITUD + " TEXT," +
                    Database.Estacions.COLUMN_NAME_SITUACIO+ " TEXT," +
                    Database.Estacions.COLUMN_NAME_ESTACIO + " TEXT," +
                    Database.Estacions.COLUMN_NAME_CLIMA+ " TEXT)";

    private static final String SQL_DELETE_ENTRIES_PARAMETRES ="DROP TABLE IF EXISTS " + Database.Observacions.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_ESTACIONS ="DROP TABLE IF EXISTS " + Database.Estacions.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Database.db";

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        
        db.execSQL(SQL_CREATE_ENTRIES_PARAMETRES);
        db.execSQL(SQL_CREATE_ENTRIES_ESTACIONS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES_PARAMETRES);
        db.execSQL(SQL_DELETE_ENTRIES_ESTACIONS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
