package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase utilitaria final encargada de centralizar la persistencia y gestión de la sesión.
 * Utiliza el almacenamiento local SharedPreferences para guardar, recuperar y limpiar de forma
 * segura los tokens de autenticación y el identificador único del usuario en el dispositivo.
 *
 * @author Francisco
 * @version 1.0
 */
public final class SessionManager {

    /** Nombre del archivo físico de SharedPreferences destinado a las credenciales de la sesión. */
    private static final String PREFS = "redvecina_sesion";

    /** Clave para almacenar y recuperar el token de acceso (Access Token). */
    private static final String ACCESS_TOKEN = "access_token";

    /** Clave para almacenar y recuperar el token de refresco (Refresh Token). */
    private static final String REFRESH_TOKEN = "refresh_token";

    /** Clave para almacenar y recuperar el identificador único del usuario (User ID). */
    private static final String USER_ID = "user_id";

    /**
     * Constructor privado para prevenir la instanciación de esta clase utilitaria.
     */
    private SessionManager() {
    }

    /**
     * Almacena las credenciales y tokens de la sesión activa de forma asíncrona.
     *
     * @param context      El contexto de Android necesario para acceder a las SharedPreferences.
     * @param accessToken  Token de autenticación de corta duración provisto por el servidor.
     * @param refreshToken Token de larga duración utilizado para renovar el Access Token.
     * @param userId       Identificador único del usuario autenticado en la plataforma.
     */
    public static void guardarSesion(
            Context context,
            String accessToken,
            String refreshToken,
            String userId) {

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

        prefs.edit()
                .putString(ACCESS_TOKEN, accessToken)
                .putString(REFRESH_TOKEN, refreshToken)
                .putString(USER_ID, userId)
                .apply();
    }

    /**
     * Recupera el token de acceso almacenado localmente en el terminal.
     *
     * @param context El contexto de Android necesario para acceder a las SharedPreferences.
     * @return El token de acceso en formato String si existe; null en caso contrario.
     */
    public static String obtenerAccessToken(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(ACCESS_TOKEN, null);
    }

    /**
     * Recupera el token de refresco almacenado localmente en el terminal.
     *
     * @param context El contexto de Android necesario para acceder a las SharedPreferences.
     * @return El token de refresco en formato String si existe; null en caso contrario.
     */
    public static String obtenerRefreshToken(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(REFRESH_TOKEN, null);
    }

    /**
     * Recupera el identificador único del usuario guardado en el almacenamiento local.
     *
     * @param context El contexto de Android necesario para acceder a las SharedPreferences.
     * @return El ID de usuario en formato String si existe; null en caso contrario.
     */
    public static String obtenerUserId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(USER_ID, null);
    }

    /**
     * Elimina físicamente todos los datos y tokens guardados en el archivo de sesión,
     * dejando al usuario en estado de desconexión absoluta.
     *
     * @param context El contexto de Android necesario para acceder a las SharedPreferences.
     */
    public static void cerrarSesion(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}