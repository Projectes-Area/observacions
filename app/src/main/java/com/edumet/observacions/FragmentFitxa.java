package com.edumet.observacions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.v4.content.FileProvider.getUriForFile;

public class FragmentFitxa extends Fragment {

    DataHelper mDbHelper;

    private ImageButton Envia;
    private ImageButton Esborra;
    private ImageButton Edit;
    private ImageView imatge;
    private TextView fenomen;
    private TextView data;
    private TextView descripcio;
    private ProgressBar mProgressBar;

    private int ID_App;
    private String ID_Edumet;
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
    //String[] nomFenomen;
    String usuari;
    //String[] nomFenomen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_fitxa, container, false);

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
                    intent.putExtra(MainActivity.EXTRA_ID_App, ID_App);
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

        //Resources res = getResources();
        //nomFenomen = res.getStringArray(R.array.nomFenomen);

        List<String> nomFenomen0 = new ArrayList<String>();
        //List<String> categories = new ArrayList<String>();

        String[] projection0 = {
                DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO,
        };
        String selection0 = DatabaseFeno.Fenologies.COLUMN_NAME_ID_FENO + " > ?";
        String[] selectionArgs0 = {"0"};
        String sortOrder0 = null;


        mDbHelper = new DataHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor0 = db.query(DatabaseFeno.Fenologies.TABLE_NAME_FENO, projection0, selection0, selectionArgs0, null, null, sortOrder0);

        while (cursor0.moveToNext()) {
            nomFenomen0.add(cursor0.getString(cursor0.getColumnIndexOrThrow(DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO)));
            //categories.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseFeno.Fenologies.COLUMN_NAME_TITOL_FENO)));
        }
        cursor0.close();
        mDbHelper.close();





        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm:ss", Locale.US);

        activity = (Fitxa) getActivity();
        ID_App = activity.getID();

        String[] projection = {
                Database.Observacions.COLUMN_NAME_ID_EDUMET,
                Database.Observacions.COLUMN_NAME_DIA,
                Database.Observacions.COLUMN_NAME_HORA,
                Database.Observacions.COLUMN_NAME_LATITUD,
                Database.Observacions.COLUMN_NAME_LONGITUD,
                Database.Observacions.COLUMN_NAME_FENOMEN,
                Database.Observacions.COLUMN_NAME_DESCRIPCIO,
                Database.Observacions.COLUMN_NAME_PATH,
                Database.Observacions.COLUMN_NAME_PATH_ENVIA,
                Database.Observacions.COLUMN_NAME_ENVIAT
        };

        String selection = Database.Observacions._ID + " = ?";
        String[] selectionArgs = {String.valueOf(ID_App)};
        String sortOrder = null;

        mDbHelper = new DataHelper(getContext());
        db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Database.Observacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();
                ID_Edumet = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_ID_EDUMET));
                elDia = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_DIA));
                laHora = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_HORA));
                laLatitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_LATITUD)));
                laLongitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_LONGITUD)));
                elFenomen = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_FENOMEN));
                laDescripcio = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_DESCRIPCIO));
                elPath = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_PATH));
                elPath_Envia = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_PATH_ENVIA));
                enviat = cursor.getInt(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_ENVIAT));
        cursor.close();
        mDbHelper.close();

        String[] nomFenomen = nomFenomen0.toArray(new String[0]);

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
                ID_App,
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

        mDbHelper = new DataHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        db.delete("observacions", Database.Observacions._ID + "=" + String.valueOf(ID_App), null);
        mDbHelper.close();

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

        if (enviat == 1) {  //Esborrar del servidor

            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            final OkHttpClient client = new OkHttpClient();

            String laUrl = getResources().getString(R.string.url_servidor);
            String cadenaRequest = laUrl + "?usuari=" + usuari + "&id=" + ID_Edumet + "&tab=eliminarFenUsu";
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
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(getActivity(), VeureFoto.class);
            intent.putExtra(MainActivity.EXTRA_PATH, elPath);
            startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File newFile = new File(elPath);
            Uri uri = getUriForFile(getContext(), "com.edumet.observacions", newFile);
            intent.setDataAndType(uri, "image/jpeg");
            PackageManager pm = getActivity().getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
            }
        }
}

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

}
