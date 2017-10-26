package com.edumet.observacions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Observacions_fetes extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v=inflater.inflate(R.layout.observacions_fetes, container, false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss");
        String dia = sdf.format(Calendar.getInstance().getTime());
        String hora = shf.format(Calendar.getInstance().getTime());
        //setContentView(R.layout.dynamically_create_view_element);

        final LinearLayout lm = (LinearLayout) v.findViewById(R.id.linearMain);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for(int j=0;j<=6;j++)
        {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);

            ImageView chk=new ImageView(getContext());
            chk.setImageResource(R.drawable.checkbox_on_background);
            ll.addView(chk);

            final ImageButton btn =new ImageButton(getContext());
            btn.setId(j+1);
            btn.setLayoutParams(params);

            final int index = j;
            // Set click listener for button
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("TAG", "index :" + index);
                    Toast.makeText(getContext(),
                            "Clicked Button Index :" + index,Toast.LENGTH_LONG).show();

                }
            });
            ll.addView(btn);

            TextView lblDia = new TextView(getContext());
            //product.setText(" Product"+j+"    ");
            lblDia.setText(dia);
            lblDia.setPadding(40,0,0,0);
            lblDia.setLayoutParams(params);
            ll.addView(lblDia);


            TextView lblHora = new TextView(getContext());
            lblHora.setText(hora);

            lblHora.setPadding(40,0,40,0);
            lblHora.setLayoutParams(params);
            ll.addView(lblHora);

             TextView lblFenomen = new TextView(getContext());
             lblFenomen.setText("Orenetes");
            //price.setText("  $"+j+"     ");
            lblFenomen.setLayoutParams(params);
            ll.addView(lblFenomen);

            lm.addView(ll);

            RelativeLayout.LayoutParams lineparams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, 1);
            View line = new View(getContext());
            //lineparams.addRule(RelativeLayout.BELOW, 1);//specify the id of the button to add the line below the button
            line.setLayoutParams(lineparams);
            line.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            lm.addView(line);
        }
        return v;
    }
}
