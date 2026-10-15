
package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreoLogin;
    private EditText etContrasenaLogin;
    private Button btnIngresar;
    private Button btnIrRegistro;

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