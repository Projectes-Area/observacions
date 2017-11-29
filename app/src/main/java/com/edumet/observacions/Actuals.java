package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class Actuals extends AppCompatActivity {

    SharedPreferences sharedPref;
    private int id_edumet;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actuals);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.actuals_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);
        navigation.setSelectedItemId(R.id.navigation_pronostic);

        sharedPref = getSharedPreferences("com.edumet.observacions", MODE_PRIVATE);
        id_edumet = sharedPref.getInt("estacio_actual", 0);
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
                    intent = new Intent(getApplicationContext(), Pronostic.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.observacions_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

