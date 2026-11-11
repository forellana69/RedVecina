package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad que gestiona la cartelera de avisos comunitarios.
 * Permite visualizar diferentes tipos de comunicados mediante diálogos informativos,
 * configurar accesos para futuras publicaciones y aplicar la navegación global.
 *
 * @author Francisco
 * @version 1.0
 */
public class AvisosActivity extends AppCompatActivity {

    /** Botón destinado a la creación y publicación de un nuevo aviso comunitario. */
    private Button btnNuevoAviso;

    /** Contenedor interactivo que despliega los detalles del aviso de reunión. */
    private LinearLayout avisoReunion;

    /** Contenedor interactivo que despliega los detalles del aviso de actividad. */
    private LinearLayout avisoActividad;

    /** Contenedor interactivo que despliega los detalles del aviso de información general. */
    private LinearLayout avisoInformacion;

    /**
     * Inicializa la actividad, vincula los componentes de la interfaz de usuario,
     * configura la navegación y define los listeners para cada interacción.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
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

    /**
     * Despliega un cuadro de diálogo flotante (AlertDialog) para mostrar detalladamente
     * el título y contenido del aviso seleccionado.
     *
     * @param titulo  El texto que se asignará como encabezado del cuadro de diálogo.
     * @param mensaje El cuerpo informativo o descripción que se mostrará al usuario.
     */
    private void mostrarAviso(String titulo, String mensaje) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }
}