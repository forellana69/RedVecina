
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
                            String accessToken =
                                    response.optString("access_token", "");

                            String refreshToken =
                                    response.optString("refresh_token", "");

                            JSONObject user =
                                    response.optJSONObject("user");

                            String userId = user == null
                                    ? ""
                                    : user.optString("id", "");

                            if (accessToken.isEmpty()
                                    || userId.isEmpty()) {
                                btnIngresar.setEnabled(true);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Supabase no devolvió una sesión válida.",
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            SessionManager.guardarSesion(
                                    LoginActivity.this,
                                    accessToken,
                                    refreshToken,
                                    userId
                            );

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