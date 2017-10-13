package com.edumet.observacions;

import android.Manifest;
import android.app.Activity;
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

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100000;
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
    private Button mStartUpdatesButton;
    private TextView GPS;
    private TextView Adressa;
    // Labels.
    private String mGPSLabel;
    private Boolean mRequestingLocationUpdates;

    private Location mLastLocation;
    private boolean mAddressRequested;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;

    private ProgressBar mProgressBar;

    private static final String EXTRA_FILENAME = "com.edumet.observacions.EXTRA_FILENAME";
    private static final int CONTENT_REQUEST = 1337;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final String PHOTOS = "fotos";
    private File output = null;
    private ImageView imatge;
    Button Foto;
    Button Envia;
    Bitmap bitmap;
    String mCurrentPhotoPath;

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
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        GPS = (TextView) findViewById(R.id.lat_long);
        Adressa = (TextView) findViewById(R.id.adressa);
        mGPSLabel = getResources().getString(R.string.latitude_label);
        mRequestingLocationUpdates = false;
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        updateUIWidgets();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getAddress();
        }
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
            updateUI();
        }
    }

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
                            showToast(getString(R.string.no_geocoder_available));
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

    private void updateUIWidgets() {
        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
                        updateUI();
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
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        updateUI();
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
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);

        } else {
            mStartUpdatesButton.setEnabled(true);
        }
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
            updateUIWidgets();
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
                        setButtonsEnabledState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putSerializable(EXTRA_FILENAME, output);
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
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
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
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
                getAddress();
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
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

    private void fesFoto() {
        if (mCurrentLocation != null) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) {
                try {
                    output = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                if (output != null) {
                    Uri outputUri = FileProvider.getUriForFile(this, "com.edumet.observacions", output);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip =
                                ClipData.newUri(getContentResolver(), "A photo", outputUri);

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
        BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //bmOptions.inPurgeable = true;
        bitmap = BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);
        imatge.setImageBitmap(bitmap);
    }

    public void sendPost() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] fotografia = baos.toByteArray();

                    String encodedFoto = Base64.encodeToString(fotografia, Base64.DEFAULT);

                    URL url = new URL("https://edumet.cat/edumet/meteo_2/dades_recarregar_feno.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("fitxer", encodedFoto);
                    jsonParam.put("usuari", 43900018);
                    jsonParam.put("lat", 41.3985);
                    jsonParam.put("lon", 2.1398);
                    jsonParam.put("id_feno", "Aus--Oreneta");
                    jsonParam.put("descripcio", "una observació");
                    jsonParam.put("tab", "");

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    conn.disconnect();
                    mProgressBar.setVisibility(ProgressBar.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
            showToast(getString(R.string.dades_enviades));
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            updateUIWidgets();
        }
    }
}





