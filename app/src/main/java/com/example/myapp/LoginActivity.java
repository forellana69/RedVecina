package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        String correo = etCorreoLogin.getText().toString().trim();
        String contrasena =
                etContrasenaLogin.getText().toString();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(
                    this,
                    "Completa el correo y la contraseña.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Toast.makeText(
                this,
                "El inicio de sesión se conectará con Supabase en el siguiente paso.",
                Toast.LENGTH_LONG
        ).show();
    }
}