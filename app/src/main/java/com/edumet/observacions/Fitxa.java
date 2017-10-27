package com.edumet.observacions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 40980055N on 27/10/17.
 */

public class Fitxa extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fitxa, container, false);
        return v;
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
    }
}
