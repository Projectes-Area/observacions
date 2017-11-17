package com.edumet.observacions;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
    }

    public void obreMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa_estacions);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        String[] projection = {
                DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstacions.Parametres.COLUMN_NAME_NOM,
                DadesEstacions.Parametres.COLUMN_NAME_LATITUD,
                DadesEstacions.Parametres.COLUMN_NAME_LONGITUD,
        };

        String selection = DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = "id_edumet ASC";

        mDbHelper = new EstacionsHelper(this);
        db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("marker",marker.getSnippet());

                FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
                fragment.mostraEstacio(Integer.valueOf(marker.getSnippet()));

                return true;
            }
        });


        LatLng observacio = new LatLng(0, 0);

        while (cursor.moveToNext()) {
            observacio = new LatLng(Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LATITUD))),
                    Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD))));
            mMap.addMarker(new MarkerOptions()
                    .position(observacio)
                    .title(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_NOM)))
                    .snippet(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET)))
            );
        }
        cursor.close();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(observacio, 7.0f));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.fitxa_toolbar, menu);
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
}
