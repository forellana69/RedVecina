
package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RedVecinaDBHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "redvecina.db";
    private static final int VERSION_BD = 1;

    public static final String TABLA_PERFIL = "perfil_local";

    public static final String COLUMNA_USUARIO_ID = "usuario_id";
    public static final String COLUMNA_NOMBRE = "nombre";
    public static final String COLUMNA_CORREO = "correo";
    public static final String COLUMNA_ROL = "rol";
    public static final String COLUMNA_ACTUALIZADO = "actualizado_en";

    public RedVecinaDBHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTablaPerfil = "CREATE TABLE " + TABLA_PERFIL + " ("
                + COLUMNA_USUARIO_ID + " TEXT PRIMARY KEY NOT NULL, "
                + COLUMNA_NOMBRE + " TEXT NOT NULL, "
                + COLUMNA_CORREO + " TEXT, "
                + COLUMNA_ROL + " TEXT NOT NULL, "
                + COLUMNA_ACTUALIZADO + " INTEGER NOT NULL"
                + ")";

        db.execSQL(crearTablaPerfil);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {
    }

    public boolean guardarPerfil(String usuarioId, String nombre,
                                 String correo, String rol) {

        if (usuarioId == null || usuarioId.trim().isEmpty()
                || nombre == null || nombre.trim().isEmpty()
                || rol == null || rol.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(COLUMNA_USUARIO_ID, usuarioId);
        valores.put(COLUMNA_NOMBRE, nombre);
        valores.put(COLUMNA_CORREO, correo);
        valores.put(COLUMNA_ROL, rol);
        valores.put(COLUMNA_ACTUALIZADO, System.currentTimeMillis());

        long resultado = db.insertWithOnConflict(
                TABLA_PERFIL,
                null,
                valores,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        return resultado != -1;
    }

    public ContentValues obtenerPerfil(String usuarioId) {

        SQLiteDatabase db = getReadableDatabase();

        String[] columnas = {
                COLUMNA_USUARIO_ID,
                COLUMNA_NOMBRE,
                COLUMNA_CORREO,
                COLUMNA_ROL,
                COLUMNA_ACTUALIZADO
        };

        String[] argumentos = { usuarioId };

        try (Cursor cursor = db.query(
                TABLA_PERFIL,
                columnas,
                COLUMNA_USUARIO_ID + " = ?",
                argumentos,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                ContentValues perfil = new ContentValues();

                perfil.put(COLUMNA_USUARIO_ID,
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                COLUMNA_USUARIO_ID)));

                perfil.put(COLUMNA_NOMBRE,
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                COLUMNA_NOMBRE)));

                perfil.put(COLUMNA_CORREO,
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                COLUMNA_CORREO)));

                perfil.put(COLUMNA_ROL,
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                COLUMNA_ROL)));

                perfil.put(COLUMNA_ACTUALIZADO,
                        cursor.getLong(cursor.getColumnIndexOrThrow(
                                COLUMNA_ACTUALIZADO)));

                return perfil;
            }
        }

        return null;
    }

    public int eliminarPerfil(String usuarioId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLA_PERFIL,
                COLUMNA_USUARIO_ID + " = ?",
                new String[]{ usuarioId }
        );
    }
}
