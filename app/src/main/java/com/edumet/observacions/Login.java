package com.edumet.observacions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by 40980055N on 19/10/17.
 */

public class Login extends Fragment {

    private Button LoginOK;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.login, container, false);
        LoginOK=(Button) v.findViewById(R.id.btnLogin);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        LoginOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).captura();
            }
        });
    }
}