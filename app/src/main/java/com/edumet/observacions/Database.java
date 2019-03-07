package com.edumet.observacions;

import android.provider.BaseColumns;

public final class Database {
    private Database() {}
    public static class Observacions implements BaseColumns {
        public static final String TABLE_NAME = "observacions";
        public static final String COLUMN_NAME_ID_EDUMET = "id_edumet";
        public static final String COLUMN_NAME_DIA = "dia";
        public static final String COLUMN_NAME_HORA = "hora";
        public static final String COLUMN_NAME_LATITUD = "latitud";
        public static final String COLUMN_NAME_LONGITUD = "longitud";
        public static final String COLUMN_NAME_FENOMEN = "fenomen";
        public static final String COLUMN_NAME_DESCRIPCIO = "descripcio";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_PATH_ENVIA = "path_envia";
        public static final String COLUMN_NAME_ENVIAT = "enviat";
    }
    public static class Estacions implements BaseColumns {
        public static final String TABLE_NAME = "estacions";
        public static final String COLUMN_NAME_ID_EDUMET = "id_edumet";
        public static final String COLUMN_NAME_CODI = "codi";
        public static final String COLUMN_NAME_NOM = "nom";
        public static final String COLUMN_NAME_POBLACIO = "poblacio";
        public static final String COLUMN_NAME_LATITUD = "latitud";
        public static final String COLUMN_NAME_LONGITUD = "longitud";
        public static final String COLUMN_NAME_ALTITUD = "altitud";
        public static final String COLUMN_NAME_SITUACIO = "situacio";
        public static final String COLUMN_NAME_CLIMA = "clima";
        public static final String COLUMN_NAME_ESTACIO = "estacio";
    }
}


