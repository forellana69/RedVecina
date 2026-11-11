package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad encargada de gestionar y mostrar la lista de chats o conversaciones del usuario.
 * Permite visualizar interacciones grupales e individuales, provee acceso a futuros chats
 * y aplica la lógica de navegación global.
 *
 * @author Francisco
 * @version 1.0
 */
public class ChatActivity extends AppCompatActivity {

    /** Botón destinado a la creación e inicio de una nueva conversación. */
    private Button btnNuevoChat;

    /** Contenedor interactivo que representa el acceso al chat con Ana González. */
    private LinearLayout chatAna;

    /** Contenedor interactivo que representa el acceso al chat grupal de la Junta de Vecinos. */
    private LinearLayout chatJunta;

    /** Contenedor interactivo que representa el acceso al chat con Carlos Muñoz. */
    private LinearLayout chatCarlos;

    /**
     * Inicializa la actividad, vincula los componentes de la interfaz de usuario,
     * configura el sistema de navegación y define las acciones de clic para cada chat.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnNuevoChat = findViewById(R.id.btnNuevoChat);
        chatAna = findViewById(R.id.chatAna);
        chatJunta = findViewById(R.id.chatJunta);
        chatCarlos = findViewById(R.id.chatCarlos);

        NavigationHelper.configurarNavegacion(this);

        btnNuevoChat.setOnClickListener(v ->
                Toast.makeText(
                        ChatActivity.this,
                        "La función para iniciar chats se implementará próximamente.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        chatAna.setOnClickListener(v ->
                mostrarMensajePendiente("Ana González")
        );

        chatJunta.setOnClickListener(v ->
                mostrarMensajePendiente("Junta de Vecinos N.º 12")
        );

        chatCarlos.setOnClickListener(v ->
                mostrarMensajePendiente("Carlos Muñoz")
        );
    }

    /**
     * Muestra un mensaje emergente de tipo Toast notificando que la conversación
     * con el contacto seleccionado está pendiente de implementación.
     *
     * @param contacto El nombre del usuario o grupo con el que se intenta interactuar.
     */
    private void mostrarMensajePendiente(String contacto) {
        Toast.makeText(
                this,
                "Chat con " + contacto + ": función pendiente de implementar.",
                Toast.LENGTH_SHORT
        ).show();
    }
}
