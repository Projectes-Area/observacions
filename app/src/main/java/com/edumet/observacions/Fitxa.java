package com.edumet.observacions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Fitxa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitud;
    private double longitud;
    private String etiqueta;
    public int parametreID;
    public int numFenomen;

    public int getID() {
        return parametreID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitxa);

        Intent intent = getIntent();
        etiqueta = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
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
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng observacio = new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(observacio).title(nomFenomen(numFenomen)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio,15.0f));
    }

    public String nomFenomen(int i) {
        switch (i) {
            case 2:
                return "Oreneta";
            case 3:
                return "Ametller";
            case 4:
                return "Cirerer";
            case 1:
                return "Papallona";
            default:
                return "Genèric";
        }
    }

}
