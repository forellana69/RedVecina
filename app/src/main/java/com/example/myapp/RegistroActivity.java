package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

/**
 * Actividad encargada de gestionar el registro de nuevos usuarios en la plataforma.
 * Provee la interfaz para ingresar credenciales, elegir el rol dentro de la comunidad
 * (Administrador o Vecino), validar las entradas y dar de alta la cuenta en el backend.
 *
 * @author Francisco
 * @version 1.0
 */
public class RegistroActivity extends AppCompatActivity {

    /** Campo de texto para ingresar el nombre completo del usuario. */
    private EditText etNombreRegistro;

    /** Campo de texto para ingresar el correo electrónico de la cuenta. */
    private EditText etCorreoRegistro;

    /** Campo de texto para ingresar la contraseña de seguridad. */
    private EditText etContrasenaRegistro;

    /** Grupo de botones de opción (RadioButtons) para alternar la selección del rol. */
    private RadioGroup rgTipoUsuario;

    /** Etiqueta informativa que describe dinámicamente el alcance del rol seleccionado. */
    private TextView tvDescripcionRol;

    /** Botón que procesa y gatilla el flujo de inscripción de la cuenta. */
    private Button btnCrearCuenta;

    /** Enlace de texto interactivo para retroceder e ir a la interfaz de login. */
    private TextView btnVolverLogin;

    /**
     * Inicializa la actividad, vincula los componentes de la interfaz de usuario,
     * configura los cambios dinámicos del rol de usuario y gestiona los clics de interacción.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombreRegistro = findViewById(R.id.etNombreRegistro);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etContrasenaRegistro = findViewById(R.id.etContrasenaRegistro);
        rgTipoUsuario = findViewById(R.id.rgTipoUsuario);
        tvDescripcionRol = findViewById(R.id.tvDescripcionRol);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        rgTipoUsuario.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAdministrador) {
                tvDescripcionRol.setText(
                        "Podrás crear una junta vecinal y gestionar sus solicitudes."
                );
            } else {
                tvDescripcionRol.setText(
                        "Podrás solicitar ingreso a una junta vecinal."
                );
            }
        });

        btnCrearCuenta.setOnClickListener(v -> registrarUsuario());

        btnVolverLogin.setOnClickListener(v -> finish());
    }

    /**
     * Captura las entradas de texto de la interfaz, valida que se cumplan las reglas de
     * negocio básicas (campos completos, formato de e-mail y longitud mínima de clave)
     * e inicia la petición asíncrona hacia el servicio de autenticación remoto.
     */
    private void registrarUsuario() {
        String nombre = etNombreRegistro.getText()
                .toString().trim();

        String correo = etCorreoRegistro.getText()
                .toString().trim();

        String contrasena = etContrasenaRegistro.getText()
                .toString();

        int rolSeleccionado = rgTipoUsuario.getCheckedRadioButtonId();

        if (nombre.isEmpty() || correo.isEmpty()
                || contrasena.isEmpty()) {
            Toast.makeText(
                    this,
                    "Completa todos los campos.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoRegistro.setError("Introduce un correo válido.");
            return;
        }

        if (contrasena.length() < 6) {
            etContrasenaRegistro.setError(
                    "Usa al menos 6 caracteres."
            );
            return;
        }

        if (rolSeleccionado == -1) {
            Toast.makeText(
                    this,
                    "Selecciona un tipo de usuario.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String rol = rolSeleccionado == R.id.rbAdministrador
                ? "administrador"
                : "vecino";

        btnCrearCuenta.setEnabled(false);

        SupabaseAuth.registrar(
                nombre,
                correo,
                contrasena,
                rol,
                new SupabaseAuth.Callback() {

                    /**
                     * Procesa la respuesta favorable del servidor ante la creación de la cuenta.
                     * Si se devuelven tokens de sesión automáticos, inicia la sesión en el terminal y
                     * navega a la pantalla principal; de lo contrario, le pide al usuario confirmar vía e-mail.
                     *
                     * @param response Objeto JSON devuelto por la API con el estado e información de la cuenta.
                     */
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            String accessToken =
                                    response.optString("access_token", "");

                            JSONObject user = response.optJSONObject("user");

                            String userId = user == null
                                    ? response.optString("id", "")
                                    : user.optString("id", "");

                            if (!accessToken.isEmpty()
                                    && !userId.isEmpty()) {

                                String refreshToken =
                                        response.optString(
                                                "refresh_token", ""
                                        );

                                SessionManager.guardarSesion(
                                        RegistroActivity.this,
                                        accessToken,
                                        refreshToken,
                                        userId
                                );

                                Toast.makeText(
                                        RegistroActivity.this,
                                        "Cuenta creada correctamente.",
                                        Toast.LENGTH_LONG
                                ).show();

                                Intent intent = new Intent(
                                        RegistroActivity.this,
                                        MainActivity.class
                                );

                                startActivity(intent);
                                finish();
                                return;
                            }

                            // Supabase puede exigir confirmación
                            // del correo antes de crear una sesión.
                            Toast.makeText(
                                    RegistroActivity.this,
                                    "Cuenta creada. Revisa tu correo y confirma la cuenta antes de ingresar.",
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent = new Intent(
                                    RegistroActivity.this,
                                    LoginActivity.class
                            );

                            startActivity(intent);
                            finish();
                        });
                    }

                    /**
                     * Gestiona las fallas globales ocurridas durante el proceso de registro de la cuenta.
                     * Reactiva el botón de creación de cuenta para permitir nuevos intentos y despliega
                     * un mensaje Toast con el motivo exacto del error.
                     *
                     * @param message Mensaje descriptivo con la causa de la falla devuelta por el servidor.
                     */
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            btnCrearCuenta.setEnabled(true);

                            Toast.makeText(
                                    RegistroActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }
}