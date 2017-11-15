package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FragmentFitxa extends Fragment {

    DadesHelper mDbHelper;

    private ImageButton Envia;
    private ImageButton Esborra;
    private ImageButton Edit;
    private ImageView imatge;
    private TextView fenomen;
    private TextView data;
    private TextView descripcio;
    private ProgressBar mProgressBar;

    private int numID;
    private String id_edumet;
    private String elDia;
    private String laHora;
    private double laLatitud;
    private double laLongitud;
    private String elFenomen;
    private String laDescripcio;
    private String elPath;
    private String elPath_Envia;
    private int enviat;
    private static boolean flagEnviada = false;

    private SQLiteDatabase db;

    Fitxa activity;
    String[] nomFenomen;
    String usuari;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_fitxa, container, false);

        mDbHelper = new DadesHelper(getContext());
        db = mDbHelper.getReadableDatabase();

        fenomen = (TextView) v.findViewById(R.id.lblFenomen);
        Envia = (ImageButton) v.findViewById(R.id.btnEnvia);
        Envia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendPost();
            }
        });
        Esborra = (ImageButton) v.findViewById(R.id.btnEsborra);
        Esborra.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Vols eliminar l'observació ?", Snackbar.LENGTH_LONG)
                        .setAction("SÍ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                esborra();
                            }
                        });
                snackbar.show();
            }
        });
        Edit = (ImageButton) v.findViewById(R.id.btnEdit);
        Edit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flagEnviada) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "No es pot editar una observació ja enviada", Snackbar.LENGTH_LONG).show();
                    Edit.setImageResource(R.mipmap.ic_edit_white);
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(MainActivity.EXTRA_ID, numID);
                    startActivity(intent);
                }
            }
        });
        imatge = (ImageView) v.findViewById(R.id.imgFoto);
        imatge.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                veure_foto();
            }
        });
        data = (TextView) v.findViewById(R.id.lblData);
        descripcio = (TextView) v.findViewById(R.id.lblDescripcio);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
        usuari = sharedPref.getString("usuari", "");

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm:ss", Locale.US);

        activity = (Fitxa) getActivity();
        numID = activity.getID();

        String[] projection = {
                DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_LATITUD,
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO,
                DadesEstructura.Parametres.COLUMN_NAME_PATH,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        String selection = DadesEstructura.Parametres._ID + " = ?";
        String[] selectionArgs = {String.valueOf(numID)};
        String sortOrder = null;

        Cursor cursor = db.query(DadesEstructura.Parametres.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();
                id_edumet = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET));
                elDia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
                laHora = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_HORA));
                laLatitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LATITUD)));
                laLongitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD)));
                elFenomen = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN));
                laDescripcio = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO));
                elPath = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH));
                elPath_Envia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA));
                enviat = cursor.getInt(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT));
        cursor.close();

        fenomen.setText(nomFenomen[Integer.valueOf(elFenomen)]);

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = format.parse(elDia);
            format = new SimpleDateFormat("HH:mm:ss", Locale.US);
            Date time = format.parse(laHora);
            data.setText(dateCatala.format(date.getTime()) + "   " + horaCatala.format(time.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        descripcio.setText(laDescripcio);
        imatge.setImageBitmap(BitmapFactory.decodeFile(elPath_Envia));

        if (enviat == 1) {
            Edit.setEnabled(false);
            Edit.setImageResource(R.mipmap.ic_edit_white);
            Envia.setEnabled(false);
            Envia.setImageResource(R.mipmap.ic_send_white);
        } else {
            flagEnviada = false;
        }
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

    public void sendPost() {

        File fitxer_a_enviar = new File(elPath_Envia);

        byte[] fotografia;
        fotografia = new byte[(int) fitxer_a_enviar.length()];
        try {
            InputStream is = new FileInputStream(fitxer_a_enviar);
            is.read(fotografia);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String encodedFoto = Base64.encodeToString(fotografia, Base64.DEFAULT);

        MainActivity.enviaObservacio(
                numID,
                encodedFoto,
                usuari,
                elDia,
                laHora,
                laLatitud,
                laLongitud,
                Integer.valueOf(elFenomen),
                laDescripcio,
                this.getContext()
        );
    }

    public void setEnviada() {
        flagEnviada = true;
    }

//
// ELIMINA OBSERVACIÓ
//

    private void esborra() {

        db = mDbHelper.getWritableDatabase();
        db.delete("observacions", DadesEstructura.Parametres._ID + "=" + String.valueOf(numID), null);

        File fitxer = new File(elPath);
        if (fitxer.exists()) {
            fitxer.delete();
        }

        fitxer = new File(elPath_Envia);
        if (fitxer.exists()) {
            fitxer.delete();
        }

        Envia.setEnabled(false);
        Envia.setImageResource(R.mipmap.ic_send_white);
        Esborra.setEnabled(false);
        Esborra.setImageResource(R.mipmap.ic_delete_white);

        if (enviat == 1) { // esborrar del servidor

            mProgressBar.setVisibility(ProgressBar.VISIBLE);

            final OkHttpClient client = new OkHttpClient();

            String laUrl = getResources().getString(R.string.url_servidor);
            String cadenaRequest = laUrl + "?usuari=" + usuari + "&id=" + id_edumet + "&tab=eliminarFenUsu";
            Request request = new Request.Builder()
                    .url(cadenaRequest)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String resposta = response.body().string().trim();
                    Log.i(".Resposta", resposta);
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                //Snackbar.make(getActivity().findViewById(android.R.id.content), "S'ha eliminat l'observació", Snackbar.LENGTH_LONG).show();
                                mProgressBar.setVisibility(ProgressBar.GONE);
                                getActivity().onBackPressed();
                            }
                        });

                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(ProgressBar.GONE);
                            }
                        });
                    }
                }
            });
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
       getActivity().onBackPressed();
    }

//
// VEURE FOTO
//

    public void veure_foto() {
        Intent intent = new Intent(getActivity(), VeureFoto.class);
        intent.putExtra(MainActivity.EXTRA_PATH, elPath);
        startActivity(intent);
    }
}
