package com.edumet.observacions;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Resources;
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
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Fitxa extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitud;
    private double longitud;
    public int parametreID;
    public int numFenomen;
    String[] nomFenomen;

    public int getID() {
        return parametreID;
    }

    //String[] nomFenomen;

    BottomNavigationView navigation;
    DataHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitxa);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.fitxa_toolbar);
        setSupportActionBar(myToolbar);

        //Resources res = getResources();
        //nomFenomen = res.getStringArray(R.array.nomFenomen);

        List<String> nomFenomen0 = new ArrayList<String>();
        //List<String> categories = new ArrayList<String>();

        String[] projection0 = {
                DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO,
        };
        String selection0 = DatabaseFeno.Fenologies.COLUMN_NAME_ID_FENO + " > ?";
        String[] selectionArgs0 = {"0"};
        String sortOrder0 = null;


        mDbHelper = new DataHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor0 = db.query(DatabaseFeno.Fenologies.TABLE_NAME_FENO, projection0, selection0, selectionArgs0, null, null, sortOrder0);

        while (cursor0.moveToNext()) {
            nomFenomen0.add(cursor0.getString(cursor0.getColumnIndexOrThrow(DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO)));
            //categories.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO)));
        }
        cursor0.close();
        mDbHelper.close();

        nomFenomen = nomFenomen0.toArray(new String[0]);





        Intent intent = getIntent();
        latitud = Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LATITUD));
        longitud = Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LONGITUD));
        numFenomen = Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_NUMFENOMEN));
        parametreID = Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_ID_App));

        FragmentFitxa firstFragment = new FragmentFitxa();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_mapa_container, firstFragment).commit();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);
        //navigation.setSelectedItemId(R.id.navigation_observacions);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getApplicationContext(),Estacions.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(),Radar.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    intent = new Intent(getApplicationContext(),Pronostic.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng observacio = new LatLng(latitud, longitud);
        mMap.addMarker(new MarkerOptions().position(observacio).title(nomFenomen[numFenomen]));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio, 15.0f));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fitxa_toolbar, menu);
        return true;
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

    @Override
    public void onBackPressed() {
                super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_observacions);
    }


}
