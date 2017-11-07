package com.edumet.observacions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Visor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visor);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.visor_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        WebView webView = (WebView) findViewById(R.id.elWebView);
        final String html;
       /* if (intent.getStringExtra(MainActivity.EXTRA_PATH)=="edumet_web") {
            //html="http://edumet.cat/edumet/meteo_2/mobil.php";
            html="http://www.google.es/";
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(html);
        }
        else {*/
        String imagePath = "file://" + intent.getStringExtra(MainActivity.EXTRA_PATH);
        html = "<html><head></head><body style=\"margin: 0; padding: 0\"> <img src=\"" + imagePath + "\"> </body></html>";
        webView.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
/*        }
    }*/
    }
        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;

                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);

            }
        }

        @Override
        public void onBackPressed () {
            super.onBackPressed();
        }

    }





