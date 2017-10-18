package com.edumet.observacions;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 40980055N on 18/10/17.
 */

public class DadesHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DadesEstructura.PendentsEntry.TABLE_NAME + " (" +
                    DadesEstructura.PendentsEntry._ID + " INTEGER PRIMARY KEY," +
                    DadesEstructura.PendentsEntry.COLUMN_NAME_LATITUD + " TEXT," +
                    DadesEstructura.PendentsEntry.COLUMN_NAME_LONGITUD + " TEXT," +
                    DadesEstructura.PendentsEntry.COLUMN_NAME_FENOMEN + " TEXT," +
                    DadesEstructura.PendentsEntry.COLUMN_NAME_DESCRIPCIO + " TEXT," +
                    DadesEstructura.PendentsEntry.COLUMN_NAME_PATH + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DadesEstructura.PendentsEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Database.db";

    public DadesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}