package com.edumet.observacions;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class MainActivity extends FragmentActivity {

    public static final String EXTRA_MESSAGE = "com.edumet.observacions.MESSAGE";
    public static final String EXTRA_LATITUD = "com.edumet.observacions.LATITUD";
    public static final String EXTRA_LONGITUD = "com.edumet.observacions.LONGITUD";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }
            Login firstFragment = new Login();
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("ACT","ActivityResult");
            super.onActivityResult(requestCode, resultCode, data);
        Captura targetFragment = new Captura();
        //Bundle args = new Bundle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        targetFragment.onActivityResult(requestCode, resultCode, data);
        transaction.commit();
        }

    public void pendents() {
        ObservacionsFetes newFragment = new ObservacionsFetes();
        Bundle args = new Bundle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void captura() {
        Captura newFragment = new Captura();
        Bundle args = new Bundle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void fitxa(int ID) {
        Fitxa newFragment = new Fitxa();
        Bundle args = new Bundle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        args.putInt("numID",ID);
        newFragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.commit();
    }

/*    public void fragment_mapa() {
        MapFragment newFragment = new MapFragment();
        Bundle args = new Bundle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container_current,newFragment);
        //transaction.replace(R.id.fragment_container, new MapFragment());
        //args.putInt("numID",ID);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new MapFragment())
                .commit();

        newFragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.commit();
    }*/
   }