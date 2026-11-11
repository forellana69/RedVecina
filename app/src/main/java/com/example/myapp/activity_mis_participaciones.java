package com.example.myapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad encargada de gestionar y mostrar la pantalla de las participaciones del usuario.
 * Permite visualizar las intervenciones o colaboraciones registradas y aplica la lógica de navegación global.
 *
 * @author Francisco
 * @version 1.0
 */
public class activity_mis_participaciones extends AppCompatActivity {

    /**
     * Inicializa la actividad, establece el diseño de la interfaz de usuario
     * y configura los componentes de navegación.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mis_participaciones);

        NavigationHelper.configurarNavegacion(this);
    }
}