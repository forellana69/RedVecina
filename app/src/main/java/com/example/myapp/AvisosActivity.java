package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AvisosActivity extends AppCompatActivity {

    private Button btnNuevoAviso;

    private LinearLayout avisoReunion;
    private LinearLayout avisoActividad;
    private LinearLayout avisoInformacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos);

        btnNuevoAviso = findViewById(R.id.btnNuevoAviso);

        avisoReunion = findViewById(R.id.avisoReunion);
        avisoActividad = findViewById(R.id.avisoActividad);
        avisoInformacion = findViewById(R.id.avisoInformacion);

        NavigationHelper.configurarNavegacion(this);

        btnNuevoAviso.setOnClickListener(v ->
                Toast.makeText(
                        AvisosActivity.this,
                        "La publicación de avisos se implementará próximamente.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        avisoReunion.setOnClickListener(v ->
                mostrarAviso(
                        "Reunión de vecinos",
                        "Invitamos a todos los vecinos a participar en la próxima reunión comunitaria."
                )
        );

        avisoActividad.setOnClickListener(v ->
                mostrarAviso(
                        "Jornada de limpieza",
                        "La comunidad está invitada a colaborar en la limpieza de los espacios comunes."
                )
        );

        avisoInformacion.setOnClickListener(v ->
                mostrarAviso(
                        "Recordatorio comunitario",
                        "Mantén limpios los espacios compartidos y respeta los horarios de descanso."
                )
        );
    }

    private void mostrarAviso(String titulo, String mensaje) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }
}