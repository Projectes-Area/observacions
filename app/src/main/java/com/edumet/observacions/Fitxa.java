package com.edumet.observacions;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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
        //MenuItem eduMenu= (MenuItem) findViewById(R.id.action_settings);
        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        Intent intent = getIntent();
        latitud= Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LATITUD));
        longitud= Double.valueOf(intent.getStringExtra(MainActivity.EXTRA_LONGITUD));
        numFenomen= Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_NUMFENOMEN));
        parametreID=Integer.valueOf(intent.getStringExtra(MainActivity.EXTRA_ID));

        Log.i("numID-Activity",String.valueOf(parametreID));

        FragmentFitxa firstFragment = new FragmentFitxa();
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        //firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_mapa_container, firstFragment).commit();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

/*        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.fitxa_toolbar);
        setSupportActionBar(myChildToolbar);*/

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng observacio = new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(observacio).title(nomFenomen[numFenomen]));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio,15.0f));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
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
