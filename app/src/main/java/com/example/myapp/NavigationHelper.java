package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class NavigationHelper {

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