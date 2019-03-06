package com.edumet.observacions;

import android.provider.BaseColumns;

public final class DatabaseFeno {
    private DatabaseFeno() {}

    public static class Fenologies implements BaseColumns {
        public static final String TABLE_NAME_FENO = "fenologies";
        public static final String COLUMN_NAME_ID_FENO = "Id_feno";
        public static final String COLUMN_NAME_BLOC_FENO = "Bloc_feno";
        public static final String COLUMN_NAME_CODI_FENO = "Codi_feno";
        public static final String COLUMN_NAME_TITOL_FENO = "Titol_feno";
    }
}



