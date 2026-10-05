package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationHelper.configurarNavegacion(this);

        findViewById(R.id.btnMisDatos).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    activity_mis_datos.class
            );

            startActivity(intent);
        });


        findViewById(R.id.btnMisAvisos).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    activity_mis_avisos.class
            );

            startActivity(intent);
        });


        findViewById(R.id.btnMisParticipaciones).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    activity_mis_participaciones.class
            );

            startActivity(intent);
        });


        findViewById(R.id.btnConfiguracionContenido).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    activity_configuracion.class
            );

            startActivity(intent);
        });
    }
}