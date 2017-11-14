package com.edumet.observacions;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class ObservacionsFetes extends Fragment {

    DadesHelper mDbHelper;

    private ProgressBar mProgressBar;

    private SQLiteDatabase db;

    String[] nomFenomen;
    String usuari;

    Bitmap bitmap;

    List itemIdsEdumet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.observacions_fetes, container, false);
        setHasOptionsMenu(true);

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mDbHelper = new DadesHelper(getContext());
        db = mDbHelper.getReadableDatabase();

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);

        usuari = sharedPref.getString("usuari", "");

        String[] projection = {
                DadesEstructura.Parametres._ID,
                DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET,
                DadesEstructura.Parametres.COLUMN_NAME_DIA,
                DadesEstructura.Parametres.COLUMN_NAME_HORA,
                DadesEstructura.Parametres.COLUMN_NAME_LATITUD,
                DadesEstructura.Parametres.COLUMN_NAME_LONGITUD,
                DadesEstructura.Parametres.COLUMN_NAME_FENOMEN,
                DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA,
                DadesEstructura.Parametres.COLUMN_NAME_ENVIAT
        };

        String selection = DadesEstructura.Parametres._ID + " > ?";
        String[] selectionArgs = {"0"};
        String sortOrder = "dia DESC, hora DESC";

        Cursor cursor = db.query(DadesEstructura.Parametres.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);                               // The sort order

        itemIdsEdumet = new ArrayList<>();
        List itemIds = new ArrayList<>();
        List itemDies = new ArrayList<>();
        List itemHores = new ArrayList<>();
        List itemLatituds = new ArrayList<>();
        List itemLongituds = new ArrayList<>();
        List itemFenomens = new ArrayList<>();
        List itemPath_Envias = new ArrayList<>();
        List itemEnviats = new ArrayList<>();
        while (cursor.moveToNext()) {
            String itemIdEdumet = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET));
            itemIdsEdumet.add(itemIdEdumet);
            String itemId = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres._ID));
            itemIds.add(itemId);
            String itemDia = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_DIA));
            itemDies.add(itemDia);
            String itemHora = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_HORA));
            itemHores.add(itemHora);
            String itemLatitud = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LATITUD));
            itemLatituds.add(itemLatitud);
            String itemLongitud = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD));
            itemLongituds.add(itemLongitud);
            String itemFenomen = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN));
            itemFenomens.add(itemFenomen);
            String itemPath_icon = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA));
            itemPath_Envias.add(itemPath_icon);
            String itemEnviat = cursor.getString(cursor.getColumnIndexOrThrow(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT));
            itemEnviats.add(itemEnviat);
        }
        cursor.close();

        SimpleDateFormat dateCatala = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat horaCatala = new SimpleDateFormat("HH:mm:ss", Locale.US);

        LinearLayout.LayoutParams paramsTitolIcona = new LinearLayout.LayoutParams(dpToPx(70), LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsTitolData = new LinearLayout.LayoutParams(dpToPx(90), LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsTitolFenomen = new LinearLayout.LayoutParams(dpToPx(80),LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsTitolEnviada = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams paramsIcona = new LinearLayout.LayoutParams(dpToPx(60),dpToPx(60));
        LinearLayout.LayoutParams paramsData = new LinearLayout.LayoutParams(dpToPx(90), dpToPx(70));
        LinearLayout.LayoutParams paramsFenomen = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(70));
        LinearLayout.LayoutParams paramsEnviada = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dpToPx(40));

        LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dpToPx(70));

        // TITOLS

        final LinearLayout lc = (LinearLayout) v.findViewById(R.id.llCapsalera);

        LinearLayout llTitol = new LinearLayout(getContext());
        llTitol.setOrientation(LinearLayout.HORIZONTAL);

        TextView lblFoto =new TextView(getContext());
        lblFoto.setText("Foto");
        TextView lblData =new TextView(getContext());
        lblData.setText("Data");
        TextView lblFenomen =new TextView(getContext());
        lblFenomen.setText("Observació");
        TextView lblEnviada =new TextView(getContext());
        lblEnviada.setText("Enviada");

        lblFoto.setLayoutParams(paramsTitolIcona);
        lblFoto.setGravity(Gravity.CENTER_HORIZONTAL);
        lblData.setLayoutParams(paramsTitolData);
        lblData.setGravity(Gravity.CENTER_HORIZONTAL);
        lblFenomen.setLayoutParams(paramsTitolFenomen);
        lblFenomen.setGravity(Gravity.CENTER_HORIZONTAL);
        lblEnviada.setLayoutParams(paramsTitolEnviada);
        lblEnviada.setGravity(Gravity.CENTER_HORIZONTAL);

        llTitol.addView(lblFoto);
        llTitol.addView(lblData);
        llTitol.addView(lblFenomen);
        llTitol.addView(lblEnviada);

        lc.addView(llTitol);

        View line0 = new View(getContext());
        RelativeLayout.LayoutParams lineparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
        line0.setLayoutParams(lineparams);
        line0.setBackgroundColor(getResources().getColor(R.color.edumet));

        lc.addView(line0);

        final LinearLayout lm = (LinearLayout) v.findViewById(R.id.linearLY);

        for (int j = 0; j < itemDies.size(); j++) {
            final String parametreID = itemIds.get(j).toString();
            final String parametreLAT = itemLatituds.get(j).toString();
            final String parametreLON = itemLongituds.get(j).toString();
            final String numFenomen = itemFenomens.get(j).toString();

            LinearLayout ll = new LinearLayout(getContext());
            ll.setGravity(Gravity.CENTER_VERTICAL);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), Fitxa.class);
                    intent.putExtra(MainActivity.EXTRA_LATITUD, parametreLAT);
                    intent.putExtra(MainActivity.EXTRA_LONGITUD, parametreLON);
                    intent.putExtra(MainActivity.EXTRA_NUMFENOMEN, numFenomen);
                    intent.putExtra(MainActivity.EXTRA_ID, parametreID);
                    startActivityForResult(intent, 1);
                }
            });
            final ImageView img = new ImageView(getContext());
            img.setId(j + 1);

            // ICONA

            setPic(60, 60, itemPath_Envias.get(j).toString());
            paramsIcona.setMargins(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
            img.setLayoutParams(paramsIcona);
            img.setImageBitmap(bitmap);
            ll.addView(img);

            // DATA

            LinearLayout llData = new LinearLayout(getContext());
            llData.setGravity(Gravity.CENTER_HORIZONTAL+Gravity.CENTER_VERTICAL);
            llData.setOrientation(LinearLayout.VERTICAL);
            llData.setLayoutParams(paramsData);
            TextView lblDia = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = format.parse(itemDies.get(j).toString());
                lblDia.setText(dateCatala.format(date.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // DIA

            lblDia.setGravity(Gravity.CENTER_HORIZONTAL+Gravity.CENTER_VERTICAL);
            llData.addView(lblDia);
            TextView lblHora = new TextView(getContext());
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
                Date time = format.parse(itemHores.get(j).toString());
                lblHora.setText(horaCatala.format(time.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //HORA

            lblHora.setGravity(Gravity.CENTER_HORIZONTAL+Gravity.CENTER_VERTICAL);
            llData.addView(lblHora);
            ll.addView(llData);

            // FENOMEN

            TextView lblFen = new TextView(getContext());
            lblFen.setText(nomFenomen[Integer.parseInt(itemFenomens.get(j).toString())]);
            lblFen.setGravity(Gravity.CENTER_HORIZONTAL+Gravity.CENTER_VERTICAL);
            lblFen.setLayoutParams(paramsFenomen);
            ll.addView(lblFen);

            // CHECK

            ImageView chk = new ImageView(getContext());
            if (Integer.valueOf(itemEnviats.get(j).toString()) == 1) {
                chk.setImageResource(R.mipmap.ic_check_on);
            } else {
                chk.setImageResource(R.mipmap.ic_check_off);
            }
            paramsEnviada.setMargins(0,20,0,20);
            chk.setLayoutParams(paramsEnviada);
            ll.addView(chk);
            lm.addView(ll);

            // LINE

            View line = new View(getContext());
            line.setLayoutParams(lineparams);
            line.setBackgroundColor(getResources().getColor(R.color.edumet));
            lm.addView(line);
        }
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarObservacions);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Boolean actualitzar = getArguments().getBoolean("actualitzar", false);
        int numNoves = getArguments().getInt("noves", 0);
        if (actualitzar) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "Sincronitzant amb el servidor ...", Snackbar.LENGTH_SHORT).show();
            try {
                sincronitza();
            } catch (Exception e) {
                Log.i("Exception", "error");
            }
        } else {
            String missatge;
            if (numNoves == 1) {
                missatge = "S'ha baixat una nova observació";
                Snackbar.make(getActivity().findViewById(android.R.id.content), missatge, Snackbar.LENGTH_SHORT).show();
            }
            if (numNoves > 1) {
                missatge = "S'han baixat " + String.valueOf(numNoves) + " noves observacions";
                Snackbar.make(getActivity().findViewById(android.R.id.content), missatge, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.observacions_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((MainActivity) getActivity()).redrawObservacionsFetes(0);
    }

    public void redraw(int numNoves) {
        ((MainActivity) getActivity()).redrawObservacionsFetes(numNoves);
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    private void setPic(int targetW, int targetH, String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmap = BitmapFactory.decodeFile(path, bmOptions);
    }

    //
    // SINCRONITZA
    //

    private final OkHttpClient client = new OkHttpClient();

    private int numNovesObservacions;
    private int numObservacionsBaixades;

    // Listes de les noves observacions

    List id_edumet = new ArrayList<>();
    List dia = new ArrayList<>();
    List hora = new ArrayList<>();
    List latitud = new ArrayList<>();
    List longitud = new ArrayList<>();
    List numFenomen = new ArrayList<>();
    List descripcio = new ArrayList<>();
    List nom_remot = new ArrayList<>();
    String nous_paths[];

    public void sincronitza() throws Exception {
        String laUrl=getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                .url(laUrl+"?usuari=" + usuari + "&tab=visuFeno")
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

                                                        for (int i = 0; i < jsonArray.length(); i++) {

                                                            JSONArray JSONobservacio = jsonArray.getJSONArray(i);
                                                            Log.i("remota",String.valueOf(JSONobservacio.getString(0)));
                                                            flagRepetida = false;
                                                            for (int j = 0; j < itemIdsEdumet.size(); j++) {

                                                                int Num1=Integer.valueOf(itemIdsEdumet.get(j).toString());
                                                                int Num2=Integer.valueOf(JSONobservacio.getString(0).toString());
                                                                //Log.i("flag",String.valueOf(Num1)+String.valueOf(Num2));
                                                                if (Num1 == Num2) {
                                                                    flagRepetida = true;
                                                                    //Log.i("flag",String.valueOf(flagRepetida));
                                                                }
                                                            }
                                                            Log.i("flag",String.valueOf(flagRepetida));
                                                            if (!flagRepetida) {
                                                                numNovaObservacio++;
                                                                id_edumet.add(JSONobservacio.getString(0));
                                                                dia.add(JSONobservacio.getString(2));
                                                                hora.add(JSONobservacio.getString(3));
                                                                latitud.add(JSONobservacio.getString(4));
                                                                longitud.add(JSONobservacio.getString(5));
                                                                numFenomen.add(JSONobservacio.getString(7));
                                                                descripcio.add(JSONobservacio.getString(8));
                                                                nom_remot.add(JSONobservacio.getString(9));

                                                                SimpleDateFormat formatDiaEdumet = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                                SimpleDateFormat formatHoraEdumet = new SimpleDateFormat("HH:mm:ss", Locale.US);

                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                                SimpleDateFormat shf = new SimpleDateFormat("HHmmss", Locale.US);

                                                                String diaString = sdf.format(formatDiaEdumet.parse(JSONobservacio.getString(2)));
                                                                String horaString = shf.format(formatHoraEdumet.parse(JSONobservacio.getString(3)));
                                                                String nomFitxer = diaString + horaString;

                                                                try {
                                                                    downloadFileAsync(numNovaObservacio, "https://edumet.cat/edumet/meteo_proves/imatges/fenologia/" + JSONobservacio.getString(9), nomFitxer);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }

                                                        }
                                                        numNovesObservacions = numNovaObservacio;
                                                        Log.i("NovesObs", String.valueOf(numNovesObservacions));
                                                        nous_paths = new String[numNovesObservacions];

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

    public void downloadFileAsync(final int numNovaObservacio, final String downloadUrl, final String nomFitxer) throws Exception {
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

                                                File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                                File miniatura = File.createTempFile(nomFitxer, ".jpg", storageDir);
                                                try {
                                                    FileOutputStream out = new FileOutputStream(miniatura);
                                                    out.write(response.body().bytes());
                                                    out.flush();
                                                    out.close();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                nous_paths[numNovaObservacio - 1] = miniatura.getAbsolutePath();
                                                numObservacionsBaixades++;
                                                if (numObservacionsBaixades == numNovesObservacions) {
                                                    Log.i("Baixades", String.valueOf(numNovesObservacions));
                                                    inclouNousRegistres();
                                                    redraw(numNovesObservacions);
                                                }
                                            }
                                        }
        );
    }

    public void inclouNousRegistres() {
        db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        for (int i = 0; i < numNovesObservacions; i++) {
            values.put(DadesEstructura.Parametres.COLUMN_NAME_ID_EDUMET, id_edumet.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_DIA, dia.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_HORA, hora.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_LATITUD, latitud.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_LONGITUD, longitud.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_FENOMEN, numFenomen.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_DESCRIPCIO, descripcio.get(i).toString());
            values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH, nous_paths[i]);
            values.put(DadesEstructura.Parametres.COLUMN_NAME_PATH_ENVIA, nous_paths[i]);
            values.put(DadesEstructura.Parametres.COLUMN_NAME_ENVIAT, 1);

            long newRowId = db.insert(DadesEstructura.Parametres.TABLE_NAME, null, values);
        }
    }

//
// GENERAL
//
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
