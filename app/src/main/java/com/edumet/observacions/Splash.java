package com.edumet.observacions;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.edumet.observacions.Database.Estacions.TABLE_NAME;

public class Splash extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarSplash);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            baixaEstacions();
        }
    }

    //
// PERMÍS LOCALITZACIÓ
//
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.i(".requestPermissions", "Requesting permission");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(".PermResult", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(".PermResult", "Permission granted");
                baixaEstacions();
            } else {
                finish();
            }
        }
    }

    //
// BAIXA ESTACIONS
//
    DataHelper mDbHelper;
    private SQLiteDatabase db;

    public void baixaEstacions() {
        mDbHelper = new DataHelper(this);
        db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Database.Estacions.COLUMN_NAME_ID_EDUMET
        };

        String selection = Database.Estacions._ID + "> ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        int numEstacions=cursor.getCount();
        cursor.close();
        db.close();
        mDbHelper.close();
        Log.i("..numEstacions",String.valueOf(numEstacions));

        if (numEstacions == 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            String laUrl = getResources().getString(R.string.url_servidor);
            Request request = new Request.Builder()
                    .url(laUrl + "?tab=cnjEst&xarxaEst=D")
                    .build();

            final OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    if (response.isSuccessful()) {
                                                        String resposta = response.body().string().trim();
                                                        try {
                                                            JSONArray jsonArray = new JSONArray(resposta);
                                                            mDbHelper = new DataHelper(getApplicationContext());
                                                            db = mDbHelper.getWritableDatabase();
                                                            ContentValues values = new ContentValues();
                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                JSONArray JSONEstacio = jsonArray.getJSONArray(i);

                                                                values.put(Database.Estacions.COLUMN_NAME_ID_EDUMET, JSONEstacio.getString(0));
                                                                values.put(Database.Estacions.COLUMN_NAME_CODI, JSONEstacio.getString(1));
                                                                values.put(Database.Estacions.COLUMN_NAME_NOM, JSONEstacio.getString(2));
                                                                values.put(Database.Estacions.COLUMN_NAME_POBLACIO, JSONEstacio.getString(3));
                                                                values.put(Database.Estacions.COLUMN_NAME_LATITUD, JSONEstacio.getString(4));
                                                                values.put(Database.Estacions.COLUMN_NAME_LONGITUD, JSONEstacio.getString(5));
                                                                values.put(Database.Estacions.COLUMN_NAME_ALTITUD, JSONEstacio.getString(6));
                                                                values.put(Database.Estacions.COLUMN_NAME_SITUACIO, JSONEstacio.getString(7));
                                                                values.put(Database.Estacions.COLUMN_NAME_ESTACIO, JSONEstacio.getString(8));
                                                                values.put(Database.Estacions.COLUMN_NAME_CLIMA, JSONEstacio.getString(9));

                                                                db.insert(TABLE_NAME, null, values);
                                                            }
                                                            db.close();
                                                            mDbHelper.close();
                                                            Log.i("..Noves estacions", String.valueOf(jsonArray.length()));
                                                            Intent intent = new Intent(getApplicationContext(), Estacions.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } catch (Exception e) {
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    Snackbar.make(findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_LONG).show();
                                                                    mProgressBar.setVisibility(ProgressBar.GONE);
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Snackbar.make(findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_LONG).show();
                                                                mProgressBar.setVisibility(ProgressBar.GONE);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
            );
        } else {
            Log.i("..Estacions", "No cal baixar-les");
            Intent intent = new Intent(getApplicationContext(), Estacions.class);
            startActivity(intent);
            finish();
        }
    }
}
