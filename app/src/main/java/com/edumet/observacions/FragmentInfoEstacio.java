package com.edumet.observacions;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentInfoEstacio extends Fragment {

    TextView temperatura;
    TextView max;
    TextView min;
    TextView humitat;
    TextView pressio;
    TextView sunrise;
    TextView sunset;
    TextView pluja;
    TextView vent;
    TextView data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_estacio, container, false);
        temperatura=(TextView) v.findViewById(R.id.lblTemperatura);
        max=(TextView) v.findViewById(R.id.lblMax);
        min=(TextView) v.findViewById(R.id.lblMin);
        humitat=(TextView) v.findViewById(R.id.lblHumitat);
        pressio=(TextView) v.findViewById(R.id.lblPressió);
        sunrise=(TextView) v.findViewById(R.id.lblSunrise);
        sunset=(TextView) v.findViewById(R.id.lblSunset);
        pluja=(TextView) v.findViewById(R.id.lblPluja);
        vent=(TextView) v.findViewById(R.id.lblVent);
        data=(TextView) v.findViewById(R.id.lblData);
        return v;
    }

    public void setValues(String Temperatura, String Max, String Min, String Humitat, String Pressio, String Sunrise, String Sunset, String Pluja, String Vent, String Data, int colorData)   {
        temperatura.setText(Temperatura);
        max.setText(Max);
        min.setText(Min);
        humitat.setText(Humitat);
        pressio.setText(Pressio);
        sunrise.setText(Sunrise);
        sunset.setText(Sunset);
        pluja.setText(Pluja);
        vent.setText(Vent);
        data.setText(Data);
        data.setTextColor(colorData);
    }
}




