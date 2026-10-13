
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

public final class SupabaseAuth {

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private SupabaseAuth() {
    }

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String message);
    }

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