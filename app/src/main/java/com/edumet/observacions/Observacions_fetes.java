package com.edumet.observacions;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Observacions_fetes extends Fragment {

    DadesHelper mDbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v=inflater.inflate(R.layout.observacions_fetes, container, false);
        mDbHelper = new DadesHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                DadesEstructura.Parametres._ID,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_LATITUD,
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO,
                DadesEstructura.Parametres.COLUMN_NAME_PATH,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ICON,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_VISTA,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        // Filter results
        String selection = DadesEstructura.Parametres._ID + " > ?";
        String[] selectionArgs = {"0"};

        // How you want the results sorted in the resulting Cursor
        //String sortOrder = DadesEstructura.Parametres.COLUMN_NAME_DIA+ " DESC";
        String sortOrder ="dia DESC, hora DESC";
        Cursor cursor = db.query(
                DadesEstructura.Parametres.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        //List itemIds = new ArrayList<>();
        List itemDies = new ArrayList<>();
        List itemHores = new ArrayList<>();
        //List itemLatituds = new ArrayList<>();
        //List itemLongituds = new ArrayList<>();
        List itemFenomens = new ArrayList<>();
        //List itemDescripcions = new ArrayList<>();
        //List itemPaths = new ArrayList<>();
        List itemPath_icons = new ArrayList<>();
        //List itemPath_vistes = new ArrayList<>();
        //List itemPath_envies = new ArrayList<>();
        List itemEnviats = new ArrayList<>();
        while(cursor.moveToNext()) {
            String itemDia=cursor.getString(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
            itemDies.add(itemDia);
            String itemHora=cursor.getString(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_HORA));
            itemHores.add(itemHora);
            String itemFenomen=cursor.getString(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN));
            itemFenomens.add(itemFenomen);
            String itemPath_icon=cursor.getString(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_ICON));
            itemPath_icons.add(itemPath_icon);
        }
        cursor.close();

        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 0, 0, 0);
        LinearLayout.LayoutParams paramsChk = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsChk.setMargins(0, 0, 20, 0);
        LinearLayout.LayoutParams paramsIcona = new LinearLayout.LayoutParams(200, 200);
        paramsIcona.setMargins(20, 10, 20, 10);

        final LinearLayout lm = (LinearLayout) v.findViewById(R.id.linearLY);

        for(int j=0;j<itemDies.size();j++)
        {
            final int index = j;
            LinearLayout ll = new LinearLayout(getContext());
            ll.setGravity(Gravity.CENTER_VERTICAL);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("TAG", "index :" + index);
                    Toast.makeText(getContext(),"Clicked Button Index :" + index,Toast.LENGTH_LONG).show();


                }
            });

            final ImageView img =new ImageView(getContext());
            img.setId(j+1);
            img.setLayoutParams(paramsIcona);
            //img.setPadding(20,10,20,10);
            //img.setLayoutParams(params);


            //img.setBackgroundColor(0);

            img.setImageBitmap(BitmapFactory.decodeFile(itemPath_icons.get(j).toString()));

            // Set click listener for button
/*            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("TAG", "index :" + index);
                    Toast.makeText(getContext(),"Clicked Button Index :" + index,Toast.LENGTH_LONG).show();

                }
            });*/
            ll.addView(img);

            TextView lblDia = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = format.parse(itemDies.get(j).toString());
                lblDia.setText(dateCatala.format(date.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ll.addView(lblDia);


            TextView lblHora = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date time = format.parse(itemHores.get(j).toString());
                lblHora.setText(horaCatala.format(time.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            lblHora.setPadding(30,0,40,0);
            ll.addView(lblHora);

             TextView lblFenomen = new TextView(getContext());
             lblFenomen.setText(nomFenomen(Integer.parseInt(itemFenomens.get(j).toString())));
            lblFenomen.setLayoutParams(params);
            ll.addView(lblFenomen);

            LinearLayout llCheck = new LinearLayout(getContext());
            llCheck.setOrientation(LinearLayout.HORIZONTAL);
            llCheck.setLayoutParams(paramsChk);
            llCheck.setVerticalGravity(Gravity.CENTER_VERTICAL);
            llCheck.setHorizontalGravity(Gravity.RIGHT);

            ImageView chk=new ImageView(getContext());
            chk.setImageResource(R.drawable.checkbox_off_background);
            chk.setPadding(0,0,20,0);
            llCheck.addView(chk);

            ll.addView(llCheck);

            lm.addView(ll);

            RelativeLayout.LayoutParams lineparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
            View line = new View(getContext());
            line.setLayoutParams(lineparams);
            line.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            lm.addView(line);
        }
        return v;
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public String nomFenomen(int i) {
        Log.i("numFenomen",String.valueOf(i));
        switch (i) {
            case 2:
                return "Oreneta";
            case 3:
                return "Ametller";
            case 4:
                return "Cirerer";
            case 1:
                return "Papallona";
            default:
                return "Genèric";
        }
    }



}
