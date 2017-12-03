package com.edumet.observacions;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;

public class Captura extends Fragment {

    private static final int CONTENT_REQUEST = 1337; // fotos
    private static final String EXTRA_FILENAME = "com.edumet.observacions.EXTRA_FILENAME";

    private ImageButton Foto;
    private ImageButton Girar;
    private ImageButton Envia;
    private ImageButton ObservacionsFetes;
    private ImageButton Mapa;
    public ImageView imatge;
    private EditText observacio;
    private Spinner spinner;

    private String timeStamp;
    private String mCurrentPhotoPath;
    private File output = null;
    private File outputMiniatura = null;
    private int midaEnvia = 800;
    public Bitmap bitmap;
    private int num_fenomen = 1;
    public int angle_foto;
    private String dia;
    private String hora;
    private Double laLatitud;
    private Double laLongitud;
    private int ID_App;

    private boolean flagGirada = false;
    private boolean flagDesada = false;
    private boolean flagEditada = false;
    private boolean flagEnEdicio = false;
    private static boolean flagEnviada = false;

    private DataHelper mDbHelper;

    private Location mCurrentLocation;

    String[] nomFenomen;
    String usuari;

    BottomNavigationView navigation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.captura, container, false);
        setHasOptionsMenu(true);

        Foto = (ImageButton) v.findViewById(R.id.btnFoto);
        Girar = (ImageButton) v.findViewById(R.id.btnGirar);
        Envia = (ImageButton) v.findViewById(R.id.btnEnvia);
        ObservacionsFetes = (ImageButton) v.findViewById(R.id.btnPendents);
        Mapa = (ImageButton) v.findViewById(R.id.btnMapa);
        imatge = (ImageView) v.findViewById(R.id.imgFoto);
        observacio = (EditText) v.findViewById(R.id.txtObservacions);
        spinner = (Spinner) v.findViewById(R.id.spinner);

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);

        navigation = (BottomNavigationView) v.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationHelper.disableShiftMode(navigation);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.edumet.observacions", getActivity().MODE_PRIVATE);
        usuari = sharedPref.getString("usuari", "");
        Double latDesada=Double.valueOf(sharedPref.getString("latitud","0"));
        Double lonDesada=Double.valueOf(sharedPref.getString("longitud","0"));
        mCurrentLocation=new Location("");
        mCurrentLocation.setLatitude(latDesada);
        mCurrentLocation.setLongitude(lonDesada);

        return v;
    }

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (flagDesada) {
                updateObservacio();
            }
            flagEnEdicio = false;
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_observacions:
                    return true;
                case R.id.navigation_estacions:
                    intent = new Intent(getActivity().getApplicationContext(), Estacions.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_radar:
                    intent = new Intent(getActivity().getApplicationContext(), Radar.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_pronostic:
                    intent = new Intent(getActivity().getApplicationContext(), Pronostic.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Foto.setEnabled(false);
        Foto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fesFoto();
            }
        });
        Girar.setEnabled(false);
        Girar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                angle_foto += 90;
                if (angle_foto >= 360) {
                    angle_foto = 0;
                }
                Log.i(".Angle", String.valueOf(angle_foto));
                bitmap = rotateViaMatrix(bitmap, 90);
                imatge.setImageBitmap(bitmap);
                flagGirada = true;
            }
        });
        Envia.setEnabled(false);
        Envia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateObservacio();
                if (flagEnEdicio) {
                    sendPost(laLatitud, laLongitud);
                } else {
                    sendPost(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
            }
        });

        ObservacionsFetes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flagDesada) {
                    updateObservacio();
                }
                flagEnEdicio = false;
                ((MainActivity) getActivity()).observacionsFetes();
            }
        });
        Mapa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapa();
            }
        });
        imatge.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                veure_foto();
            }
        });

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        List<String> categories = new ArrayList<String>();
        for (int i = 1; i < nomFenomen.length; i++) {
            categories.add(nomFenomen[i]);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                num_fenomen = position + 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Bundle arguments = getArguments();

        if (arguments != null && !flagEditada) {
            ID_App = getArguments().getInt("ID_App", 0);
            flagEditada = true;
        } else {
            ID_App = 0;
        }
        if (ID_App > 0) {
            flagEnEdicio = true;
            loadObservacio();
        }

        int numPendents = checkPendents();
        if (numPendents > 0) {
            ObservacionsFetes.setImageResource(R.mipmap.ic_time_red);
        }

        //updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.captura_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(EXTRA_FILENAME, output);
        super.onSaveInstanceState(savedInstanceState);
    }

    /*private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            updateLocationUI();
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_observacions);
        //updateLocationUI();
        if (output != null) {
            imatge.setImageBitmap(bitmap);
            if (!flagEnviada) {
                enableBotons();
            }
        } else {
            imatge.setImageResource(R.drawable.estacions);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imatge.setImageBitmap(bitmap);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    angle_foto = 0;
                    setPic(midaEnvia, midaEnvia);
                    imatge.setImageBitmap(bitmap);
                    galleryAddPic();
                    enableBotons();
                    ((MainActivity) getActivity()).hihaFoto();
                    desa();
                    flagDesada = true;
                    flagGirada = false;
                    flagEnviada = false;
                    flagEnEdicio = false;
                    Log.i(".onActivityResult", "Foto");
                } else {
                    imatge.setImageResource(R.drawable.estacions);
                }
                break;
        }
    }

    private void enableBotons() {
        Girar.setImageResource(R.mipmap.ic_rotate_edumet);
        Girar.setEnabled(true);
        Envia.setImageResource(R.mipmap.ic_send_edumet);
        Envia.setEnabled(true);
    }

/*    private void updateLocationUI() {
            Foto.setImageResource(R.mipmap.ic_camera_edumet);
            Foto.setEnabled(true);
            Mapa.setImageResource(R.mipmap.ic_location_edumet);
            Mapa.setEnabled(true);
    }*/

    //
    // MAPA
    //

    public void mapa() {
        if (flagEnEdicio) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(laLatitud));
            intent.putExtra(MainActivity.EXTRA_LONGITUD, String.valueOf(laLongitud));
            intent.putExtra(MainActivity.EXTRA_NUMFENOMEN, String.valueOf(num_fenomen));
            startActivity(intent);
        } else {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(mCurrentLocation.getLatitude()));
                intent.putExtra(MainActivity.EXTRA_LONGITUD, String.valueOf(mCurrentLocation.getLongitude()));
                intent.putExtra(MainActivity.EXTRA_NUMFENOMEN, "0");
                startActivity(intent);
        }
    }

    //
    // FOTOGRAFIA
    //

    public void fesFoto() {
        if (mCurrentLocation != null) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                try {
                    output = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                if (output != null) {
                    Uri outputUri = getUriForFile(getContext(), "com.edumet.observacions", output);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "Una foto", outputUri);
                        i.setClipData(clip);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            getActivity().grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                    try {
                        startActivityForResult(i, CONTENT_REQUEST);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(super.getContext(), R.string.msg_no_camera, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }
            }
        } else {
            Toast.makeText(super.getContext(), R.string.encara_sense_lloc, Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(timeStamp, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void fesMiniatura(int x) {
        try {
            String minTimeStamp = String.valueOf(x) + "_" + timeStamp;
            File storageDir = getActivity().getFilesDir();
            outputMiniatura = File.createTempFile(minTimeStamp, ".jpg", storageDir);
        } catch (IOException ex) {
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        try {
            FileOutputStream out = new FileOutputStream(outputMiniatura);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void setPic(int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    static public Bitmap rotateViaMatrix(Bitmap original, int angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return (Bitmap.createBitmap(original, 0, 0, original.getWidth(),
                original.getHeight(), matrix, true));
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

    public void sendPost(Double latitud, Double longitud) {
        File fitxer_a_enviar = new File(outputMiniatura.getAbsolutePath());

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

        ((MainActivity) getActivity()).enviaObservacio(
                ID_App,
                encodedFoto,
                usuari,
                dia,
                hora,
                latitud, //mCurrentLocation.getLatitude(),
                longitud, //mCurrentLocation.getLongitude(),
                num_fenomen,
                observacio.getText().toString(),
                this.getContext()
        );
    }

    public void setEnviada() {
        flagEnviada = true;
    }

    //
    // DESA
    //

    public void desa() {
        fesMiniatura(midaEnvia);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        dia = sdf.format(Calendar.getInstance().getTime());
        hora = shf.format(Calendar.getInstance().getTime());

        ID_App = ((MainActivity) getActivity()).desaObservacio(
                dia,
                hora,
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                num_fenomen,
                observacio.getText().toString(),
                mCurrentPhotoPath,
                outputMiniatura.getAbsolutePath()
        );
    }

    //
    // EDITA OBSERVACIO
    //

    public void loadObservacio() {
        String[] projection = {
                Database.Observacions.COLUMN_NAME_DIA,
                Database.Observacions.COLUMN_NAME_HORA,
                Database.Observacions.COLUMN_NAME_LATITUD,
                Database.Observacions.COLUMN_NAME_LONGITUD,
                Database.Observacions.COLUMN_NAME_FENOMEN,
                Database.Observacions.COLUMN_NAME_DESCRIPCIO,
                Database.Observacions.COLUMN_NAME_PATH,
                Database.Observacions.COLUMN_NAME_PATH_ENVIA,
        };

        String selection = Database.Observacions._ID + " = ?";
        String[] selectionArgs = {String.valueOf(ID_App)};
        String sortOrder = null;

        mDbHelper = new DataHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Database.Observacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();
        dia = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_DIA));
        hora = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_HORA));
        laLatitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_LATITUD)));
        laLongitud = Double.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_LONGITUD)));
        String elFenomen = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_FENOMEN));
        String laDescripcio = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_DESCRIPCIO));
        String elPath = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_PATH));
        String elPath_Envia = cursor.getString(cursor.getColumnIndexOrThrow(Database.Observacions.COLUMN_NAME_PATH_ENVIA));
        cursor.close();
        mDbHelper.close();

        num_fenomen = Integer.valueOf(elFenomen);
        spinner.setSelection(num_fenomen - 1);
        observacio.setText(laDescripcio);
        output = new File(elPath);
        outputMiniatura = new File(elPath_Envia);
        bitmap = BitmapFactory.decodeFile(elPath_Envia);
        imatge.setImageBitmap(bitmap);
        mCurrentPhotoPath = elPath;

        angle_foto = 0;
        enableBotons();
        ((MainActivity) getActivity()).hihaFoto();
        flagDesada = true;
        flagGirada = false;
        flagEnviada = false;
    }

    public void updateObservacio() {
        String unlog = String.valueOf(ID_App);
        Log.i(".UpdateID", unlog);

        if (flagGirada) {
            File fitxer = new File(outputMiniatura.getAbsolutePath());
            if (fitxer.exists()) {
                fitxer.delete();
            }
            fesMiniatura(midaEnvia);
        }
        mDbHelper = new DataHelper(getContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Database.Observacions.COLUMN_NAME_FENOMEN, num_fenomen);
        values.put(Database.Observacions.COLUMN_NAME_DESCRIPCIO, observacio.getText().toString());
        values.put(Database.Observacions.COLUMN_NAME_PATH_ENVIA, outputMiniatura.getAbsolutePath());

        String selection = Database.Observacions._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(ID_App)};

        db.update(Database.Observacions.TABLE_NAME, values, selection, selectionArgs);
        mDbHelper.close();
    }

//
// VEURE FOTO
//

    public void veure_foto() {
        if (output != null) {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(getActivity(), VeureFoto.class);
                intent.putExtra(MainActivity.EXTRA_PATH, mCurrentPhotoPath);
                startActivity(intent);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = getUriForFile(getContext(), "com.edumet.observacions", output);
                intent.setDataAndType(uri, "image/jpeg");
                PackageManager pm = getActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                }
            }
        }
    }

    public int checkPendents() {
        mDbHelper = new DataHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {};
        String selection = Database.Observacions.COLUMN_NAME_ENVIAT + " = ?";
        String[] selectionArgs = {"0"};
        String sortOrder = "enviat DESC";

        Cursor cursor = db.query(Database.Observacions.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        int nPendents = cursor.getCount();
        cursor.close();
        mDbHelper.close();
        return nPendents;
    }
}