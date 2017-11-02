package com.edumet.observacions;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ObservacionsFetes extends Fragment {

    DadesHelper mDbHelper;

    private ProgressBar mProgressBar;

    private SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v=inflater.inflate(R.layout.observacions_fetes, container, false);

        mProgressBar=(ProgressBar) v.findViewById(R.id.progressBarObservacions);

        mDbHelper = new DadesHelper(getContext());

        db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                DadesEstructura.Parametres._ID,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ICON,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        // Filter results
        String selection = DadesEstructura.Parametres._ID + " > ?";
        String[] selectionArgs = {"0"};
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

        List itemIds = new ArrayList<>();
        List itemDies = new ArrayList<>();
        List itemHores = new ArrayList<>();
        List itemFenomens = new ArrayList<>();
        List itemPath_icons = new ArrayList<>();
        while(cursor.moveToNext()) {
            String itemId=cursor.getString(
                    cursor.getColumnIndexOrThrow(DadesEstructura.Parametres._ID));
            itemIds.add(itemId);
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

        final LinearLayout lm = (LinearLayout) v.findViewById(R.id.linearLY);

        for(int j=0;j<itemDies.size();j++)
        {
            final int index = j;
            LinearLayout ll = new LinearLayout(getContext());
            ll.setGravity(Gravity.CENTER_VERTICAL);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            final int parametreID=Integer.valueOf(itemIds.get(j).toString());

            ll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((MainActivity) getActivity()).fitxa(parametreID);
                    //Toast.makeText(getContext(),"Clicked Button Index :" + index,Toast.LENGTH_LONG).show();
                }
            });
            final ImageView img =new ImageView(getContext());
            img.setId(j+1);
            LinearLayout.LayoutParams paramsIcona = new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60));
            paramsIcona.setMargins(dpToPx(10), dpToPx(10), 0, dpToPx(10));
            img.setLayoutParams(paramsIcona);
            img.setImageBitmap(BitmapFactory.decodeFile(itemPath_icons.get(j).toString()));

            ll.addView(img);

            TextView lblDia = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = format.parse(itemDies.get(j).toString());
                lblDia.setText(dateCatala.format(date.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(dpToPx(10), 0, 0, 0);

            lblDia.setLayoutParams(layoutParams);
            ll.addView(lblDia);

            TextView lblHora = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date time = format.parse(itemHores.get(j).toString());
                lblHora.setText(horaCatala.format(time.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            lblHora.setLayoutParams(layoutParams);
            ll.addView(lblHora);

            TextView lblFenomen = new TextView(getContext());
            lblFenomen.setText(nomFenomen(Integer.parseInt(itemFenomens.get(j).toString())));
            lblFenomen.setLayoutParams(layoutParams);
            ll.addView(lblFenomen);

            LinearLayout llCheck = new LinearLayout(getContext());
            llCheck.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsChk = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
            paramsChk.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            LinearLayout.LayoutParams paramsLLChk = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsLLChk.setMargins(0, 0, 0, 0);
            llCheck.setLayoutParams(paramsLLChk);
            llCheck.setVerticalGravity(Gravity.CENTER_VERTICAL);
            llCheck.setHorizontalGravity(Gravity.RIGHT);

            ImageView chk=new ImageView(getContext());
            chk.setImageResource(R.mipmap.ic_check_off);
            chk.setLayoutParams(paramsChk);
            llCheck.addView(chk);

            ll.addView(llCheck);

            lm.addView(ll);

            View line = new View(getContext());
            RelativeLayout.LayoutParams lineparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
            line.setLayoutParams(lineparams);
            line.setBackgroundColor(getResources().getColor(R.color.edumet));

            lm.addView(line);
        }
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        try {
            sincronitza();
        } catch (Exception e) {
            Log.i("exception", "error");
        }
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public String nomFenomen(int i) {
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
    
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    //
    // SINCRONITZA
    //

    private final OkHttpClient client = new OkHttpClient();

    public void sincronitza() throws Exception {
        Request request = new Request.Builder()
                .url("https://edumet.cat/edumet/meteo_proves/dades_recarregar.php?usuari=43900018&tab=visuFeno")
                .build();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {

            String fila,espai,dia,hora,latitud,longitud,usuari,numFenomen,descripcio,nom_remot,dataStamp;

                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_LONG).show();
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                    }
                                                });
                                                Log.i("CLIENT", getString(R.string.error_connexio));
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                                Log.i("RESPONSE", response.toString());
                                                String resposta = response.body().string().trim();
                                                Log.i("CONTENT", resposta);
                                                try {
                                                    JSONArray jsonArray = new JSONArray(resposta);
                                                    Log.i("length", String.valueOf(jsonArray.length()));

                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONArray JSONobservacio=jsonArray.getJSONArray(i);

                                                        fila=JSONobservacio.getString(0);
                                                        espai=JSONobservacio.getString(1);
                                                        dia=JSONobservacio.getString(2);
                                                        hora=JSONobservacio.getString(3);
                                                        latitud=JSONobservacio.getString(4);
                                                        longitud=JSONobservacio.getString(5);
                                                        usuari=JSONobservacio.getString(6);
                                                        numFenomen=JSONobservacio.getString(7);
                                                        descripcio=JSONobservacio.getString(8);
                                                        nom_remot=JSONobservacio.getString(9);
                                                        dataStamp=JSONobservacio.getString(10);

                                                        Log.i("NOM_REMOT",nom_remot);

                                                        insertaObservacio(fila,espai,dia,hora,latitud,longitud,usuari,numFenomen,descripcio,nom_remot,dataStamp);
                                                    }

                                                } catch (Exception e) {
                                                    Log.i("error", "error");
                                                }

                                                if (response.isSuccessful()) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "S'han rebut les dades", Snackbar.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);


                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.dades_enviades));


                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.error_servidor));
                                                }
                                            }
                                        }
        );
    }

    public void insertaObservacio(String fila,String espai, String dia,String hora,String latitud,String longitud,String usuari,String numFenomen,String descripcio,String nom_remot,String dataStamp)    {

try {
    downloadFileAsync("https://edumet.cat/edumet/meteo_proves/imatges/fenologia/" + nom_remot, nom_remot);
}
catch (Exception e) {
}
}

    public void downloadFileAsync(final String downloadUrl, final String nomFitxer) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                Log.i("BAIXADA FITXER",downloadUrl);
                /*FileOutputStream fos = new FileOutputStream(nomFitxer);
                fos.write(response.body().bytes());
                fos.close();*/
            }
        });
    }


}
