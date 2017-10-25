package com.edumet.observacions;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import static android.app.Activity.RESULT_OK;
import static java.lang.String.valueOf;

public class Captura extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    //The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private static final int CONTENT_REQUEST = 1337; // fotos
    private static final String EXTRA_FILENAME = "com.edumet.observacions.EXTRA_FILENAME";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    // UI Widgets.
    private ImageButton Foto;
    private ImageButton Girar;
    private ImageButton Envia;
    private ImageButton Desa;
    private ImageButton Pendents;
    private ImageButton Mapa;
    private TextView GPS;
    private ImageView imatge;
    private EditText observacio;
    private ProgressBar mProgressBar;
    private Spinner spinner;

    private String mGPSLabel;
    private String timeStamp;
    private String pathIcon;
    private String pathVista;
    private String pathEnvia;
    private String mCurrentPhotoPath;
    private String minPhotoPath;
    static private boolean mRequestingLocationUpdates;
    private File output = null;
    private File outputMiniatura=null;
    private int midaEnvio=800;
    private int midaVista=200;
    private int midaIcon=60;
    private Bitmap bitmap;
    private Bitmap bitmapTemp;
    private int num_fenomen = 0;
    private int angle_foto;
    private boolean jaLocalitzat=false;

    DadesHelper mDbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.captura, container, false);

        //Lloc = (Button) v.findViewById(R.id.btnGPS);
        Foto = (ImageButton) v.findViewById(R.id.btnFoto);
        Girar = (ImageButton) v.findViewById(R.id.btnGirar);
        Envia = (ImageButton) v.findViewById(R.id.btnEnvia);
        Desa = (ImageButton) v.findViewById(R.id.btnDesa);
        Pendents = (ImageButton) v.findViewById(R.id.btnPendents);
        Mapa = (ImageButton) v.findViewById(R.id.btnMapa);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        GPS = (TextView) v.findViewById(R.id.txtGPS);
        imatge = (ImageView) v.findViewById(R.id.imgFoto);
        observacio = (EditText) v.findViewById(R.id.txtObservacions);
        spinner = (Spinner) v.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.fenomens, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mDbHelper = new DadesHelper(getContext());

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Foto.setEnabled(false);
        Foto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fesFoto();
            }
        });
        Girar.setEnabled(false);
        Girar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                angle_foto+=90;
                if (angle_foto>=360) {
                    angle_foto=0;
                }
                Log.i("ANGLE", valueOf(angle_foto));
                bitmap=rotateViaMatrix(bitmap,90);
                imatge.setImageBitmap(bitmap);
            }
        });
        Envia.setEnabled(false);
        Envia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendPost();
            }
        });
        Desa.setEnabled(false);
        Desa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desa();
            }
        });
        Pendents.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).pendents();
            }
        });
        Mapa.setEnabled(false);
        Mapa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapa();
            }
        });

        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putSerializable(EXTRA_FILENAME, output);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i("ONRESUME",String.valueOf(mRequestingLocationUpdates));
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
        updateLocationUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        Log.i("ONPAUSE","OK");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imatge.setImageBitmap(bitmap);
    }

    //
    // LOCALITZACIÓ
    //

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.i("onActivityResult", "User agreed to make required location settings changes.");
                        mRequestingLocationUpdates=true;
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("onActivityResult", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                updateLocationUI();
                Log.i("onActivityResult",String.valueOf(mRequestingLocationUpdates));
                break;
            case CONTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    angle_foto=0;
                    setPic();
                    galleryAddPic();
                    Girar.setEnabled(true);
                    Envia.setEnabled(true);
                    Desa.setEnabled(true);
                    Log.i("onActivityResult","Foto");
                }
                break;
        }
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "All location settings are satisfied.");
                            //noinspection MissingPermission
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                            mRequestingLocationUpdates=true;
                            updateLocationUI();

                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("startLocationUpdates", "Location settings are not satisfied. Attempting to upgrade location settings.");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("startLocationUpdates", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.i("startLocationUpdates", errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            if (!jaLocalitzat) {
                Snackbar.make(getActivity().findViewById(android.R.id.content),"S'ha localitzat la teva ubicació",Snackbar.LENGTH_LONG).show();
                jaLocalitzat=true;
            }
            GPS.setText(mGPSLabel + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
            Foto.setEnabled(true);
            Mapa.setEnabled(true);
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.i("stopLocationUpdates", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i("requestPermissions", "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
});
        } else {
            Log.i("requestPermissions", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("onRequestPermResult", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i("onRequestPermResult", "Permission granted, updates requested, starting location updates");
                }
            } else {
                Log.i("onRequestPermResult","Show Snackbar");
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    //
    // MAPA

    public void mapa() {
        String laUri="geo:"+String.valueOf(mCurrentLocation.getLatitude())+","+ valueOf(mCurrentLocation.getLongitude()+"?z=9");
        Uri gmmIntentUri = Uri.parse(laUri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
        startActivity(mapIntent);
        }
    }

    //
    // FOTOGRAFIA
    //

    private void fesFoto() {
        if (mCurrentLocation != null) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                try {
                    output = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                if (output != null) {
                    Uri outputUri = FileProvider.getUriForFile(getContext(), "com.edumet.observacions", output);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "Una foto", outputUri);
                        i.setClipData(clip);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            getActivity().grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                    try {
                        startActivityForResult(i, CONTENT_REQUEST);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(super.getContext(), R.string.msg_no_camera, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }
            }

        } else {
            Toast.makeText(super.getContext(), R.string.encara_sense_lloc, Toast.LENGTH_LONG).show();
        }
    }
    
    private File createImageFile() throws IOException {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(timeStamp, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createMiniatura(int x) throws IOException {
        String minTimeStamp = String.valueOf(x) + "_"+timeStamp;
        File storageDir = getActivity().getFilesDir();
        File miniatura = File.createTempFile(minTimeStamp, ".jpg", storageDir);
        minPhotoPath = miniatura.getAbsolutePath();
        return miniatura;
    }

    private void fesMiniatura(int x,int angle) {
        setPicTemp(x, x);
        try {
            outputMiniatura = createMiniatura(x);
        } catch (IOException ex) {
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        try {
            FileOutputStream out = new FileOutputStream(outputMiniatura);
            bitmapTemp=rotateViaMatrix(bitmapTemp,angle);
            bitmapTemp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private void fesMiniatures() {
        fesMiniatura(midaIcon,angle_foto); // icona
        pathIcon=outputMiniatura.getAbsolutePath();
        fesMiniatura(midaVista,angle_foto); // vista
        pathVista=outputMiniatura.getAbsolutePath();
        fesMiniatura(midaEnvio,angle_foto); // envio
        pathEnvia =outputMiniatura.getAbsolutePath();
        Log.i("mCurrentPhotoPath",mCurrentPhotoPath);
        Log.i("pathIcon",pathIcon);
        Log.i("pathVista",pathVista);
        Log.i("pathEnvia", pathEnvia);
    }

    private void desaObservacio() {
        fesMiniatures();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
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

    private void setPicTemp(int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmapTemp = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    static private Bitmap rotateViaMatrix(Bitmap original,int angle) {
        Matrix matrix= new Matrix();
        matrix.setRotate(angle);
        return(Bitmap.createBitmap(original, 0, 0, original.getWidth(),
                original.getHeight(), matrix, true));
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

    private int getNumFenomen() {
        int codi_fenomen = 2;
        switch (num_fenomen) {
            case 0:
                codi_fenomen = 2; // Aus--Oreneta
                break;
            case 1:
                codi_fenomen = 3; // Floracions--Ametller
                break;
            case 2:
                codi_fenomen = 4; // Floracions--Cirerer
                break;
            case 3:
                codi_fenomen = 1; // Insectes--Papallona
                break;
        }
        return codi_fenomen;
    }

    private void sendPost() {
        ByteArrayOutputStream baosEnv = new ByteArrayOutputStream();
        setPicTemp(midaEnvio,midaEnvio);
        bitmapTemp=rotateViaMatrix(bitmapTemp,angle_foto);
        Log.i("angle_foto", valueOf(angle_foto));
        bitmapTemp.compress(Bitmap.CompressFormat.JPEG, 100, baosEnv);
        byte[] fotografia = baosEnv.toByteArray();

/*        byte[] fotografia;
        fotografia = new byte[(int) output.length()];
        try {
            InputStream is = new FileInputStream(output);
            is.read(fotografia);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String encodedFoto = Base64.encodeToString(fotografia, Base64.DEFAULT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss");
        String dia = sdf.format(Calendar.getInstance().getTime());
        String hora = shf.format(Calendar.getInstance().getTime());

        final OkHttpClient client = new OkHttpClient();

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("fitxer", encodedFoto);
            jsonParam.put("usuari", 43900018);
            jsonParam.put("dia",dia);
            jsonParam.put("hora",hora);
            jsonParam.put("lat", mCurrentLocation.getLatitude());
            jsonParam.put("lon", mCurrentLocation.getLongitude());
            jsonParam.put("id_feno", getNumFenomen());
            jsonParam.put("descripcio", observacio.getText());
            jsonParam.put("tab", "salvarFenoApp");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("JSON sortida", jsonParam.toString());

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonParam.toString());

        final Request request = new Request.Builder()
                .url("https://edumet.cat/edumet/meteo_proves/dades_recarregar.php")
                //.url("https://edumet.cat/edumet/meteo_2/dades_recarregar_feno.php")
                //.url("http://tecnologia.isantandreu.net/prova.php")
                //.url("https://edumet.cat/edumet/meteo_proves/prova.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "auth")
                .addHeader("cache-control", "no-cache")
                .build();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio,Snackbar.LENGTH_LONG).show();
                                                        //Toast.makeText(getActivity().getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                    }
                                                });
                                                Log.i("CLIENT", getString(R.string.error_connexio));
                                            }
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                                Log.i("RESPONSE", response.toString());
                                                Log.i("CONTENT", response.body().string());
                                                if (response.isSuccessful()) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content),R.string.dades_enviades,Snackbar.LENGTH_LONG).show();
                                                            //Toast.makeText(getActivity().getBaseContext(), R.string.dades_enviades, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.dades_enviades));
                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio,Snackbar.LENGTH_LONG).show();
                                                            //Toast.makeText(getActivity().getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.error_servidor));
                                                }
                                            }
                                        }
        );
    }

    //
    // DESA
    //

    private void desa() {
        desaObservacio();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss");
        String dia = sdf.format(Calendar.getInstance().getTime());
        String hora = shf.format(Calendar.getInstance().getTime());

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DadesEstructura.Parametres.COLUMN_NAME_DIA, dia);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_HORA, hora);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_LATITUD, mCurrentLocation.getLatitude());
        values.put(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD, mCurrentLocation.getLongitude());
        values.put(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN, getNumFenomen());
        values.put(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO, observacio.getText().toString());
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH, mCurrentPhotoPath);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH_ICON, pathIcon);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH_VISTA, pathVista);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA, pathEnvia);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT, 0);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DadesEstructura.Parametres.TABLE_NAME, null, values);
        String strLong = Long.toString(newRowId);
        Log.i("SQL", strLong);
        Snackbar.make(getActivity().findViewById(android.R.id.content),"S'ha desat l'observació",Snackbar.LENGTH_LONG).show();
        //Toast.makeText(getActivity(), "S'ha desat l'observació", Toast.LENGTH_LONG).show();

        // Ara llegim
 /*       db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                DadesEstructura.Parametres._ID,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_LATITUD,
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO,
                DadesEstructura.Parametres.COLUMN_NAME_PATH,
                DadesEstructura.Parametres.COLUMN_NAME_ANGLE,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        // Filter results WHERE "ID" = '1'
        String selection = DadesEstructura.Parametres._ID + " = ?";
        String[] selectionArgs = {"1"};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD+ " DESC";
        Cursor cursor = db.query(
                DadesEstructura.Parametres.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres._ID));
            itemIds.add(itemId);
        }
        cursor.close();
        Toast.makeText(getActivity(), itemIds.get(0).toString(), Toast.LENGTH_LONG).show();*/
    }

    //
    // GENERAL
    //

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            num_fenomen = pos;
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}









