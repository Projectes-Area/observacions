package com.edumet.observacions;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    private int numNovesEstacions;

    private TextView poblacio;
    private TextView latitud;
    private TextView longitud;
    private TextView altitud;
    private ImageView foto;
    private Spinner spinner;

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
        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        while (cursor.moveToNext()) {
            String itemIdEdumet = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET));
            itemIdsEdumet.add(itemIdEdumet);
        }
        cursor.close();

        poblacio = (TextView) v.findViewById(R.id.lblPoblacio);
        latitud = (TextView) v.findViewById(R.id.lblLatitud);
        longitud = (TextView) v.findViewById(R.id.lblLongitud);
        altitud = (TextView) v.findViewById(R.id.lblAltitud);
        foto = (ImageView) v.findViewById(R.id.imgFoto);

        spinner = (Spinner) v.findViewById(R.id.spinnerEstacions);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarEstacions);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        try {
            sincronitza();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

                                                        numNovesEstacions = 0;
                                                        int numNovaEstacio = 0;

                                                        Boolean flagRepetida;

                                                        db = mDbHelper.getWritableDatabase();

                                                        ContentValues values = new ContentValues();

                                                        for (int i = 0; i < jsonArray.length(); i++) {

                                                            JSONArray JSONEstacio = jsonArray.getJSONArray(i);
                                                            flagRepetida = false;
                                                            for (int j = 0; j < itemIdsEdumet.size(); j++) {
                                                                int Num1 = Integer.valueOf(itemIdsEdumet.get(j).toString());
                                                                int Num2 = Integer.valueOf(JSONEstacio.getString(0).toString());
                                                                if (Num1 == Num2) {
                                                                    flagRepetida = true;
                                                                }
                                                            }
                                                            if (!flagRepetida) {
                                                                numNovaEstacio++;

                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET, JSONEstacio.getString(0));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_CODI, JSONEstacio.getString(1));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_NOM, JSONEstacio.getString(2));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_POBLACIO, JSONEstacio.getString(3));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_LATITUD, JSONEstacio.getString(4));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD, JSONEstacio.getString(5));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ALTITUD, JSONEstacio.getString(6));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_SITUACIO, JSONEstacio.getString(7));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_ESTACIO, JSONEstacio.getString(8));
                                                                values.put(DadesEstacions.Parametres.COLUMN_NAME_CLIMA, JSONEstacio.getString(9));

                                                                long newRowId = db.insert(DadesEstacions.Parametres.TABLE_NAME, null, values);
                                                                Log.i(".Estacio_ID", String.valueOf(newRowId));
                                                            }
                                                        }
                                                        numNovesEstacions = numNovaEstacio;
                                                        Log.i(".NovesEst", String.valueOf(numNovesEstacions));
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                try {
                                                                    ompleSpinner();
                                                                    ((Estacions) getActivity()).obreMapa();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                        mProgressBar.setVisibility(ProgressBar.GONE);

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_LONG).show();
                                                                mProgressBar.setVisibility(ProgressBar.GONE);
                                                            }
                                                        });
                                                    }

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

    public void mostraEstacio(int EstacioID) {
        String[] projection = {
                //DadesEstacions.Parametres._ID,
                DadesEstacions.Parametres.COLUMN_NAME_CODI,
                DadesEstacions.Parametres.COLUMN_NAME_POBLACIO,
                DadesEstacions.Parametres.COLUMN_NAME_LATITUD,
                DadesEstacions.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstacions.Parametres.COLUMN_NAME_ALTITUD,
        };

        String selection = DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET + " = ?";
        String[] selectionArgs = {String.valueOf(EstacioID)};
        String sortOrder = null;

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();

        String codi = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_CODI));
        poblacio.setText(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_POBLACIO)));
        latitud.setText("Latitud: " + String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LATITUD))));
        longitud.setText("Longitud: " + String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_LONGITUD))));
        altitud.setText("Altitud: " + String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ALTITUD))) + " metres");
        cursor.close();

        String laUrl = "http://edumet.cat/edumet-data/" + codi + "/estacio/profile1/imatges/fotocentre.jpg";
        Log.i(".laUrl", laUrl);
        Request request = new Request.Builder()
                .url(laUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        foto.setImageResource(R.drawable.edumet);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    InputStream inputStream = response.body().byteStream();
                                                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            foto.setImageBitmap(bitmap);
                                                        }
                                                    });
                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            foto.setImageResource(R.drawable.edumet);
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }

    public void ompleSpinner() {
        String[] projection = {
                DadesEstacions.Parametres._ID,
                DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstacions.Parametres.COLUMN_NAME_NOM
        };
        String selection = DadesEstacions.Parametres._ID + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        Cursor cursor = db.query(DadesEstacions.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);                               // The sort order

        List<String> noms = new ArrayList<String>();
        final List IDsEdumet = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemIdNom = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_NOM));
            noms.add(itemIdNom);
            int ID_Edumet = cursor.getInt(cursor.getColumnIndexOrThrow(DadesEstacions.Parametres.COLUMN_NAME_ID_EDUMET));
            IDsEdumet.add(ID_Edumet);
        }
        cursor.close();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, noms);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mostraEstacio(Integer.valueOf(IDsEdumet.get(position).toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void clicaSpinner(int posicio) {
        spinner.setSelection(posicio);
    }

}


