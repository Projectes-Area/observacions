package com.edumet.observacions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentInfoEstacio extends Fragment {

    TextView temperatura;
    TextView pressio;
    TextView humitat;
    TextView pluja;
    TextView vent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_estacio, container, false);
        temperatura=(TextView) v.findViewById(R.id.lblTemperatura);
        pressio=(TextView) v.findViewById(R.id.lblPressió);
        humitat=(TextView) v.findViewById(R.id.lblHumitat);
        pluja=(TextView) v.findViewById(R.id.lblPluja);
        vent=(TextView) v.findViewById(R.id.lblVent);
        return v;
    }

    public void setValues(String Temperatura,String Pressio,String Humitat,String Pluja,String Vent)   {
        temperatura.setText(Temperatura);
        pressio.setText(Pressio);
        humitat.setText(Humitat);
        pluja.setText(Pluja);
        vent.setText(Vent);
    }
}




