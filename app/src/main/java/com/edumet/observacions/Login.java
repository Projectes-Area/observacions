package com.edumet.observacions;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                    sincronitza();
                } catch (Exception e) {
                    Log.i("Exception", "error");
                }
            }
        });

    }

//
// SINCRONITZA
//

    final OkHttpClient client = new OkHttpClient();

    public void sincronitza() throws Exception {
        String cadenaRequest="https://edumet.cat/edumet/meteo_proves/dades_recarregar.php?ident="+Usuari.getText().toString()+"&psw="+Contrasenya.getText().toString()+"&tab=registrar_se";
        Log.i("post",cadenaRequest);
        Request request = new Request.Builder()
                .url(cadenaRequest)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                Log.i("RESPONSE", response.toString());
                Log.i("CONTENT", response.body().toString());
                String resposta=response.body().string().trim();
                Log.i("TRIMMED", resposta);
                if (response.isSuccessful()) {
                    if(resposta=="") {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Identificació incorrecta", Snackbar.LENGTH_LONG).show();
                                //Toast.makeText(getActivity().getBaseContext(), R.string.dades_enviades, Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(ProgressBar.GONE);
                                ((MainActivity) getActivity()).captura();
                            }
                        });
                    }
                    else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Benvingut/da", Snackbar.LENGTH_LONG).show();
                                //Toast.makeText(getActivity().getBaseContext(), R.string.dades_enviades, Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(ProgressBar.GONE);
                                ((MainActivity) getActivity()).captura();
                            }
                        });
                    }

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_connexio,Snackbar.LENGTH_LONG).show();
                            //Toast.makeText(getActivity().getBaseContext(), R.string.error_connexio, Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });
                    Log.i("CLIENT", getString(R.string.error_servidor));
                }
            }
        });
    }
    }