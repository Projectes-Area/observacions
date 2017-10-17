package com.edumet.observacions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Using location settings.
 * <p/>
 * Uses the {@link com.google.android.gms.location.SettingsApi} to ensure that the device's system
 * settings are properly configured for the app's location needs. When making a request to
 * Location services, the device's system settings may be in a state that prevents the app from
 * obtaining the location data that it needs. For example, GPS or Wi-Fi scanning may be switched
 * off. The {@code SettingsApi} makes it possible to determine if a device's system settings are
 * adequate for the location request, and to optionally invoke a dialog that allows the user to
 * enable the necessary settings.
 * <p/>
 * This sample allows the user to request location updates using the ACCESS_FINE_LOCATION setting
 * (as specified in AndroidManifest.xml).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;
    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    //The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // UI Widgets.
    private Button Foto;
    private Button Envia;
    private TextView GPS;
    private TextView Adressa;
    private ImageView imatge;
    private ProgressBar mProgressBar;

    private String mGPSLabel;
    private String mAddressOutput;
    private Boolean mRequestingLocationUpdates;
    private Location mLastLocation;
    private boolean mAddressRequested;
    private AddressResultReceiver mResultReceiver;

    private static final int CONTENT_REQUEST = 1337;
    private static final String EXTRA_FILENAME = "com.edumet.observacions.EXTRA_FILENAME";
    private String mCurrentPhotoPath;
    private File output = null;
    private Bitmap bitmap;
    private Bitmap bitmapEnv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mAddressRequested = false;
        mAddressOutput = "";
        Foto = (Button) findViewById(R.id.btnFoto);
        Envia = (Button) findViewById(R.id.btnEnvia);
        Foto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fesFoto();
            }
        });
        Envia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendPost();
            }
        });
        imatge = (ImageView) findViewById(R.id.imgFoto);
        // Locate the UI widgets.
        GPS = (TextView) findViewById(R.id.txtGPS);
        Adressa = (TextView) findViewById(R.id.txtAdressa);
        mGPSLabel = getResources().getString(R.string.latitude_label);
        mRequestingLocationUpdates = false;
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putSerializable(EXTRA_FILENAME, output);
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        }
        startLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocationUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imatge.setImageBitmap(bitmap);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                Adressa.setText(mAddressOutput);
            }
            updateLocationUI();
        }
    }

    //
    // LOCALITZACIÓ
    //

    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @SuppressWarnings("MissingPermission")
    private void getAddress() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.w(TAG, "onSuccess:null");
                            return;
                        }
                        mLastLocation = location;
                        if (!Geocoder.isPresent()) {
                            Toast.makeText(MainActivity.this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (mAddressRequested) {
                            startIntentService();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getLastLocation:onFailure", e);
                    }
                });
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
            case CONTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    setPic();
                    galleryAddPic();
                    Log.i(TAG, "Added to gallery.");
                    Envia.setEnabled(true);
                }
                break;
        }
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
        getAddress();
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });
    }


    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            GPS.setText(mGPSLabel + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
            Foto.setEnabled(true);
            if (mLastLocation != null) {
                startIntentService();
                return;
            }
            mAddressRequested = true;
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
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

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                }
            } else {
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Adressa.setText(mAddressOutput);
            mAddressRequested = false;
        }
    }

    //
    // FOTOGRAFIA
    //

    private void fesFoto() {
        if (mCurrentLocation != null) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) {
                try {
                    output = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(this, R.string.fitxer_error, Toast.LENGTH_LONG).show();
                    finish();
                }
                if (output != null) {
                    Uri outputUri = FileProvider.getUriForFile(this, "com.edumet.observacions", output);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip =
                                ClipData.newUri(getContentResolver(), "Una foto", outputUri);

                        i.setClipData(clip);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        List<ResolveInfo> resInfoList =
                                getPackageManager()
                                        .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            grantUriPermission(packageName, outputUri,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                    try {
                        startActivityForResult(i, CONTENT_REQUEST);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, R.string.msg_no_camera, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        } else {
            Toast.makeText(this, R.string.encara_sense_lloc, Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(timeStamp, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



    private void setPic() {
        int targetW = imatge.getWidth();
        int targetH = imatge.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imatge.setImageBitmap(bitmap);
    }

    private void setPicEnv() {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmapEnv = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    public void sendPost() {
        ByteArrayOutputStream baosEnv = new ByteArrayOutputStream();
        setPicEnv();
        bitmapEnv.compress(Bitmap.CompressFormat.JPEG, 100, baosEnv);
        byte[] fotografia = baosEnv.toByteArray();

        String encodedFoto = Base64.encodeToString(fotografia, Base64.DEFAULT);

        final OkHttpClient client = new OkHttpClient();

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("fitxer", encodedFoto);
            jsonParam.put("usuari", 43900018);
            jsonParam.put("lat", 41.3985);
            jsonParam.put("lon", 2.1398);
            jsonParam.put("id_feno", "Aus--Oreneta");
            jsonParam.put("descripcio", "una observació");
            jsonParam.put("tab", "salvarFenoApp");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("JSON", jsonParam.toString());

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonParam.toString());

        final Request request = new Request.Builder()
                //.url("https://edumet.cat/edumet/meteo_2/dades_recarregar_feno.php")
                .url("http://tecnologia.isantandreu.net/prova.php")
                //url("https://edumet.cat/edumet/meteo_2/prova.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "auth")
                .addHeader("cache-control", "no-cache")
                .build();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                    }
                                                });
                                                Log.i("APP", getString(R.string.error_connexio));
                                            }
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                                Log.i("RESPONSE", response.toString());
                                                Log.i("CONTENT", response.body().string());
                                                if (response.isSuccessful()) {
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), R.string.dades_enviades, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("APP", getString(R.string.dades_enviades));
                                                } else {
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("APP", getString(R.string.error_servidor));
                                                }
                                            }
                                        }
        );
    }

    //
    // GENERAL
    //

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
    }
}






