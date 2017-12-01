package com.edumet.observacions;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Estacions extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SQLiteDatabase db;

    DataHelper mDbHelper;
    SharedPreferences sharedPref;
    BottomNavigationView navigation;

    private int estacioPreferida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estacions);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.estacions_toolbar);
        setSupportActionBar(myToolbar);

        FragmentEstacions firstFragment = new FragmentEstacions();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_estacions_container, firstFragment).commit();

        FragmentInfoEstacio secFragment = new FragmentInfoEstacio();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_info_container, secFragment).commit();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        estacioPreferida=sharedPref.getInt("estacio_preferida", 0);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);
        //navigation.setSelectedItemId(R.id.navigation_estacions);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(),Radar.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    intent = new Intent(getApplicationContext(),Pronostic.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
                Database.Estacions._ID,
                Database.Estacions.COLUMN_NAME_ID_EDUMET,
                Database.Estacions.COLUMN_NAME_LATITUD,
                Database.Estacions.COLUMN_NAME_LONGITUD,
        };

        String selection = Database.Estacions._ID+ " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        mDbHelper = new DataHelper(this);
        db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
                //fragment.buidaInfo();
                fragment.clicaSpinner(Integer.valueOf(marker.getSnippet())-1);
                return true;
            }
        });

        estacioPreferida=sharedPref.getInt("estacio_preferida", 0);
        int ID_estacioPreferida=0;

        LatLng observacio;

        while (cursor.moveToNext()) {
            observacio = new LatLng(Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LATITUD))),
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LONGITUD))));
            mMap.addMarker(new MarkerOptions()
                    .position(observacio)
                    .snippet(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions._ID)))
            );
            if(cursor.getInt(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ID_EDUMET))==estacioPreferida) {
                ID_estacioPreferida=cursor.getInt(cursor.getColumnIndexOrThrow(Database.Estacions._ID));
            }
        }
        cursor.close();

        FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
        //fragment.buidaInfo();
        fragment.clicaSpinner(ID_estacioPreferida-1);
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

    @Override
    public void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_estacions);
    }


}
