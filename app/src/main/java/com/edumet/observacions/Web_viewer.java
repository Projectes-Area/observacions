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

public class Web_viewer extends AppCompatActivity {

    BottomNavigationView navigation;
    String desti;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_viewer);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.web_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        WebView contenidor = (WebView) findViewById(R.id.web_radar);
        WebSettings webSettings = contenidor.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String html = "";
        desti = getIntent().getStringExtra("desti");
        Log.i(".Destí",desti);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        switch (desti) {
            case "pronostic":
                SharedPreferences sharedPref;
                sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
                int estacioActual = sharedPref.getInt("estacio_actual", 0);
                EstacionsHelper mDbHelper= new EstacionsHelper(this);
                SQLiteDatabase db= mDbHelper.getReadableDatabase();

                String[] projection = {
                        DadesEstacions.Parametres.COLUMN_NAME_CODI,
                };
                String selection = DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET + " = ?";
                String[] selectionArgs = {String.valueOf(estacioActual)};
                String sortOrder = null;

                Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.moveToFirst();

                String codi = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_CODI));
                cursor.close();
                mDbHelper.close();

                html = "<iframe src='http://m.meteo.cat/?codi='"+codi+"' height='100%' width='100%' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='yes' style='font-size:0.8em'></iframe>";
                Log.i(".Html",html);
                navigation.setSelectedItemId(R.id.navigation_pronostic);
                break;
            case "radar":
                html = "<iframe src='https://edumet.cat/edumet/meteo_2/00_radar_mobil.php' style='height: 100%; width: 100%; margin: 0 auto ' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='no'></iframe>";
                navigation.setSelectedItemId(R.id.navigation_radar);
                break;
        }
        contenidor.loadData(html, "text/html", null);
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
                        Log.i(".Cap a","radar");
                        startActivity(intent);
                    }
                    return true;
                case R.id.navigation_pronostic:
                    if (desti.equals("radar")) {
                        intent = new Intent(getApplicationContext(), Web_viewer.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("desti", "pronostic");
                        Log.i(".Cap a","pronostic");
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
}
