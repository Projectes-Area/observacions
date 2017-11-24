package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Actuals extends AppCompatActivity {

    SharedPreferences sharedPref;
    private int id_edumet;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actuals);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.actuals_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_pronostic);

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        id_edumet = sharedPref.getInt("estacio_actual", 0);
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getApplicationContext(), Estacions.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(), Web_viewer.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("desti","web_viewer");
                    startActivity(intent);

                    return true;
                case R.id.navigation_pronostic:
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.observacions_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public int estacioPropera() {
        EstacionsHelper mDbHelper;
        SQLiteDatabase db;
        mDbHelper = new EstacionsHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] projection = {
                DadesEstacions.Parametres._ID,
                DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstacions.Parametres.COLUMN_NAME_LATITUD,
                DadesEstacions.Parametres.COLUMN_NAME_LONGITUD,
        };

        String selection = DadesEstacions.Parametres._ID+ " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        //int estacioPropera = 0;
        int ID_estacioPropera = 0;
        double distanciaPropera = 1000000;

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);

        Double latBarcelona=41.3985;
        Double lonBarcelona=2.1398;

        double latitud = Double.valueOf(sharedPref.getString("latitud", String.valueOf(latBarcelona)));
        double longitud = Double.valueOf(sharedPref.getString("longitud",  String.valueOf(lonBarcelona)));

        while (cursor.moveToNext()) {
            Double distancia = calculaDistancia(
                    latitud,
                    longitud,
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LATITUD))),
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD))));

            Log.i("km", String.valueOf(distancia));

            if (distancia < distanciaPropera) {
                distanciaPropera = distancia;
                //estacioPropera = Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET)));
                ID_estacioPropera = Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres._ID)));
            }
        }
        cursor.close();
        return ID_estacioPropera;
    }

    public double calculaDistancia(Double lat1,Double lon1,Double lat2,Double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    @Override
    public void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_pronostic);
    }
}

