package com.edumet.observacions;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FragmentEstacions extends Fragment {

    EstacionsHelper mDbHelper;
    private SQLiteDatabase db;

    private ProgressBar mProgressBar;

    private final OkHttpClient client = new OkHttpClient();

    private int numNovesObservacions;
    private int numObservacionsBaixades;

    List itemIdsEdumet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_estacions, container, false);

        String[] projection = {
                DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET,
        };

        String selection = DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = "id_edumet ASC";

        mDbHelper = new EstacionsHelper(getContext());
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

        while (cursor.moveToNext()) {
            String itemIdEdumet = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET));
            itemIdsEdumet.add(itemIdEdumet);
        }
        cursor.close();
        //db.close();

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarEstacions);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        try {
            sincronitza();
        } catch (Exception e) {
            Log.i(".Exception", "error");
        }
    }

/*    List id_edumet = new ArrayList<>();
    List codi = new ArrayList<>();
    List nom = new ArrayList<>();
    List poblacio = new ArrayList<>();
    List latitud = new ArrayList<>();
    List longitud = new ArrayList<>();
    List altitud = new ArrayList<>();
    List situacio = new ArrayList<>();
    List estacio = new ArrayList<>();
    List clima = new ArrayList<>();*/

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public void sincronitza() throws Exception {
        String laUrl = getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                .url(laUrl + "?tab=cnjEst")
                .build();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {

                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_LONG).show();
                                                        mProgressBar.setVisibility(ProgressBar.GONE);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String resposta = response.body().string().trim();
                                                    try {
                                                        JSONArray jsonArray = new JSONArray(resposta);

                                                        numNovesObservacions = 0;
                                                        numObservacionsBaixades = 0;
                                                        int numNovaObservacio = 0;

                                                        Boolean flagRepetida;

                                                        db = mDbHelper.getWritableDatabase();

                                                        ContentValues values = new ContentValues();

                                                        for (int i = 0; i < jsonArray.length(); i++) {

                                                            JSONArray JSONobservacio = jsonArray.getJSONArray(i);
                                                            flagRepetida = false;
                                                            for (int j = 0; j < itemIdsEdumet.size(); j++) {
                                                                int Num1=Integer.valueOf(itemIdsEdumet.get(j).toString());
                                                                int Num2=Integer.valueOf(JSONobservacio.getString(0).toString());
                                                                if (Num1 == Num2) {
                                                                    flagRepetida = true;
                                                                }
                                                            }
                                                            if (!flagRepetida) {
                                                                numNovaObservacio++;
/*                                                                id_edumet.add(JSONobservacio.getString(0));
                                                                codi.add(JSONobservacio.getString(1));
                                                                nom.add(JSONobservacio.getString(2));
                                                                poblacio.add(JSONobservacio.getString(3));
                                                                latitud.add(JSONobservacio.getString(4));
                                                                longitud.add(JSONobservacio.getString(5));
                                                                altitud.add(JSONobservacio.getString(6));
                                                                situacio.add(JSONobservacio.getString(7));
                                                                estacio.add(JSONobservacio.getString(8));
                                                                clima.add(JSONobservacio.getString(9));*/

                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET, JSONobservacio.getString(0));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_CODI, JSONobservacio.getString(1));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_NOM, JSONobservacio.getString(2));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_POBLACIO,JSONobservacio.getString(3));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_LATITUD, JSONobservacio.getString(4));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD, JSONobservacio.getString(5));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ALTITUD, JSONobservacio.getString(6));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_SITUACIO, JSONobservacio.getString(7));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ESTACIO, JSONobservacio.getString(8));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_CLIMA, JSONobservacio.getString(9));

                                                                long newRowId = db.insert(DadesEstacions.Parametres.TABLE_NAME, null, values);
                                                                //mDbHelper.close();
                                                                Log.i(".Estacio_ID", String.valueOf(newRowId));/*

                                                                try {
                                                                    downloadFileAsync(numNovaObservacio, "https://edumet.cat/edumet/meteo_proves/imatges/fenologia/" + JSONobservacio.getString(9), nomFitxer);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }*/
                                                            }
                                                        }
                                                        numNovesObservacions = numNovaObservacio;
                                                        Log.i(".NovesEst", String.valueOf(numNovesObservacions));

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            //Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }


}
