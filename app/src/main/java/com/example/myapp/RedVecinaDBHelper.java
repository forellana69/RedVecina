package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Gestor de la base de datos local SQLite para la aplicación.
 * Se encarga de la creación, actualización y administración de las tablas locales,
 * permitiendo almacenar, recuperar y eliminar la información del perfil del usuario de forma persistente.
 *
 * @author Francisco
 * @version 1.0
 */
public class RedVecinaDBHelper extends SQLiteOpenHelper {

    /** Nombre físico del archivo de la base de datos en el almacenamiento interno. */
    private static final String NOMBRE_BD = "redvecina.db";

    /** Versión actual del esquema de la base de datos. */
    private static final int VERSION_BD = 1;

    /** Nombre de la tabla destinada a cachear los datos de perfil del usuario. */
    public static final String TABLA_PERFIL = "perfil_local";

    /** Columna identificadora única del usuario (Clave Primaria). */
    public static final String COLUMNA_USUARIO_ID = "usuario_id";

    /** Columna que almacena el nombre o alias del usuario. */
    public static final String COLUMNA_NOMBRE = "nombre";

    /** Columna que almacena el correo electrónico registrado. */
    public static final String COLUMNA_CORREO = "correo";

    /** Columna que guarda el rol o nivel de privilegios en el sistema. */
    public static final String COLUMNA_ROL = "rol";

    /** Columna que registra la marca de tiempo de la última actualización local en milisegundos. */
    public static final String COLUMNA_ACTUALIZADO = "actualizado_en";

    /**
     * Constructor del helper de base de datos.
     * Instancia el manejador configurando el contexto de la aplicación, el nombre de la BD y su versión.
     *
     * @param context El contexto de Android utilizado para abrir o crear la base de datos.
     */
    public RedVecinaDBHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /**
     * Se ejecuta automáticamente cuando la base de datos es creada por primera vez.
     * Se encarga de ejecutar la sentencia DDL para construir la estructura de la tabla de perfil.
     *
     * @param db Instancia de la base de datos sobre la cual se ejecutarán los scripts de creación.
     */
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

    /**
     * Se ejecuta de forma automática cuando se detecta un cambio en el número de versión de la base de datos.
     *
     * @param db              Instancia de la base de datos afectada.
     * @param versionAnterior Número identificador de la versión antigua del esquema.
     * @param versionNueva    Número identificador de la nueva versión del esquema.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {
    }

    /**
     * Inserta o actualiza un registro de perfil en la base de datos local.
     * Utiliza la estrategia de reemplazo en caso de conflicto con una clave primaria existente.
     *
     * @param usuarioId El identificador único del usuario. No puede ser nulo ni estar vacío.
     * @param nombre    El nombre público del usuario. No puede ser nulo ni estar vacío.
     * @param correo    La dirección de correo electrónico asociada al perfil.
     * @param rol       El rol asignado al usuario. No puede ser nulo ni estar vacío.
     * @return true si el perfil fue guardado de manera exitosa; false en caso contrario o ante parámetros inválidos.
     */
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

    /**
     * Consulta y extrae la información del perfil de un usuario a partir de su ID.
     *
     * @param usuarioId El identificador único del usuario que se desea buscar.
     * @return Un contenedor ContentValues cargado con las columnas del perfil si se encuentra el registro;
     *         null si no existen coincidencias o en caso de error de lectura.
     */
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

    /**
     * Remueve físicamente el registro de perfil local correspondiente al identificador provisto.
     *
     * @param usuarioId El identificador único del perfil que será removido de la tabla.
     * @return La cantidad de filas afectadas por la operación de borrado (debería ser 1 si tuvo éxito, o 0 si no existía).
     */
    public int eliminarPerfil(String usuarioId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLA_PERFIL,
                COLUMNA_USUARIO_ID + " = ?",
                new String[]{ usuarioId }
        );
    }
}