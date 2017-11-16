package com.edumet.observacions;

import android.provider.BaseColumns;

public final class DadesEstacions {
    private DadesEstacions() {}
    public static class Parametres implements BaseColumns {
        public static final String TABLE_NAME = "estacions";
        public static final String COLUMN_NAME_ID_EDUMET = "id_edumet";
        public static final String COLUMN_NAME_CODI = "codi";
        public static final String COLUMN_NAME_NOM = "nom";
        public static final String COLUMN_NAME_POBLACIO = "poblacio";
        public static final String COLUMN_NAME_LATITUD = "latitud";
        public static final String COLUMN_NAME_LONGITUD = "longitud";
        public static final String COLUMN_NAME_ALTITUD = "altitud";
        public static final String COLUMN_NAME_SITUACIO = "situacio";
        public static final String COLUMN_NAME_ESTACIO = "estacio";
        public static final String COLUMN_NAME_CLIMA = "clima";
    }
}
