
package com.example.myapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SupabaseData {

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SupabaseData() {
    }

    public interface PerfilCallback {
        void onSuccess(String nombre, String correo, String rol);

        void onError(String mensaje);
    }


    public static void obtenerPerfil(
            String usuarioId,
            String accessToken,
            String correoSesion,
            PerfilCallback callback) {

        executor.execute(() -> {

            HttpURLConnection conexion = null;

            try {
                if (usuarioId == null || usuarioId.trim().isEmpty()
                        || accessToken == null
                        || accessToken.trim().isEmpty()) {
                    callback.onError(
                            "Falta el identificador o el token de sesión."
                    );
                    return;
                }

                String urlTexto = SupabaseConfig.PROJECT_URL
                        + "/rest/v1/perfiles"
                        + "?id=eq." + usuarioId
                        + "&select=id,nombre,rol";

                URL url = new URL(urlTexto);
                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(15000);
                conexion.setReadTimeout(15000);

                conexion.setRequestProperty(
                        "apikey", SupabaseConfig.PUBLISHABLE_KEY);
                conexion.setRequestProperty(
                        "Authorization", "Bearer " + accessToken);
                conexion.setRequestProperty(
                        "Accept", "application/json");

                int codigoRespuesta = conexion.getResponseCode();

                InputStream flujo = codigoRespuesta >= 200
                        && codigoRespuesta < 300
                        ? conexion.getInputStream()
                        : conexion.getErrorStream();

                StringBuilder respuesta = new StringBuilder();

                if (flujo != null) {
                    try (BufferedReader lector = new BufferedReader(
                            new InputStreamReader(
                                    flujo, StandardCharsets.UTF_8))) {

                        String linea;
                        while ((linea = lector.readLine()) != null) {
                            respuesta.append(linea);
                        }
                    }
                }

                if (codigoRespuesta < 200 || codigoRespuesta >= 300) {
                    callback.onError(
                            "Supabase respondió con el código "
                                    + codigoRespuesta + ". Detalle: "
                                    + respuesta);
                    return;
                }

                JSONArray perfiles = new JSONArray(respuesta.toString());

                if (perfiles.length() == 0) {
                    callback.onError(
                            "No se encontró el perfil. Comprueba que "
                                    + "exista en Supabase y que las políticas "
                                    + "RLS permitan al usuario leerlo.");
                    return;
                }

                JSONObject perfil = perfiles.getJSONObject(0);
                String nombre = perfil.optString("nombre", "");
                String rol = perfil.optString("rol", "");

                if (nombre.trim().isEmpty() || rol.trim().isEmpty()) {
                    callback.onError(
                            "El perfil no contiene un nombre o rol válido.");
                    return;
                }

                callback.onSuccess(nombre, correoSesion, rol);

            } catch (Exception e) {
                callback.onError(
                        "No se pudo obtener el perfil: " + e.getMessage());

            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        });
    }
}
