package com.edumet.observacions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class VeureFoto extends AppCompatActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        String elPath =  intent.getStringExtra(MainActivity.EXTRA_PATH);
        setContentView(new Zoom(this,elPath));
    }
}
