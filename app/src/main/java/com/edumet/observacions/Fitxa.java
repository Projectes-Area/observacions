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
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_mapa_container, firstFragment).commit();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActionBar ab = getSupportActionBar();
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
        getMenuInflater().inflate(R.menu.fitxa_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentFitxa fragment = (FragmentFitxa) fm.findFragmentById(R.id.fragment_mapa_container);
        Intent intent;
        Uri uri;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mostra_imatge:
                fragment.veure_foto();
                return true;
            case R.id.veure_mapa:
                fragment.veure_mapa();
                return true;
            case R.id.envia_observacio:
                fragment.sendPost();
                return true;
            case R.id.elimina:
                fragment.esborra();
                return true;
            case R.id.les_meves_observacions:
                this.finish();
                return true;
            case R.id.edumet_web:
                uri = Uri.parse("https://edumet.cat/edumet/meteo_2/index.php");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
