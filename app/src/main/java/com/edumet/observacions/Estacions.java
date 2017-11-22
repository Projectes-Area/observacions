package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class Estacions extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    EstacionsHelper mDbHelper;
    private SQLiteDatabase db;

    private double latitud;
    private double longitud;

    private int estacioPreferida;

    SharedPreferences sharedPref;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estacions);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.estacions_toolbar);
        setSupportActionBar(myToolbar);

        FragmentEstacions firstFragment = new FragmentEstacions();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_estacions_container, firstFragment).commit();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);

        Double latBarcelona=41.3985;
        Double lonBarcelona=2.1398;

        latitud = Double.valueOf(sharedPref.getString("latitud", String.valueOf(latBarcelona)));
        longitud = Double.valueOf(sharedPref.getString("longitud",  String.valueOf(lonBarcelona)));
        estacioPreferida=sharedPref.getInt("estacio_preferida", 0);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_estacions);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
/*                    intent = new Intent(getApplicationContext(),Estacions.class);
                    startActivity(intent);*/
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(),Radar.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    //intent = new Intent(getApplicationContext(),Proostic.class);
                    //startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    public void obreMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa_estacions);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        String[] projection = {
                DadesEstacions.Parametres._ID,
                DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstacions.Parametres.COLUMN_NAME_LATITUD,
                DadesEstacions.Parametres.COLUMN_NAME_LONGITUD,
        };

        String selection = DadesEstacions.Parametres._ID+ " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        mDbHelper = new EstacionsHelper(this);
        db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
                fragment.clicaSpinner(Integer.valueOf(marker.getSnippet())-1);
                return true;
            }
        });

        int estacioPropera=0;
        int ID_estacioPropera=0;
        double distanciaPropera=1000000;

        LatLng observacio;

        while (cursor.moveToNext()) {
            observacio = new LatLng(Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LATITUD))),
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD))));
            mMap.addMarker(new MarkerOptions()
                    .position(observacio)
                    .snippet(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres._ID)))
            );


            Double distancia = calculaDistancia(
                    latitud,
                    longitud,
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LATITUD))),
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD))));

            Log.i("km", String.valueOf(distancia));

            if (distancia<distanciaPropera) {
                distanciaPropera=distancia;
                estacioPropera=Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET)));
                ID_estacioPropera=Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres._ID)));
            }
        }
        cursor.close();

        if(estacioPreferida==0) {
            estacioPreferida=estacioPropera;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("estacio_preferida", estacioPropera);
            editor.apply();
        }

        FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
        fragment.clicaSpinner(ID_estacioPropera-1);

        LatLng posicio = new LatLng(latitud, longitud);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicio, 11.0f));
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.edumet_web:
                Uri uri = Uri.parse("https://edumet.cat/edumet/meteo_2/index.php");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intent2 = new Intent();
                intent2.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri2 = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent2.setData(uri2);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void mouCamera(double lat, double lon) {
        LatLng posicio = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicio, 11.0f));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
}
