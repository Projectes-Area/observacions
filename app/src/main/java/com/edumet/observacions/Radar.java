package com.edumet.observacions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class Radar extends AppCompatActivity {

    BottomNavigationView navigation;
    WebView contenidor;
    ProgressBar mProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radar);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.radar_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarRadar);

        contenidor = (WebView) findViewById(R.id.web_radar);
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

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_radar);
    }

    @Override
    public void onStart() {
        super.onStart();
        String html = "<iframe src='https://edumet.cat/edumet/meteo_proves/00_radar_mobil.php' style='height: 100%; width: 100%; margin: 0 auto ' hspace='0' marginheight='0' marginwidth='0' vspace='0' frameborder='0' scrolling='no'></iframe>";
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
                    return true;
                case R.id.navigation_pronostic:
                    intent = new Intent(getApplicationContext(), Pronostic.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };
}
