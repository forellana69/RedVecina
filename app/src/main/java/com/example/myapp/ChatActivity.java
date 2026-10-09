package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private Button btnNuevoChat;
    private LinearLayout chatAna;
    private LinearLayout chatJunta;
    private LinearLayout chatCarlos;

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

    private void mostrarMensajePendiente(String contacto) {
        Toast.makeText(
                this,
                "Chat con " + contacto + ": función pendiente de implementar.",
                Toast.LENGTH_SHORT
        ).show();
    }
}