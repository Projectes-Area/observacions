package com.edumet.observacions;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
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

public class Fitxa extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitud;
    private double longitud;
    public int parametreID;
    public int numFenomen;

    public int getID() {
        return parametreID;
    }


    String[] nomFenomen;

    String usuari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitxa);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.fitxa_toolbar);
        setSupportActionBar(myToolbar);

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        Intent intent = getIntent();
        latitud = Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LATITUD));
        longitud = Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LONGITUD));
        numFenomen = Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_NUMFENOMEN));
        parametreID = Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_ID));

        FragmentFitxa firstFragment = new FragmentFitxa();
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        //firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_mapa_container, firstFragment).commit();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng observacio = new LatLng(latitud, longitud);
        mMap.addMarker(new MarkerOptions().position(observacio).title(nomFenomen[numFenomen]));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio, 15.0f));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

/*                Intent intent = new Intent(this, Visor.class);
                intent.putExtra(MainActivity.EXTRA_PATH, "edumet_web");
                startActivity(intent);*/
                return true;

            case R.id.action_settings:
                // Build intent that displays the App settings screen.
                Intent intent2 = new Intent();
                intent2.setAction(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri2 = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent2.setData(uri2);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                return true;

/*            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;*/


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
