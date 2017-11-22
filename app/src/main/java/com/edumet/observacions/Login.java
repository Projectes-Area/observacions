package com.edumet.observacions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
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
    private BottomNavigationView navigation;

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
                    LoginOK.setEnabled(false);
                    sincronitza();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

//
// LOGIN
//
    final OkHttpClient client = new OkHttpClient();

    public void sincronitza() throws Exception {

        String laUrl=getResources().getString(R.string.url_servidor);
        String cadenaRequest = laUrl+"?ident=" + Usuari.getText().toString() + "&psw=" + Contrasenya.getText().toString() + "&tab=registrar_se";
        Log.i("Login", cadenaRequest);
        Request request = new Request.Builder()
                .url(cadenaRequest)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
                                //editor.putInt("estacio_preferida", 0);
                                editor.apply();

                                ((MainActivity) getActivity()).captura();
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
                    Log.i("Login", getString(R.string.servidor_no_disponible));
                }
            }
        });
    }
}