package com.edumet.observacions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class Estacions extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private static boolean mRequestingLocationUpdates;

    private GoogleMap mMap;

    DataHelper mDbHelper;
    private SQLiteDatabase db;

    SharedPreferences sharedPref;
    BottomNavigationView navigation;

    private boolean flagLocalitzada = false;

    private int estacioPreferida;
    private int ID_estacioPreferida = 0;

    List IDS_Edumet = new ArrayList<>();
    List latituds = new ArrayList<>();
    List longituds = new ArrayList<>();

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
        //ab.setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        estacioPreferida = sharedPref.getInt("estacio_preferida", 0);

        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);

        String[] projection = {
                Database.Estacions.COLUMN_NAME_ID_EDUMET,
                Database.Estacions.COLUMN_NAME_LATITUD,
                Database.Estacions.COLUMN_NAME_LONGITUD,
        };
        String selection = Database.Estacions._ID + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        mDbHelper = new DataHelper(this);
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        while (cursor.moveToNext()) {
            IDS_Edumet.add(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ID_EDUMET)));
            latituds.add(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LATITUD)));
            longituds.add(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LONGITUD)));
        }
        cursor.close();
        mDbHelper.close();
        obreMapa();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            if (flagLocalitzada == true) {
                switch (item.getItemId()) {
                    case R.id.navigation_observacions:
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_estacions:
                        return true;
                    case R.id.navigation_radar:
                        intent = new Intent(getApplicationContext(), Radar.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_pronostic:
                        intent = new Intent(getApplicationContext(), Pronostic.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        startLocationUpdates();
    }


    //
    // LOCALITZACIÓ
    //

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            updateLocationUI();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i("..Location Request", "Done");
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
        Log.i("..Location Callback", "Done");
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        Log.i("..Location Settings", "Done");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("..onActResult", "Done");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.i("..OnActResult", "User agreed to make required location settings changes.");
                        mRequestingLocationUpdates = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("..OnActResult", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        finish();
                        break;
                }
                updateLocationUI();
                Log.i("..OnActResult", String.valueOf(mRequestingLocationUpdates));
                break;
        }
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("..startLocation", "All location settings are satisfied.");
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mRequestingLocationUpdates = true;
                        updateLocationUI();

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("..startUpdates", "Location settings are not satisfied. Attempting to upgrade location settings.");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(Estacions.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("..startUpdates", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(getApplicationContext(), R.string.fix_settings, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.i("..stopUpdates", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            if (!flagLocalitzada) {
                flagLocalitzada = true;
                desaPreferencies();
            }
        }
    }

    //
    // MAPA
    //

    public void obreMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa_estacions);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (Integer.valueOf(marker.getTag().toString()) > 0) {
                    FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
                    fragment.clicaSpinner(Integer.valueOf(marker.getTag().toString()));
                } else {
                    marker.showInfoWindow();
                    //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        estacioPreferida = sharedPref.getInt("estacio_preferida", 0);
        Log.i("..NumEstacions", String.valueOf(IDS_Edumet.size()));

        LatLng observacio;
        Marker marcador;
        for (int i = 0; i < IDS_Edumet.size(); i++) {
            observacio = new LatLng(Double.valueOf(latituds.get(i).toString()), Double.valueOf(longituds.get(i).toString()));
            marcador = mMap.addMarker(new MarkerOptions()
                    .position(observacio)
            );
            marcador.setTag(i);
            if (Integer.valueOf(IDS_Edumet.get(i).toString()) == estacioPreferida) {
                ID_estacioPreferida = i;
                //marcador.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
        }
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
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        Log.i("..OnPause", String.valueOf(mRequestingLocationUpdates));
    }

    @Override
    public void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_estacions);
        Log.i(".OnResume", String.valueOf(mRequestingLocationUpdates));
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onBackPressed() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), "Vols sortir de l'App ?", Snackbar.LENGTH_LONG)
                .setAction("SÍ", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
        snackbar.show();
    }

    public void desaPreferencies() {
        SharedPreferences sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("latitud", String.valueOf(mCurrentLocation.getLatitude()));
        editor.putString("longitud", String.valueOf(mCurrentLocation.getLongitude()));
        Log.i("...latitud_actual", String.valueOf(mCurrentLocation.getLatitude()));

        int estacioPreferida = sharedPref.getInt("estacio_preferida", 0);
        if (estacioPreferida == 0) {
            int estacioPropera = 0;
            double distanciaPropera = 1000000;

            for (int i = 0; i < IDS_Edumet.size(); i++) {
                Double distancia = calculaDistancia(
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(),
                        Double.valueOf(latituds.get(i).toString()),
                        Double.valueOf(longituds.get(i).toString())
                );
                if (distancia < distanciaPropera) {
                    distanciaPropera = distancia;
                    estacioPropera = Integer.valueOf(IDS_Edumet.get(i).toString());
                    ID_estacioPreferida = i;
                }
            }
            Log.i("...Estacio propera", String.valueOf(estacioPropera));
            Log.i("...ID_Estacio preferida", String.valueOf(ID_estacioPreferida));
            editor.putInt("estacio_preferida", estacioPropera);
            editor.putInt("estacio_actual", estacioPropera);
        }
        editor.apply();

        LatLng observacio = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        Marker marcador;
        marcador = mMap.addMarker(new MarkerOptions()
                .position(observacio)
                .title("Ubicació actual")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        );
        marcador.setTag(0);

        FragmentEstacions fragment = (FragmentEstacions) getSupportFragmentManager().findFragmentById(R.id.fragment_estacions_container);
        fragment.clicaSpinner(ID_estacioPreferida);
    }

    public double calculaDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }
}
