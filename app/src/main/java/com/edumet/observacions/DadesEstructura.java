package com.edumet.observacions;

import android.provider.BaseColumns;

/**
 * Created by 40980055N on 18/10/17.
 */

public final class DadesEstructura {
    // To prevent someone from accidentally instantiating the contract class, make the constructor private.
    private DadesEstructura() {}
    /* Inner class that defines the table contents */
    public static class Parametres implements BaseColumns {
        public static final String TABLE_NAME = "pendents_enviar";
        public static final String COLUMN_NAME_LATITUD = "latitud";
        public static final String COLUMN_NAME_LONGITUD = "longitud";
        public static final String COLUMN_NAME_FENOMEN = "fenomen";
        public static final String COLUMN_NAME_DESCRIPCIO = "descripcio";
        public static final String COLUMN_NAME_PATH = "path";
    }
}


