package com.edumet.observacions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private boolean jaLocalitzat = false;
    private boolean jaHiHaFoto = false;
    private Double latitud;
    private Double longitud;

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.les_meves_observacions:
                observacionsFetes();
                return true;
            case R.id.la_meva_ubicacio:
                if (jaLocalitzat) {
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(latitud));
                    intent.putExtra(MainActivity.EXTRA_LONGITUD, String.valueOf(longitud));
                    startActivity(intent);
                }
                return true;
            case R.id.fotografia:
                if (jaLocalitzat) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.fesFoto();
                }
                return true;
            case R.id.gira_imatge:
                if (jaHiHaFoto) {
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
            case R.id.envia_observacio:
                if (jaHiHaFoto) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.desa();
                    fragment.sendPost();
                }
                return true;
            case R.id.desa_observacio:
                if (jaHiHaFoto) {
                    FragmentManager fm = getSupportFragmentManager();
                    Captura fragment = (Captura) fm.findFragmentById(R.id.fragment_container);
                    fragment.desa();
                }
                return true;
            case R.id.edumet_web:
                Uri uri = Uri.parse("https://edumet.cat/edumet/meteo_2/index.php");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intent2 = new Intent();
                intent2.setAction(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("ACT", "OnRequest");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Captura targetFragment = new Captura();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        targetFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("ACT", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        Captura targetFragment = new Captura();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        targetFragment.onActivityResult(requestCode, resultCode, data);
        transaction.commit();
    }

    public void ubicacio(double lat, double lon) {
        jaLocalitzat = true;
        latitud = lat;
        longitud = lon;
        Log.i("ACT-Ubicació", String.valueOf(lat) + "," + String.valueOf(lon));
    }

    public void hihaFoto() {
        jaHiHaFoto = true;
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
        Log.i("AppID", String.valueOf(newRowId));
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

        final View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        final ProgressBar mProgressBar= (ProgressBar) rootView.findViewById(R.id.progress_bar);
        final Handler mHandler = new Handler(context.getMainLooper());
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {

                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Snackbar.make(rootView, R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String numResposta = response.body().string().trim();
                                                    Snackbar.make(rootView, R.string.dades_enviades, Snackbar.LENGTH_SHORT).show();
                                                    int nouEdumetID = Integer.valueOf(numResposta);
                                                    Log.i("Edumet_ID", numResposta);
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
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }

    public static void updateID(int AppID, int EdumetID, Context context) {
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
        Log.i("UpdatedRows", String.valueOf(count));
    }
}