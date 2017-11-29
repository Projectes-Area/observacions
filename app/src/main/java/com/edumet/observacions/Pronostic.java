package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Pronostic extends AppCompatActivity {
    BottomNavigationView navigation;
    WebView contenidor;
    ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pronostic);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.pronostic_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        contenidor = (WebView) findViewById(R.id.web_pronostic);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarPronostic);

        WebSettings webSettings = contenidor.getSettings();
        webSettings.setJavaScriptEnabled(true);

        contenidor.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                Snackbar.make(findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(contenidor,true);
        }
        CookieManager.getInstance().setAcceptCookie(true);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_pronostic);
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPref;
        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        int ID_Edumet = sharedPref.getInt("estacio_actual", 0);
        Log.i(".estacioActual", String.valueOf(ID_Edumet));
        try {
            baixa_cINE_Edumet(ID_Edumet);
        } catch (Exception e) {
            e.printStackTrace();
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
                    intent = new Intent(getApplicationContext(), Radar.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    return true;
            }
            return false;
        }
    };

    public void baixa_cINE_Edumet(int Edumet_ID) throws Exception {

        DataHelper mDbHelper;
        SQLiteDatabase db;
        mDbHelper = new DataHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] projection = {Database.Estacions.COLUMN_NAME_CODI};

        String selection = Database.Estacions.COLUMN_NAME_ID_EDUMET + " = ?";
        String[] selectionArgs = {String.valueOf(Edumet_ID)};
        String sortOrder = null;

        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();
        String codEst_Edumet = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_CODI));
        cursor.close();
        mDbHelper.close();

        final OkHttpClient client = new OkHttpClient();

        String laUrl = getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                .url(laUrl + "?tab=mobil&codEst=" + codEst_Edumet)
                .build();

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                        Snackbar.make(findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String resposta = response.body().string().trim();
                                                    try {
                                                        JSONArray jsonArray = new JSONArray(resposta);
                                                        JSONArray JSONEstacio = jsonArray.getJSONArray(0);
                                                        final String INE = JSONEstacio.getString(20);
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                String html = "<iframe src='http://m.meteo.cat/?codi=" + INE + "' height='100%' width='100%' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='yes' style='font-size:0.8em'></iframe>";
                                                                contenidor.loadData(html, "text/html", null);
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        Log.i(".jsonerror", "jsonerror");
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                String html = "<iframe src='http://m.meteo.cat/?codi=080193' height='100%' width='100%' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='yes' style='font-size:0.8em'></iframe>";
                                                                contenidor.loadData(html, "text/html", null);
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    Log.i("notsuccessful", "notsuccessful");
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                            Snackbar.make(findViewById(android.R.id.content), R.string.error_estacio_no_dades, Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }
}
