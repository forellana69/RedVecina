package com.example.myapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad encargada de gestionar y mostrar la pantalla de debates de la aplicación.
 * Permite a los usuarios visualizar los hilos de discusión y participar en los debates comunitarios.
 *
 * @author Francisco
 * @version 1.0
 */
public class DebateActivity extends AppCompatActivity {

    /**
     * Inicializa la actividad y establece el diseño de la interfaz de usuario (layout).
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debate);
    }
}