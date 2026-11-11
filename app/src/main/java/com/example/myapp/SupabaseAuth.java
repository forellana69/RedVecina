package com.example.myapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase utilitaria final encargada de gestionar los procesos de autenticación con el backend de Supabase.
 * Centraliza las peticiones de red asíncronas para el registro de nuevos usuarios y el inicio de sesión
 * utilizando un servicio de ejecución de un único hilo en segundo plano.
 *
 * @author Francisco
 * @version 1.0
 */
public final class SupabaseAuth {

    /** Servicio de ejecución configurado con un hilo único para procesar las llamadas de red en segundo plano. */
    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    /**
     * Constructor privado para prevenir la instanciación de esta clase utilitaria.
     */
    private SupabaseAuth() {
    }

    /**
     * Interfaz de retorno (Callback) destinada a capturar e interceptar los resultados de los flujos
     * de autenticación asíncronos.
     */
    public interface Callback {

        /**
         * Ejecuta las acciones correspondientes al recibir una respuesta exitosa y estructurada del servidor.
         *
         * @param response Objeto JSON cargado con los datos provistos por la API de Supabase.
         */
        void onSuccess(JSONObject response);

        /**
         * Ejecuta las acciones correspondientes ante la presencia de fallas de red, credenciales incorrectas o problemas de lectura.
         *
         * @param message Mensaje descriptivo con el motivo o causa detallada del fallo.
         */
        void onError(String message);
    }

    /**
     * Envía una solicitud asíncrona para registrar una nueva cuenta de usuario en el servidor.
     * Estructura un cuerpo JSON con el correo, contraseña y los metadatos de perfil correspondientes.
     *
     * @param nombre   Nombre completo o alias que se asociará al perfil del usuario.
     * @param correo   Dirección de correo electrónico que servirá como credencial de acceso.
     * @param contrasena Clave de seguridad para proteger el acceso a la cuenta.
     * @param rol      Rol comunitario asignado al usuario (ej. "vecino", "administrador").
     * @param callback Interfaz para interceptar y gestionar los eventos de éxito o error.
     */
    public static void registrar(
            String nombre,
            String correo,
            String contrasena,
            String rol,
            Callback callback) {

        try {
            JSONObject metadata = new JSONObject();
            metadata.put("nombre", nombre);
            metadata.put("rol", rol);

            JSONObject body = new JSONObject();
            body.put("email", correo);
            body.put("password", contrasena);
            body.put("data", metadata);

            ejecutarPeticion("/auth/v1/signup", body, callback);

        } catch (JSONException e) {
            callback.onError("No se pudieron preparar los datos del registro.");
        }
    }

    /**
     * Envía una solicitud asíncrona para validar credenciales e iniciar la sesión de un usuario.
     * Envía el correo y la contraseña al endpoint de intercambio de tokens mediante una petición POST.
     *
     * @param correo     Dirección de correo electrónico registrada del usuario.
     * @param contrasena Clave de seguridad asociada a la cuenta.
     * @param callback   Interfaz para interceptar y gestionar los eventos de éxito o error.
     */
    public static void iniciarSesion(
            String correo,
            String contrasena,
            Callback callback) {

        try {
            JSONObject body = new JSONObject();
            body.put("email", correo);
            body.put("password", contrasena);

            ejecutarPeticion(
                    "/auth/v1/token?grant_type=password",
                    body,
                    callback
            );

        } catch (JSONException e) {
            callback.onError("No se pudieron preparar los datos del inicio de sesión.");
        }
    }

    /**
     * Gestiona la apertura de las conexiones HTTP, inyecta las credenciales obligatorias
     * de la API, transmite los datos del cuerpo de la solicitud de forma asincrona y
     * retorna las respuestas correspondientes al callback.
     *
     * @param endpoint El segmento de ruta relativo que define el servicio destino en la API.
     * @param body     Estructura JSON que contiene los datos que seran transmitidos en la solicitud.
     * @param callback Interfaz destinada a retornar de forma segura los resultados de la operacion.
     */
    private static void ejecutarPeticion(
            String endpoint,
            JSONObject body,
            Callback callback) {

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;

            try {
                String baseUrl = SupabaseConfig.PROJECT_URL
                        .replaceAll("/+$", "");

                URL url = new URL(baseUrl + endpoint);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                byte[] requestBytes =
                        body.toString().getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBytes);
                }

                int status = connection.getResponseCode();

                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String responseText = leerRespuesta(stream);

                if (status < 200 || status >= 300) {
                    String message = extraerError(responseText);
                    throw new IOException(message);
                }

                JSONObject response = responseText.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseText);

                callback.onSuccess(response);

            } catch (Exception e) {
                callback.onError(mensajeAmigable(e));

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }



    /**
     * Consume el flujo de entrada (InputStream) de la conexión de red y ensambla
     * secuencialmente las líneas de texto obtenidas utilizando la codificación UTF-8.
     *
     * @param stream Flujo de datos crudos proveniente de la respuesta HTTP.
     * @return El contenido textual completo de la respuesta de red.
     * @throws IOException Si ocurre un fallo físico de lectura o cierre del flujo.
     */
    private static String leerRespuesta(InputStream stream)
            throws IOException {

        if (stream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    /**
     * Examina el cuerpo de la respuesta en busca de las estructuras de error comunes
     * de Supabase (recorriendo claves como msg, message, error_description y error)
     * para extraer el motivo técnico del fallo del backend.
     *
     * @param responseText Cadena de texto en formato JSON devuelta por el servidor.
     * @return El mensaje técnico descriptivo extraído del JSON, o un mensaje genérico por defecto si no es legible.
     */
    private static String extraerError(String responseText) {
        try {
            JSONObject json = new JSONObject(responseText);

            String[] campos = {
                    "msg",
                    "message",
                    "error_description",
                    "error"
            };

            for (String campo : campos) {
                String valor = json.optString(campo, "");

                if (!valor.isEmpty() && !"null".equals(valor)) {
                    return valor;
                }
            }

        } catch (JSONException ignored) {
        }

        return "Supabase rechazó la solicitud. Revisa la configuración.";
    }

    /**
     * Normaliza los códigos de error de red y los mensajes nativos en inglés emitidos por la API de Supabase,
     * transformándolos en explicaciones claras, amigables y legibles para el usuario final.
     *
     * @param e Excepción capturada durante el proceso de conexión, lectura o autenticación.
     * @return Un mensaje explicativo adaptado e interpretado en español.
     */
    private static String mensajeAmigable(Exception e) {
        String mensaje = e.getMessage();

        if (mensaje == null) {
            return "No se pudo conectar con Supabase.";
        }

        String normalizado = mensaje.toLowerCase();

        if (normalizado.contains("invalid login credentials")) {
            return "El correo o la contraseña son incorrectos.";
        }

        if (normalizado.contains("user already registered")
                || normalizado.contains("already registered")) {
            return "Ya existe una cuenta registrada con ese correo.";
        }

        if (normalizado.contains("email not confirmed")) {
            return "Debes confirmar tu correo electrónico antes de ingresar.";
        }

        if (normalizado.contains("password")) {
            return "Supabase rechazó la contraseña. Revisa los requisitos.";
        }

        if (e instanceof java.net.UnknownHostException
                || normalizado.contains("unable to resolve host")) {
            return "No se pudo encontrar el servidor. Revisa la URL y tu conexión.";
        }

        return mensaje;
    }
}