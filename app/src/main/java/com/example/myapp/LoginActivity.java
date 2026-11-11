
package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

/**
 * Actividad encargada de gestionar el inicio de sesión y la autenticación de los usuarios.
 * Provee la verificación de credenciales, la persistencia de la sesión activa en el dispositivo
 * y la redirección hacia el módulo de registro o la pantalla principal.
 *
 * @author Francisco
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity {

    /** Campo de texto destinado a la entrada del correo electrónico del usuario. */
    private EditText etCorreoLogin;

    /** Campo de texto destinado a la entrada de la contraseña de la cuenta. */
    private EditText etContrasenaLogin;

    /** Botón que gatilla la lógica de validación e inicio de sesión. */
    private Button btnIngresar;

    /** Botón que redirige al usuario hacia la pantalla de registro de cuentas. */
    private Button btnIrRegistro;

    /**
     * Inicializa la actividad, vincula las vistas de la interfaz gráfica, define las
     * acciones al pulsar los botones e invoca el intento automático de restauración de sesión.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCorreoLogin = findViewById(R.id.etCorreoLogin);
        etContrasenaLogin = findViewById(R.id.etContrasenaLogin);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnIngresar.setOnClickListener(v -> iniciarSesion());

        btnIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroActivity.class
            );
            startActivity(intent);
        });

        restaurarSesion();
    }

    /**
     * Verifica la existencia de credenciales previamente guardadas de forma local. Si el token
     * y el identificador son válidos, intenta sincronizar el perfil con el backend en segundo plano
     * y redirige de forma directa a la actividad principal de la aplicación.
     */
    private void restaurarSesion() {
        String accessToken =
                SessionManager.obtenerAccessToken(this);

        String usuarioId =
                SessionManager.obtenerUserId(this);

        if (accessToken == null || accessToken.isEmpty()
                || usuarioId == null || usuarioId.isEmpty()) {
            return;
        }

        btnIngresar.setEnabled(false);

        SupabaseData.obtenerPerfil(
                usuarioId,
                accessToken,
                null,
                new SupabaseData.PerfilCallback() {

                    @Override
                    public void onSuccess(
                            String nombre,
                            String correoPerfil,
                            String rol) {

                        runOnUiThread(() -> {
                            RedVecinaDBHelper dbHelper =
                                    new RedVecinaDBHelper(
                                            LoginActivity.this);

                            boolean guardado;

                            try {
                                android.content.ContentValues perfilLocal =
                                        dbHelper.obtenerPerfil(usuarioId);

                                String correoLocal = null;

                                if (perfilLocal != null) {
                                    correoLocal = perfilLocal.getAsString(
                                            RedVecinaDBHelper.COLUMNA_CORREO);
                                }

                                guardado = dbHelper.guardarPerfil(
                                        usuarioId,
                                        nombre,
                                        correoLocal,
                                        rol
                                );

                            } finally {
                                dbHelper.close();
                            }

                            if (!guardado) {
                                btnIngresar.setEnabled(true);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "No se pudo actualizar el perfil local.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();
                        });
                    }


                    /**
                     * Intercepta e informa los errores ocurridos durante la restauración de sesión.
                     * Si la respuesta corresponde a un error de credenciales vencidas (401), se limpian
                     * los tokens locales y se solicita reautenticación explícita.
                     *
                     * @param mensaje Detalle técnico descriptivo del fallo devuelto por el servidor.
                     */
                    @Override
                    public void onError(String mensaje) {
                        runOnUiThread(() -> {
                            btnIngresar.setEnabled(true);

                            if (mensaje != null
                                    && mensaje.contains("código 401")) {
                                SessionManager.cerrarSesion(
                                        LoginActivity.this);
                            }

                            Toast.makeText(
                                    LoginActivity.this,
                                    "No se pudo restaurar la sesión. "
                                            + "Inicia sesión nuevamente si es "
                                            + "necesario. Detalle: " + mensaje,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    /**
     * Valida las entradas de texto del usuario (presencia de campos vacíos y formato correcto de correo),
     * bloquea temporalmente el botón de ingreso y delega el intento de autenticación en los servicios remotos.
     */
    private void iniciarSesion() {
        String correo = etCorreoLogin.getText()
                .toString().trim();

        String contrasena = etContrasenaLogin.getText()
                .toString();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(
                    this,
                    "Completa el correo y la contraseña.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoLogin.setError("Introduce un correo válido.");
            return;
        }

        btnIngresar.setEnabled(false);

        SupabaseAuth.iniciarSesion(
                correo,
                contrasena,
                new SupabaseAuth.Callback() {

                    /**
                     * Procesa la respuesta exitosa del servidor de autenticación. Extrae los tokens
                     * de acceso/refresco junto con el ID del usuario, los almacena localmente mediante
                     * el SessionManager e inicia la consulta para descargar y sincronizar el perfil.
                     *
                     * @param response Objeto JSON devuelto por el servidor con las credenciales de la sesión.
                     */
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            try {
                                String accessToken = response.optString("access_token", "");
                                String refreshToken = response.optString("refresh_token", "");

                                JSONObject usuario = response.optJSONObject("user");

                                if (usuario == null) {
                                    throw new Exception(
                                            "Supabase no devolvió los datos del usuario.");
                                }

                                String usuarioId = usuario.optString("id", "");

                                if (accessToken.isEmpty() || usuarioId.isEmpty()) {
                                    throw new Exception(
                                            "La respuesta de inicio de sesión está incompleta.");
                                }

                                SessionManager.guardarSesion(
                                        LoginActivity.this,
                                        accessToken,
                                        refreshToken,
                                        usuarioId
                                );

                                SupabaseData.obtenerPerfil(
                                        usuarioId,
                                        accessToken,
                                        correo,
                                        new SupabaseData.PerfilCallback() {

                                            /**
                                             * Callback interno gatillado cuando el perfil es recuperado remotamente de forma exitosa.
                                             * Almacena de manera segura la información en la base de datos local SQLite y redirige al
                                             * usuario hacia la interfaz principal (MainActivity).
                                             *
                                             * @param nombre       Nombre público o alias del usuario del perfil.
                                             * @param correoPerfil Dirección de correo electrónico asociada a la cuenta.
                                             * @param rol          Nivel de permisos o rol asignado al usuario dentro de la comunidad.
                                             */
                                            @Override
                                            public void onSuccess(
                                                    String nombre,
                                                    String correoPerfil,
                                                    String rol) {

                                                runOnUiThread(() -> {
                                                    RedVecinaDBHelper dbHelper =
                                                            new RedVecinaDBHelper(
                                                                    LoginActivity.this);

                                                    boolean guardado;

                                                    try {
                                                        guardado = dbHelper.guardarPerfil(
                                                                usuarioId,
                                                                nombre,
                                                                correoPerfil,
                                                                rol
                                                        );
                                                    } finally {
                                                        dbHelper.close();
                                                    }

                                                    if (!guardado) {
                                                        btnIngresar.setEnabled(true);

                                                        Toast.makeText(
                                                                LoginActivity.this,
                                                                "No se pudo guardar el perfil local.",
                                                                Toast.LENGTH_LONG
                                                        ).show();

                                                        return;
                                                    }

                                                    Toast.makeText(
                                                            LoginActivity.this,
                                                            "Inicio de sesión correcto.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    Intent intent = new Intent(
                                                            LoginActivity.this,
                                                            MainActivity.class
                                                    );

                                                    startActivity(intent);
                                                    finish();
                                                });
                                            }

                                            /**
                                             * Callback interno gatillado ante la presencia de errores en la descarga del perfil remoto.
                                             * Desbloquea la interfaz de usuario para permitir un nuevo intento e informa el motivo.
                                             *
                                             * @param mensaje Detalle del error arrojado por el servicio de base de datos.
                                             */
                                            @Override
                                            public void onError(String mensaje) {
                                                runOnUiThread(() -> {
                                                    btnIngresar.setEnabled(true);

                                                    Toast.makeText(
                                                            LoginActivity.this,
                                                            "No se pudo cargar el perfil: "
                                                                    + mensaje,
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                });
                                            }
                                        }
                                );

                            } catch (Exception e) {
                                btnIngresar.setEnabled(true);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Error al procesar el inicio de sesión: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }

                    /**
                     * Gestiona las fallas globales durante la autenticación de credenciales (por ejemplo, contraseña errónea).
                     * Reactiva el botón de ingreso y despliega una notificación informativa con la descripción del problema.
                     *
                     * @param message Mensaje descriptivo con la causa de la denegación del servicio.
                     */
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            btnIngresar.setEnabled(true);

                            Toast.makeText(
                                    LoginActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }
}
