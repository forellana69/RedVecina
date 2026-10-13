
package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    private static final String PREFS = "redvecina_sesion";

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_ID = "user_id";

    private SessionManager() {
    }

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

    public static String obtenerAccessToken(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(ACCESS_TOKEN, null);
    }

    public static String obtenerRefreshToken(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(REFRESH_TOKEN, null);
    }

    public static String obtenerUserId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(USER_ID, null);
    }

    public static void cerrarSesion(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}