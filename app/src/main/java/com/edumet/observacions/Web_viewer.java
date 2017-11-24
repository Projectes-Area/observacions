package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Web_viewer extends AppCompatActivity {

    BottomNavigationView navigation;
    String desti;
    WebView contenidor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_viewer);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.web_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        contenidor = (WebView) findViewById(R.id.web_radar);
        WebSettings webSettings = contenidor.getSettings();
        webSettings.setJavaScriptEnabled(true);

        desti = getIntent().getStringExtra("desti");
        Log.i(".Destí", desti);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

    }

    @Override
    public void onStart() {
        super.onStart();
        switch (desti) {
            case "pronostic":
                SharedPreferences sharedPref;
                sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
                int estacioActual = sharedPref.getInt("estacio_actual", 0);
                Log.i(".estacioActual",String.valueOf(estacioActual));
                try {
                    baixaINE(estacioActual);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                navigation.setSelectedItemId(R.id.navigation_pronostic);
                break;
            case "radar":
                String html = "<iframe src='https://edumet.cat/edumet/meteo_2/00_radar_mobil.php' style='height: 100%; width: 100%; margin: 0 auto ' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='no'></iframe>";
                navigation.setSelectedItemId(R.id.navigation_radar);
                contenidor.loadData(html, "text/html", null);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.observacions_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getApplicationContext(), Estacions.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    if (desti.equals("pronostic")) {
                        intent = new Intent(getApplicationContext(), Web_viewer.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("desti", "radar");
                        Log.i(".Cap a", "radar");
                        startActivity(intent);
                    }
                    return true;
                case R.id.navigation_pronostic:
                    if (desti.equals("radar")) {
                        intent = new Intent(getApplicationContext(), Web_viewer.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("desti", "pronostic");
                        Log.i(".Cap a", "pronostic");
                        startActivity(intent);
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        switch (desti) {
            case "pronostic":
                navigation.setSelectedItemId(R.id.navigation_pronostic);
                break;
            case "radar":
                navigation.setSelectedItemId(R.id.navigation_radar);
                break;
        }
    }

    private final OkHttpClient client = new OkHttpClient();

    String INE;

    public void baixaINE(int Edumet_ID) throws Exception {

        EstacionsHelper mDbHelper;
        SQLiteDatabase db;
        mDbHelper = new EstacionsHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] projection = {
                DadesEstacions.Parametres.COLUMN_NAME_CODI,
        };

        String selection = DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET + " = ?";
        String[] selectionArgs = {String.valueOf(Edumet_ID)};
        String sortOrder = null;

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();

        String codi = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_CODI));
        cursor.close();

        Log.i(".Codi",codi);

        String laUrl = getResources().getString(R.string.url_servidor);

        Request request = new Request.Builder()
                .url(laUrl + "?tab=mobil&codEst=" + codi)
                .build();

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                                Log.i(".failure","failure");
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String resposta = response.body().string().trim();
                                                    try {
                                                        Log.i(".resposta",resposta);
                                                        JSONArray jsonArray = new JSONArray(resposta);
                                                        JSONArray JSONEstacio = jsonArray.getJSONArray(0);

                                                        INE = JSONEstacio.getString(20);
                                                        Log.i(".INE_Estació", INE);
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                String html = "<iframe src='http://m.meteo.cat/?codi=" + INE + "' height='490px' width='100%' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='1' scrolling='yes' style='font-size:0.8em'></iframe>";
                                                                Log.i(".Html", html);
                                                                contenidor.loadData(html, "text/html", null);
                                                            }
                                                        });

                                                    } catch (Exception e) {
                                                        Log.i(".jsonerror","jsonerror");
                                                        e.printStackTrace();
                                                    }
                                                }
                                                else {
                                                    Log.i("notsuccessful","notsuccessful");
                                                }
                                            }
                                        }
        );
    }

}
