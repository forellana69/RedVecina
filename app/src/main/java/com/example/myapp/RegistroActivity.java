
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

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombreRegistro;
    private EditText etCorreoRegistro;
    private EditText etContrasenaRegistro;
    private RadioGroup rgTipoUsuario;
    private TextView tvDescripcionRol;
    private Button btnCrearCuenta;
    private Button btnVolverLogin;

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