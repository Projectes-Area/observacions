package com.edumet.observacions;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Login extends Fragment {

    private Button LoginOK;
    private EditText Usuari;
    private EditText Contrasenya;
    private ProgressBar mProgressBar;

    private boolean flagAutentificat = false;
    private boolean flagEstacions = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login, container, false);
        LoginOK = (Button) v.findViewById(R.id.btnLogin);
        Usuari = (EditText) v.findViewById(R.id.txtUser);
        Contrasenya = (EditText) v.findViewById(R.id.txtPassword);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarLogin);
        mProgressBar.setVisibility(ProgressBar.GONE);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        LoginOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    LoginOK.setEnabled(false);
                    baixaEstacions();
                    autentica();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

//
// LOGIN
//
    public void autentica() throws Exception {

        String laUrl = getResources().getString(R.string.url_servidor);
        String cadenaRequest = laUrl + "?ident=" + Usuari.getText().toString() + "&psw=" + Contrasenya.getText().toString() + "&tab=registrar_se";
        Log.i("Login", cadenaRequest);
        Request request = new Request.Builder()
                .url(cadenaRequest)
                .build();
        final OkHttpClient client = new OkHttpClient();

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
                final String resposta = response.body().string().trim();
                Log.i("Resposta", resposta);
                if (response.isSuccessful()) {
                    if (resposta.isEmpty()) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.identificacio_incorrecta, Snackbar.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(ProgressBar.GONE);
                                LoginOK.setEnabled(true);
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressBar.setVisibility(ProgressBar.GONE);
                                LoginOK.setEnabled(true);
                                SharedPreferences sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("usuari", Usuari.getText().toString());
                                editor.putString("nom_usuari", resposta);
                                editor.apply();
                                Log.i(".Autentificació", "Correcta");
                                flagAutentificat = true;
                                if (flagEstacions) {
                                    ((MainActivity) getActivity()).captura();
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            mProgressBar.setVisibility(ProgressBar.GONE);
                                        }
                                    });
                                }
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.servidor_no_disponible, Snackbar.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(ProgressBar.GONE);
                            LoginOK.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

//
// BAIXA ESTACIONS
//

    EstacionsHelper mDbHelper;
    private SQLiteDatabase db;

    public void baixaEstacions() throws Exception {
        String laUrl = getResources().getString(R.string.url_servidor);
        Request request = new Request.Builder()
                //.url(laUrl + "?tab=cnjEst&xarxaEst=meteoCat")
                .url(laUrl + "?tab=cnjEst")
                .build();

        Log.i("Baixa", laUrl + "?tab=cnjEst");

        final OkHttpClient client = new OkHttpClient();
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
                                                    Log.i(".Estacions",resposta);
                                                    try {
                                                        JSONArray jsonArray = new JSONArray(resposta);
                                                        mDbHelper = new EstacionsHelper(getContext());
                                                        db = mDbHelper.getWritableDatabase();
                                                        ContentValues values = new ContentValues();

                                                        for (int i = 0; i < jsonArray.length(); i++) {
                                                            JSONArray JSONEstacio = jsonArray.getJSONArray(i);

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
                                                        }
                                                        Log.i(".Estacions", "Tot baixat");
                                                        flagEstacions = true;
                                                        if (flagAutentificat) {
                                                            ((MainActivity) getActivity()).captura();
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    mProgressBar.setVisibility(ProgressBar.GONE);
                                                                }
                                                            });
                                                        }
                                                    } catch (Exception e) {
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

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}