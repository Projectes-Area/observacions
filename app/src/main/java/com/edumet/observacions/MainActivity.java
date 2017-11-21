package com.edumet.observacions;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUD = "com.edumet.observacions.LATITUD";
    public static final String EXTRA_LONGITUD = "com.edumet.observacions.LONGITUD";
    public static final String EXTRA_ID = "com.edumet.observacions.ID";
    public static final String EXTRA_NUMFENOMEN = "com.edumet.observacions.NUMFENOMEN";
    public static final String EXTRA_PATH = "com.edumet.observacions.PATH";

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean jaLocalitzada = false;
    private boolean jaDesada = false;
    private Double latitud;
    private Double longitud;
    private int AppID;

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        AppID = getIntent().getIntExtra(MainActivity.EXTRA_ID, 0);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            SharedPreferences sharedPref = this.getSharedPreferences("com.edumet.observacions", this.MODE_PRIVATE);
            String usuariDesat = sharedPref.getString("usuari", "");

            if (usuariDesat.isEmpty()) {
                Login firstFragment = new Login();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
            } else {
                Captura firstFragment = new Captura();
                Bundle args = new Bundle();
                args.putInt("AppID", AppID);
                firstFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
            }
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_observacions);

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
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getApplicationContext(),Radar.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    //intent = new Intent(getApplicationContext(),Proostic.class);
                    //startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.i(".requestPermissions", "Requesting permission");
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(".FRAGonReqPermResult", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(".FRAGonReqPermResult", "Permission granted, updates requested, starting location updates");

            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Uri uri;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.les_meves_observacions:
                if (jaDesada) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.updateObservacio();
                }
                observacionsFetes();
                return true;
            case R.id.la_meva_ubicacio:
                if (jaLocalitzada) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    intent = new Intent(this, MapsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(latitud));
                    intent.putExtra(MainActivity.EXTRA_LONGITUD, String.valueOf(longitud));
                    intent.putExtra(MainActivity.EXTRA_NUMFENOMEN, "0");
                    startActivity(intent);
                }
                return true;
            case R.id.fotografia:
                if (jaLocalitzada) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.fesFoto();
                }
                return true;
            case R.id.gira_imatge:
                if (jaDesada) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.angle_foto += 90;
                    if (fragment.angle_foto >= 360) {
                        fragment.angle_foto = 0;
                    }
                    fragment.bitmap = fragment.rotateViaMatrix(fragment.bitmap, 90);
                    fragment.imatge.setImageBitmap(fragment.bitmap);
                }
                return true;
            //           case R.id.envia_observacio:
            //             if (jaDesada) {
/*                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.updateObservacio();
                    fragment.sendPost();*/
            //             }
            //       return true;
            case R.id.mostra_imatge:
                if (jaDesada) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.veure_foto();
                }
                return true;
/*            case R.id.sincronitza:
                FragmentManager fm2 = getSupportFragmentManager();
                ObservacionsFetes fragment2 = (ObservacionsFetes) fm2.findFragmentById(R.id.fragment_container);
                try {
                    fragment2.sincronitza();
                } catch (Exception e) {
                    Log.i(".Exception", "error");
                }
                return true;*/
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Captura targetFragment = new Captura();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        targetFragment.onActivityResult(requestCode, resultCode, data);
        transaction.commit();
    }

    public void ubicacio(double lat, double lon) {
        jaLocalitzada = true;
        latitud = lat;
        longitud = lon;
        Log.i(".ACT-ubicació", String.valueOf(lat) + "," + String.valueOf(lon));
    }

    public void hihaFoto() {
        jaDesada = true;
    }

    public void redrawObservacionsFetes(int numNoves) {
        Captura fragmentA = new Captura();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ObservacionsFetes newFragment = new ObservacionsFetes();
        Bundle args = new Bundle();
        args.putBoolean("actualitzar", false);
        args.putInt("noves", numNoves);
        newFragment.setArguments(args);
        fragmentTransaction.replace(R.id.fragment_container, newFragment);
        fragmentManager.popBackStack();
        fragmentTransaction.addToBackStack(fragmentA.getClass().getName());
        fragmentTransaction.commit();
    }

    public void observacionsFetes() {
        ObservacionsFetes newFragment = new ObservacionsFetes();
        Bundle args = new Bundle();
        args.putBoolean("actualitzar", true);
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void captura() {
        Captura newFragment = new Captura();
        Bundle args = new Bundle();
        args.putInt("AppID", 0);
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();
    }

    public int desaObservacio(String dia, String hora,
                              Double latitud, Double longitud,
                              int num_fenomen, String observacio,
                              String path, String pathEnvia) {
        DadesHelper mDbHelper;
        mDbHelper = new DadesHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET, 0);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_DIA, dia);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_HORA, hora);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_LATITUD, String.valueOf(latitud));
        values.put(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD, String.valueOf(longitud));
        values.put(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN, num_fenomen);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO, observacio);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH, path);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA, pathEnvia);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT, 0);

        long newRowId = db.insert(DadesEstructura.Parametres.TABLE_NAME, null, values);
        mDbHelper.close();
        Log.i(".AppID", String.valueOf(newRowId));
        return (int) newRowId;
    }

    public static void enviaObservacio(final int AppID, String encodedFoto,
                                       String usuari, final String dia, final String hora,
                                       final Double latitud, final Double longitud,
                                       final int num_fenomen, final String observacio,
                                       final Context context) {

        final OkHttpClient client = new OkHttpClient();

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("fitxer", encodedFoto);
            jsonParam.put("usuari", usuari);
            jsonParam.put("dia", dia);
            jsonParam.put("hora", hora);
            jsonParam.put("lat", latitud);
            jsonParam.put("lon", longitud);
            jsonParam.put("id_feno", num_fenomen);
            jsonParam.put("descripcio", observacio);
            jsonParam.put("tab", "salvarFenoApp");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonParam.toString());

        String laUrl = context.getResources().getString(R.string.url_servidor);

        final Request request = new Request.Builder()
                .url(laUrl)
                //.url("https://edumet.cat/edumet/meteo_2/dades_recarregar_feno.php")
                //.url("http://tecnologia.isantandreu.net/prova.php")
                //.url("https://edumet.cat/edumet/meteo_proves/prova.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "auth")
                .addHeader("cache-control", "no-cache")
                .build();

        final View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        final ProgressBar mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        final ImageButton Envia = (ImageButton) rootView.findViewById(R.id.btnEnvia);
        final Handler mHandler = new Handler(context.getMainLooper());
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        Envia.setEnabled(false);
        Envia.setImageResource(R.mipmap.ic_send_white);

        client.newCall(request).enqueue(new Callback() {

                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Snackbar.make(rootView, R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                        Envia.setEnabled(true);
                                                        Envia.setImageResource(R.mipmap.ic_send_edumet);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String numResposta = response.body().string().trim();
                                                    Snackbar.make(rootView, R.string.dades_enviades, Snackbar.LENGTH_SHORT).show();
                                                    int nouEdumetID = Integer.valueOf(numResposta);
                                                    updateID(AppID, nouEdumetID, context);

                                                    mHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                } else {
                                                    Snackbar.make(rootView.findViewById(android.R.id.content), R.string.error_servidor, Snackbar.LENGTH_SHORT).show();
                                                    mHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                            Envia.setEnabled(true);
                                                            Envia.setImageResource(R.mipmap.ic_send_edumet);
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }

    public static void updateID(int AppID, int EdumetID, Context context) {
        String unlog = String.valueOf(AppID) + "-" + String.valueOf(EdumetID);
        Log.i(".updateID", unlog);
        DadesHelper mDbHelper;
        mDbHelper = new DadesHelper(context);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT, 1);
        values.put(DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET, EdumetID);

        String selection = DadesEstructura.Parametres._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(AppID)};

        int count = db.update(DadesEstructura.Parametres.TABLE_NAME, values, selection, selectionArgs);
        mDbHelper.close();
        Captura frag1 = new Captura();
        frag1.setEnviada();
        FragmentFitxa frag2 = new FragmentFitxa();
        frag2.setEnviada();
    }
}