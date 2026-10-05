package com.example.myapp;

import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EmergenciaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emergencia);

        NavigationHelper.configurarNavegacion(this);

        findViewById(R.id.btnEnviarAlerta)
                .setOnClickListener(v -> mostrarConfirmacion());

    }

    private void mostrarConfirmacion() {

        new AlertDialog.Builder(this)
                .setTitle("Enviar alerta")
                .setMessage(
                        "¿Estás seguro de que deseas enviar una alerta de emergencia?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Enviar",
                        (dialog, which) -> {

                            Toast.makeText(
                                    this,
                                    "Alerta preparada para enviar",
                                    Toast.LENGTH_LONG
                            ).show();

                        }
                )
                .show();
    }
}