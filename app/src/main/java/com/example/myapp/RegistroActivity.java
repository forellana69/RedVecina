package com.example.myapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombreRegistro;
    private EditText etCorreoRegistro;
    private EditText etContrasenaRegistro;

    private RadioGroup rgTipoUsuario;
    private TextView tvDescripcionRol;
    private TextView btnVolverLogin;
    private Button btnCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombreRegistro = findViewById(R.id.etNombreRegistro);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etContrasenaRegistro =
                findViewById(R.id.etContrasenaRegistro);

        rgTipoUsuario = findViewById(R.id.rgTipoUsuario);
        tvDescripcionRol = findViewById(R.id.tvDescripcionRol);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        rgTipoUsuario.setOnCheckedChangeListener(
                (group, checkedId) -> actualizarDescripcionRol()
        );

        btnVolverLogin.setOnClickListener(v -> finish());

        btnCrearCuenta.setOnClickListener(v -> validarRegistro());

        actualizarDescripcionRol();
    }

    private void actualizarDescripcionRol() {
        int rolSeleccionado = rgTipoUsuario.getCheckedRadioButtonId();

        if (rolSeleccionado == R.id.rbAdministrador) {
            tvDescripcionRol.setText(
                    "Como administrador, podrás crear una junta " +
                            "de vecinos y gestionar las solicitudes de ingreso."
            );
        } else {
            tvDescripcionRol.setText(
                    "Como vecino, podrás solicitar unirte a una junta " +
                            "después de crear tu cuenta. Por defecto, no " +
                            "pertenecerás a ninguna junta."
            );
        }
    }

    private void validarRegistro() {
        String nombre =
                etNombreRegistro.getText().toString().trim();

        String correo =
                etCorreoRegistro.getText().toString().trim();

        String contrasena =
                etContrasenaRegistro.getText().toString();

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

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(correo).matches()) {
            etCorreoRegistro.setError(
                    "Ingresa un correo electrónico válido."
            );
            etCorreoRegistro.requestFocus();
            return;
        }

        if (contrasena.length() < 6) {
            etContrasenaRegistro.setError(
                    "La contraseña debe tener al menos 6 caracteres."
            );
            etContrasenaRegistro.requestFocus();
            return;
        }

        if (rolSeleccionado == -1) {
            Toast.makeText(
                    this,
                    "Selecciona un tipo de cuenta.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String rol;

        if (rolSeleccionado == R.id.rbAdministrador) {
            rol = "administrador";
        } else {
            rol = "vecino";
        }

        Toast.makeText(
                this,
                "Formulario válido. Tipo de cuenta: " + rol +
                        ". En el siguiente paso conectaremos Supabase.",
                Toast.LENGTH_LONG
        ).show();
    }
}