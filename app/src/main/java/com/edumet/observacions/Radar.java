package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Radar extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radar);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.web_toolbar);
        setSupportActionBar(myToolbar);

        WebView radar = (WebView) findViewById(R.id.web_radar);
        WebSettings webSettings = radar.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //String html="<html><body>You scored <b>192</b> points.</body></html>";

        //String html = "<iframe width=\"450\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/31592/charts/1?width=450&height=260&results=60&dynamic=true\" ></iframe>";

        //String html="<iframe src='https://edumet.cat/edumet/meteo_2/00_radar_mobil.php' style='height: 75vh; width: 100%; margin: 0 auto ' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='1' scrolling='no'></iframe>";

        String html="<iframe src='https://edumet.cat/edumet/meteo_2/00_radar_mobil.php' style='height: 100%; width: 100%; margin: 0 auto ' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='no'></iframe>";

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_radar);

        radar.loadData(html, "text/html", null);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getApplicationContext(),Estacions.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    return true;
                case R.id.navigation_pronostic:
                    //intent = new Intent(getApplicationContext(),Proostic.class);
                    //startActivity(intent);
                    return true;
            }
            return false;
        }
    };


}
