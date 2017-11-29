package com.edumet.observacions;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FragmentEstacions extends Fragment {

    DataHelper mDbHelper;
    private SQLiteDatabase db;

    private ProgressBar mProgressBar;

    private final OkHttpClient client = new OkHttpClient();

    private int numNovesEstacions;

    private TextView poblacio;
    private TextView latitud;
    private TextView longitud;
    private TextView altitud;
    private ImageView foto;
    private ImageView estrella;
    private Spinner spinner;

    private int ID_Edumet_actual;

    List itemIdsEdumet = new ArrayList<>();
    List valorsEstacio;

    SimpleDateFormat diaCatala = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm", Locale.US);
    Date date;

    SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_estacions, container, false);
        setHasOptionsMenu(true);

        String[] projection = {
                Database.Estacions.COLUMN_NAME_ID_EDUMET,
        };

        String selection = Database.Estacions.COLUMN_NAME_ID_EDUMET + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = "id_edumet ASC";

        mDbHelper = new DataHelper(getContext());
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        while (cursor.moveToNext()) {
            String itemIdEdumet = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ID_EDUMET));
            itemIdsEdumet.add(itemIdEdumet);
        }
        cursor.close();

        poblacio = (TextView) v.findViewById(R.id.lblPoblacio);
        latitud = (TextView) v.findViewById(R.id.lblLatitud);
        longitud = (TextView) v.findViewById(R.id.lblLongitud);
        altitud = (TextView) v.findViewById(R.id.lblAltitud);
        foto = (ImageView) v.findViewById(R.id.imgFoto);
        estrella = (ImageView) v.findViewById(R.id.imgEstrella);

        spinner = (Spinner) v.findViewById(R.id.spinnerEstacions);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarEstacions);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.observacions_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        try {
            sincronitza();
        } catch (Exception e) {
            e.printStackTrace();
        }

        estrella.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("estacio_preferida", ID_Edumet_actual);
                editor.apply();
                estrella.setImageResource(R.mipmap.ic_star_on);
            }
        });
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public void sincronitza() throws Exception {
        String laUrl = getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                //.url(laUrl + "?tab=cnjEst&xarxaEst=meteoCat")
                .url(laUrl + "?tab=cnjEst")
                .build();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
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
                                                                values.put(Database.Estacions.COLUMN_NAME_ID_EDUMET, JSONEstacio.getString(0));
                                                                values.put(Database.Estacions.COLUMN_NAME_CODI, JSONEstacio.getString(1));
                                                                values.put(Database.Estacions.COLUMN_NAME_NOM, JSONEstacio.getString(2));
                                                                values.put(Database.Estacions.COLUMN_NAME_POBLACIO, JSONEstacio.getString(3));
                                                                values.put(Database.Estacions.COLUMN_NAME_LATITUD, JSONEstacio.getString(4));
                                                                values.put(Database.Estacions.COLUMN_NAME_LONGITUD, JSONEstacio.getString(5));
                                                                values.put(Database.Estacions.COLUMN_NAME_ALTITUD, JSONEstacio.getString(6));
                                                                values.put(Database.Estacions.COLUMN_NAME_SITUACIO, JSONEstacio.getString(7));
                                                                values.put(Database.Estacions.COLUMN_NAME_ESTACIO, JSONEstacio.getString(8));
                                                                values.put(Database.Estacions.COLUMN_NAME_CLIMA, JSONEstacio.getString(9));

                                                                long newRowId = db.insert(Database.Estacions.TABLE_NAME, null, values);
                                                                Log.i(".Estacio_ID_App", String.valueOf(newRowId));
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
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                mProgressBar.setVisibility(ProgressBar.GONE);
                                                            }
                                                        });

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_SHORT).show();
                                                                mProgressBar.setVisibility(ProgressBar.GONE);
                                                            }
                                                        });
                                                    }

                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_SHORT).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                }
                                            }
                                        }
        );
    }

    public void mostraEstacio(int ID_Edumet) {

        String[] projection = {
                Database.Estacions._ID,
                Database.Estacions.COLUMN_NAME_ID_EDUMET,
                Database.Estacions.COLUMN_NAME_CODI,
                Database.Estacions.COLUMN_NAME_POBLACIO,
                Database.Estacions.COLUMN_NAME_LATITUD,
                Database.Estacions.COLUMN_NAME_LONGITUD,
                Database.Estacions.COLUMN_NAME_ALTITUD,
        };

        String selection = Database.Estacions.COLUMN_NAME_ID_EDUMET + " = ?";
        String[] selectionArgs = {String.valueOf(ID_Edumet)};
        String sortOrder = null;

        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();

        String id_edumet = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ID_EDUMET));
        String codEst_Edumet = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_CODI));
        String strLat = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LATITUD));
        String strLon = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_LONGITUD));
        poblacio.setText(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_POBLACIO)));
        latitud.setText("Latitud: " + strLat);
        longitud.setText("Longitud: " + strLon);
        altitud.setText("Altitud: " + String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ALTITUD))) + " metres");
        cursor.close();

        //mostraInfo(false);
        try {
            baixaValors(codEst_Edumet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
        int estacioPreferida = sharedPref.getInt("estacio_preferida", 0);

        ID_Edumet_actual = Integer.valueOf(id_edumet);

        if (estacioPreferida == ID_Edumet_actual) {
            estrella.setImageResource(R.mipmap.ic_star_on);
        } else {
            estrella.setImageResource(R.mipmap.ic_star_off);
        }

        sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("estacio_actual", ID_Edumet_actual);
        editor.apply();

        ((Estacions) getActivity()).mouCamera(Double.valueOf(strLat), Double.valueOf(strLon));

        String laUrl = "http://edumet.cat/edumet-data/" + codEst_Edumet + "/estacio/profile1/imatges/fotocentre.jpg";
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
                Database.Estacions._ID,
                Database.Estacions.COLUMN_NAME_ID_EDUMET,
                Database.Estacions.COLUMN_NAME_NOM
        };
        String selection = Database.Estacions._ID + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = null;

        Cursor cursor = db.query(Database.Estacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);                               // The sort order

        List<String> noms = new ArrayList<String>();
        final List IDsEdumet = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemIdNom = cursor.getString(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_NOM));
            noms.add(itemIdNom);
            int ID_Edumet = cursor.getInt(cursor.getColumnIndexOrThrow(Database.Estacions.COLUMN_NAME_ID_EDUMET));
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

    public void baixaValors(String codEst_Edumet) throws Exception {
        String laUrl = getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                .url(laUrl + "?tab=mobil&codEst=" + codEst_Edumet)
                .build();

        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        mostraInfo(false);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    String resposta = response.body().string().trim();
                                                    Log.i(".resposta", resposta);
                                                    valorsEstacio = new ArrayList<>();
                                                    try {
                                                        JSONArray jsonArray = new JSONArray(resposta);
                                                        JSONArray JSONEstacio = jsonArray.getJSONArray(0);

                                                        for (int i = 0; i < JSONEstacio.length(); i++) {
                                                            valorsEstacio.add(JSONEstacio.getString(i));
                                                            Log.i(".Valor_Estació", JSONEstacio.getString(i));
                                                        }
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                double pressio = Double.valueOf(valorsEstacio.get(11).toString());
                                                                if (pressio == 0.0) {
                                                                    mostraInfo(false);
                                                                } else {
                                                                    mostraInfo(true);
                                                                }
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                mostraInfo(false);
                                                                }
                                                        });
                                                    }
                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            mostraInfo(false);
                                                                }
                                                    });
                                                }
                                            }
                                        }
        );
    }

    public void mostraInfo(boolean mostra) {
        String Temperatura = "";
        String Max = "";
        String Min = "";
        String Humitat = "";
        String Pressio = "";
        String Sunrise = "";
        String Sunset = "";
        String Pluja = "";
        String Vent = "";
        String dataActualitzacio="";

        if (mostra) {
            Temperatura = valorsEstacio.get(4).toString() + " ºC";
            Max = valorsEstacio.get(5).toString() + " ºC";
            Min = valorsEstacio.get(6).toString() + " ºC";
            Humitat = valorsEstacio.get(10).toString() + " %";
            Pressio = valorsEstacio.get(11).toString() + " HPa";
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
                date = format.parse(valorsEstacio.get(2).toString());
                Sunrise = horaCatala.format(date.getTime());
                date = format.parse(valorsEstacio.get(3).toString());
                Sunset = horaCatala.format(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String dia="";
            String hora="";
            try {
                SimpleDateFormat formatDia = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss", Locale.US);
                date = formatDia.parse(valorsEstacio.get(0).toString());
                dia = diaCatala.format(date.getTime());
                date = formatHora.parse(valorsEstacio.get(1).toString());
                hora = horaCatala.format(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Pluja = valorsEstacio.get(12).toString() + " mm";
            Vent = valorsEstacio.get(13).toString() + " Km/h";

            dataActualitzacio = "Valors mesurats a  " + dia + "  " + hora;
        }

        FragmentInfoEstacio fragmentInfo = (FragmentInfoEstacio)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_info_container);
        fragmentInfo.setValues(Temperatura, Max, Min, Humitat, Pressio, Sunrise, Sunset, Pluja, Vent);

        if (mostra) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), dataActualitzacio, Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_estacio_no_dades, Snackbar.LENGTH_SHORT).show();
        }
    }
}


