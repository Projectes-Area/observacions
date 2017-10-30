package com.edumet.observacions;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 40980055N on 27/10/17.
 */

public class Fitxa extends Fragment {

    DadesHelper mDbHelper;

    private ImageButton Envia;
    private ImageButton Mapa;
    private ImageButton Esborra;
    private ImageView imatge;
    private TextView fenomen;
    private TextView data;
    private TextView descripcio;
    private ProgressBar mProgressBar;

    private int numID;
    private String elDia;
    private String laHora;
    private double laLatitud;
    private double laLongitud;
    private String elFenomen;
    private String laDescripcio;
    private String elPath;
    private String elPath_Envia;
    private String elPath_Vista;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fitxa, container, false);
        numID=getArguments().getInt("numID");

        mDbHelper = new DadesHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        fenomen=(TextView) v.findViewById(R.id.lblFenomen);
        Envia=(ImageButton) v.findViewById(R.id.btnEnvia);
        Mapa=(ImageButton) v.findViewById(R.id.btnMapa);
        Esborra=(ImageButton) v.findViewById(R.id.btnEsborra);
        imatge = (ImageView) v.findViewById(R.id.imgFoto);
        data = (TextView) v.findViewById(R.id.lblData);
        descripcio = (TextView) v.findViewById(R.id.lblDescripcio);

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                DadesEstructura.Parametres._ID,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_LATITUD,
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO,
                DadesEstructura.Parametres.COLUMN_NAME_PATH,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_VISTA,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        // Filter results
        String selection = DadesEstructura.Parametres._ID + " = ?";
        String[] selectionArgs = {String.valueOf(numID)};
        //String sortOrder = DadesEstructura.Parametres.COLUMN_NAME_DIA+ " DESC";
        String sortOrder =null;

        Cursor cursor = db.query(
                DadesEstructura.Parametres.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(cursor.moveToNext()) {
            if (Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres._ID)))==numID) {
                elDia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
                laHora = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
                laLatitud=Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LATITUD)));
                laLongitud=Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD)));
                elFenomen = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN));
                laDescripcio = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO));
                elPath = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH));
                elPath_Envia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA));
                elPath_Vista = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_VISTA));
            }
        }
        cursor.close();

        return v;
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm:ss");

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(elDia);
            format = new SimpleDateFormat("HH:mm:ss");
            Date time = format.parse(laHora);
            data.setText(dateCatala.format(date.getTime())+" "+horaCatala.format(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        descripcio.setText(laDescripcio);
        imatge.setImageBitmap(BitmapFactory.decodeFile(elPath_Vista));
    }
}
