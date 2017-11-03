package com.edumet.observacions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.v4.content.FileProvider.getUriForFile;

public class FragmentFitxa extends Fragment {

    DadesHelper mDbHelper;

    private ImageButton Envia;
    private ImageButton Mapa;
    private ImageButton Esborra;
    private ImageView imatge;
    private TextView fenomen;
    private TextView data;
    private TextView descripcio;
    private ProgressBar mProgressBar;

    private int numID;
    private String elDia;
    private String laHora;
    private double laLatitud;
    private double laLongitud;
    private String elFenomen;
    private String laDescripcio;
    private String elPath;
    private String elPath_Envia;

    private SQLiteDatabase db;

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        Mapa = (ImageButton) v.findViewById(R.id.btnMapa);
        Mapa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapa();
            }
        });
        Esborra = (ImageButton) v.findViewById(R.id.btnEsborra);
        Esborra.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                esborra();
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
        mProgressBar=(ProgressBar) v.findViewById(R.id.progressBar);

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm:ss");

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(elDia);
            format = new SimpleDateFormat("HH:mm:ss");
            Date time = format.parse(laHora);
            data.setText(dateCatala.format(date.getTime()) + " " + horaCatala.format(time.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Fitxa activity = (Fitxa) getActivity();
        numID=activity.getID();
        Log.i("numID",String.valueOf(numID));


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
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };
        // Filter results
        String selection = DadesEstructura.Parametres._ID + " = ?";
        String[] selectionArgs = {String.valueOf(numID)};
        //String sortOrder = DadesEstructura.Parametres.COLUMN_NAME_DIA+ " DESC";
        String sortOrder = null;

        Cursor cursor = db.query(
                DadesEstructura.Parametres.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while (cursor.moveToNext()) {
            if (Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres._ID))) == numID) {
                elDia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
                laHora = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_HORA));
                laLatitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LATITUD)));
                laLongitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD)));
                elFenomen = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN));
                laDescripcio = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO));
                elPath = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH));
                elPath_Envia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA));
            }
        }
        cursor.close();

        fenomen.setText(nomFenomen(Integer.valueOf(elFenomen)));
        data.setText(elDia+" "+laHora);
        descripcio.setText(laDescripcio);
        imatge.setImageBitmap(BitmapFactory.decodeFile(elPath_Envia));
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

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

    private void sendPost() {

        File fitxer_a_enviar=new File(elPath_Envia);

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

/*        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss");*/
/*        String dia = sdf.format(Calendar.getInstance().getTime());
        String hora = shf.format(Calendar.getInstance().getTime());*/

        final OkHttpClient client = new OkHttpClient();

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("fitxer", encodedFoto);
            jsonParam.put("usuari", 43900018);
            jsonParam.put("dia", elDia);
            jsonParam.put("hora", laHora);
            jsonParam.put("lat", laLatitud);
            jsonParam.put("lon", laLongitud);
            jsonParam.put("id_feno", Integer.valueOf(elFenomen));
            jsonParam.put("descripcio", laDescripcio);
            jsonParam.put("tab", "salvarFenoApp");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("JSON sortida", jsonParam.toString());

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonParam.toString());

        final Request request = new Request.Builder()
                .url("https://edumet.cat/edumet/meteo_proves/dades_recarregar.php")
                //.url("https://edumet.cat/edumet/meteo_2/dades_recarregar_feno.php")
                //.url("http://tecnologia.isantandreu.net/prova.php")
                //.url("https://edumet.cat/edumet/meteo_proves/prova.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "auth")
                .addHeader("cache-control", "no-cache")
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
                                                Log.i("CLIENT", getString(R.string.error_connexio));
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                                Log.i("RESPONSE", response.toString());
                                                Log.i("CONTENT", response.body().string());
                                                if (response.isSuccessful()) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.dades_enviades, Snackbar.LENGTH_LONG).show();
                                                            //Toast.makeText(getActivity().getBaseContext(), R.string.dades_enviades, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                            Envia.setEnabled(false);
                                                            Envia.setImageResource(R.mipmap.ic_send_white);
                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.dades_enviades));
                                                } else {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio, Snackbar.LENGTH_LONG).show();
                                                            //Toast.makeText(getActivity().getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                                        }
                                                    });
                                                    Log.i("CLIENT", getString(R.string.error_servidor));
                                                }
                                            }
                                        }
        );
    }

//
// ESBORRA OBSERVACIÓ
//

    private void esborra() {

        db = mDbHelper.getWritableDatabase();

        db.delete("observacions", DadesEstructura.Parametres._ID+ "=" + String.valueOf(numID), null);

        Envia.setEnabled(false);
        Envia.setImageResource(R.mipmap.ic_send_white);
        Esborra.setEnabled(false);
        Esborra.setImageResource(R.mipmap.ic_delete_white);

        Snackbar.make(getActivity().findViewById(android.R.id.content),"S'ha esborrat l'observació",Snackbar.LENGTH_LONG).show();
    }

    //
    // MAPA
    //

    public void mapa() {

        Intent intent = new Intent(getActivity(), MapsActivity.class);
        String message = " ";
        intent.putExtra(MainActivity.EXTRA_MESSAGE, nomFenomen(Integer.valueOf(elFenomen)));
        intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(laLatitud));
        intent.putExtra(MainActivity.EXTRA_LONGITUD,String.valueOf(laLongitud));
        startActivity(intent);
    }

//
// VEURE FOTO
//

    public void veure_foto() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File newFile = new File(elPath);
        Log.i("path",elPath);
        Uri uri = getUriForFile(getContext(), "com.edumet.observacions", newFile);
        intent.setDataAndType(uri, "image/*");

        PackageManager pm = getActivity().getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }
    }
}
