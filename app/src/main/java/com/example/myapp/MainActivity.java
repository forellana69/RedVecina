package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad principal y panel de control (Dashboard) de la aplicación.
 * Actúa como el centro neurálgico que conecta al usuario con sus datos de perfil,
 * sus publicaciones de avisos, el registro de participaciones y los ajustes de configuración,
 * aplicando además la barra de navegación compartida.
 *
 * @author Francisco
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Inicializa la actividad, establece el diseño de la interfaz gráfica y configura
     * los listeners de cada botón para permitir el direccionamiento hacia los diferentes
     * módulos de la aplicación.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
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