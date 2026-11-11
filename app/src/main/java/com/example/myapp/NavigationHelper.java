package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * Clase de utilidad (Helper) encargada de centralizar y unificar la lógica del menú
 * o barra de navegación compartida en toda la aplicación.
 * Evita la duplicación de código vinculando dinámicamente las acciones de clic en los
 * componentes comunes de las distintas actividades.
 *
 * @author Francisco
 * @version 1.0
 */
public class NavigationHelper {

    /**
     * Busca los elementos visuales de la barra de navegación presentes en la actividad provista,
     * y si existen, les asigna los escuchadores de clics necesarios para redirigir al usuario
     * a los módulos de Chat, Avisos, Emergencia, Debate y Perfil.
     *
     * @param activity La instancia de la actividad actual sobre la cual se buscarán las vistas
     *                 de navegación y se ejecutarán las transiciones de pantalla (Intents).
     */
    public static void configurarNavegacion(Activity activity) {

        // CHAT
        View navChat = activity.findViewById(R.id.navChat);

        if (navChat != null) {
            navChat.setOnClickListener(v -> {

                Intent intent = new Intent(activity, ChatActivity.class);
                activity.startActivity(intent);
            });
        }

        View navAvisos = activity.findViewById(R.id.navAvisos);

        if (navAvisos != null) {
            navAvisos.setOnClickListener(v -> {

                Intent intent = new Intent(activity, AvisosActivity.class);
                activity.startActivity(intent);
            });
        }

        View navEmergencia = activity.findViewById(R.id.navEmergencia);

        if (navEmergencia != null) {
            navEmergencia.setOnClickListener(v -> {

                Intent intent = new Intent(activity, EmergenciaActivity.class);
                activity.startActivity(intent);
            });
        }

        View navDebate = activity.findViewById(R.id.navDebate);

        if (navDebate != null) {
            navDebate.setOnClickListener(v -> {

                Intent intent = new Intent(activity, DebateActivity.class);
                activity.startActivity(intent);
            });
        }

        View navPerfil = activity.findViewById(R.id.navPerfil);

        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {

                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
            });
        }
    }
}
