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

/**
 * Clase utilitaria final encargada de la gestión, consulta y sincronización de los datos
 * de la base de datos de Supabase (PostgreSQL/REST API).
 * Ejecuta operaciones de lectura y escritura en segundo plano utilizando un pool de hilos de ejecución único.
 *
 * @author Francisco
 * @version 1.0
 */
public final class SupabaseData {

    /** Servicio de ejecución configurado con un hilo único para procesar las consultas de datos en segundo plano. */
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    /**
     * Constructor privado para prevenir la instanciación de esta clase utilitaria.
     */
    private SupabaseData() {
    }

    /**
     * Interfaz de retorno (Callback) destinada a capturar e interceptar los resultados de los flujos
     * de consulta y descarga asíncrona de perfiles de usuario.
     */
    public interface PerfilCallback {

        /**
         * Ejecuta las acciones correspondientes cuando el perfil se recupera de forma exitosa
         * y se procesan sus datos de manera correcta.
         *
         * @param nombre Nombre público o alias del usuario registrado en la base de datos.
         * @param correo Dirección de correo electrónico asociada al perfil o a la sesión activa.
         * @param rol    Nivel de privilegios o rol asignado al usuario dentro de la plataforma (ej. "vecino", "administrador").
         */
        void onSuccess(String nombre, String correo, String rol);

        /**
         * Ejecuta las acciones correspondientes ante la presencia de fallas de conexión,
         * códigos HTTP erróneos o fallos en las políticas de seguridad.
         *
         * @param mensaje Detalle explicativo o técnico con la causa del problema devuelto.
         */
        void onError(String mensaje);
    }

    /**
     * Realiza una petición HTTP GET asíncrona a la API REST de Supabase para obtener las columnas
     * de perfil correspondientes al ID de usuario proporcionado, adjuntando las cabeceras de autorización Bearer.
     *
     * @param usuarioId    El identificador único del usuario que se desea consultar.
     * @param accessToken  Token JWT de acceso activo para validar los permisos en las políticas RLS.
     * @param correoSesion Correo electrónico del usuario que será retransmitido de vuelta al callback de éxito.
     * @param callback     Interfaz destinada a retornar los resultados de éxito o error al hilo llamador.
     */
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
