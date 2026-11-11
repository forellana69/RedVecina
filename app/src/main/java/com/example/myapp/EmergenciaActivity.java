package com.example.myapp;

import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.Toast;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad encargada de gestionar situaciones de emergencia dentro de la aplicación.
 * Ofrece la posibilidad de emitir una alarma acústica local, activar vibración cíclica
 * y obtener la localización geográfica exacta del dispositivo para compartirla mediante
 * un mapa dinámico con la red de usuarios a la que pertenece
 *
 * @author Francisco
 * @version 1.0
 */
public class EmergenciaActivity extends AppCompatActivity {

    /** Código identificador para la solicitud de permisos de geolocalización en tiempo de ejecución. */
    private static final int CODIGO_PERMISO_UBICACION = 1001;

    /** Administrador del sistema encargado de proveer acceso a los servicios de localización. */
    private LocationManager locationManager;

    /** Escuchador de eventos que reacciona ante los cambios de coordenadas geográficas recibidas. */
    private LocationListener locationListener;

    /** Manejador asociado al hilo principal utilizado para programar operaciones con retardo de tiempo. */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** Tarea programable encargada de cancelar la búsqueda de señal si se supera el tiempo límite. */
    private Runnable tiempoLimiteUbicacion;

    /** Componente encargado de reproducir señales auditivas de alerta mediante el hardware del teléfono. */
    private ToneGenerator generadorTono;

    /** Instancia del sistema responsable de controlar el hardware de vibración del terminal. */
    private Vibrator vibrador;

    /**
     * Inicializa la actividad, infla la interfaz gráfica de usuario, vincula el helper
     * de navegación general y define el listener de pulsación para el gatillo de emergencia.
     *
     * @param savedInstanceState Si la actividad se vuelve a inicializar después de haber
     *                           sido cerrada previamente, este Bundle contiene los datos
     *                           más recientes suministrados. De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emergencia);

        NavigationHelper.configurarNavegacion(this);

        findViewById(R.id.btnEnviarAlerta)
                .setOnClickListener(v -> mostrarConfirmacion());

    }

    /**
     * Despliega un cuadro de diálogo flotante (AlertDialog) para evitar activaciones accidentales,
     * requiriendo una confirmación explícita del usuario antes de detonar el protocolo operativo.
     */
    private void mostrarConfirmacion() {

        new AlertDialog.Builder(this)
                .setTitle("Enviar alerta")
                .setMessage(
                        "¿Estás seguro de que deseas enviar una alerta de emergencia?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton("Enviar", (dialog, which) -> {
                    activarAlerta();
                })
                .show();
    }

    /**
     * Inicia el proceso operativo de respuesta ante la emergencia. Ejecuta inmediatamente
     * la alarma sensorial sonora/vibratoria local y evalúa si el usuario ya dispone de los
     * permisos obligatorios de ubicación; si no los tiene, solicita el acceso en tiempo de ejecución.
     */
    private void activarAlerta() {

        iniciarAlarmaLocal();

        boolean tieneUbicacionFina =
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean tieneUbicacionAproximada =
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (!tieneUbicacionFina && !tieneUbicacionAproximada) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    CODIGO_PERMISO_UBICACION
            );
            return;
        }

        obtenerUbicacionActual();
    }

    /**
     * Dispara las alertas físicas inmediatas en el terminal de manera segura. Combina señales
     * acústicas continuas a través del canal de alarma del sistema con un patrón cíclico de
     * vibraciones rítmicas durante un periodo controlado de 8 segundos.
     */
    private void iniciarAlarmaLocal() {
        try {
            if (generadorTono != null) {
                generadorTono.release();
            }

            generadorTono = new ToneGenerator(
                    AudioManager.STREAM_ALARM,
                    100
            );

            generadorTono.startTone(
                    ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                    8000
            );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "No se pudo activar el sonido de alarma.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        try {
            vibrador = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            if (vibrador != null && vibrador.hasVibrator()) {
                long[] patron = {0, 500, 300, 500, 300, 500};

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrador.vibrate(
                            VibrationEffect.createWaveform(patron, 0)
                    );
                } else {
                    vibrador.vibrate(patron, 0);
                }

                handler.postDelayed(() -> {
                    if (vibrador != null) {
                        vibrador.cancel();
                    }
                }, 8000);
            }
        } catch (Exception e) {
        }

        Toast.makeText(
                this,
                "Alerta de emergencia activada en este dispositivo.",
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Construye un mensaje de texto formateado con las coordenadas físicas actuales y un enlace web
     * directo hacia Google Maps, delegando su envío mediante un selector de intenciones nativo del sistema.
     *
     * @param ubicacion Estructura espacial con la información detallada de la latitud y longitud actuales.
     */
    private void compartirUbicacion(Location ubicacion) {
        double latitud = ubicacion.getLatitude();
        double longitud = ubicacion.getLongitude();

        String enlaceMapa =
                "https://www.google.com/maps/search/?api=1&query="
                        + latitud + "," + longitud;

        String mensaje =
                "🚨 ALERTA DE EMERGENCIA - REDVECINA\n\n"
                        + "Necesito ayuda. Esta es mi ubicación actual:\n"
                        + enlaceMapa
                        + "\n\nLatitud: " + latitud
                        + "\nLongitud: " + longitud;

        Intent compartir = new Intent(Intent.ACTION_SEND);
        compartir.setType("text/plain");
        compartir.putExtra(Intent.EXTRA_SUBJECT, "Alerta de emergencia");
        compartir.putExtra(Intent.EXTRA_TEXT, mensaje);

        startActivity(
                Intent.createChooser(
                        compartir,
                        "Compartir ubicación de emergencia"
                )
        );
    }

    /**
     * Intercepta la respuesta del cuadro de diálogo nativo de concesión de permisos.
     * Si la solicitud de ubicación es aprobada por el usuario, inicia la consulta de la posición geográfica;
     * de lo contrario, alerta mediante un mensaje en pantalla sobre la limitación para compartirla.
     *
     * @param requestCode  Código entero que valida la correspondencia de la llamada de solicitud.
     * @param permissions  Colección de cadenas de texto con los identificadores de permisos solicitados.
     * @param grantResults Matriz de enteros que determina si cada permiso fue otorgado o rechazado.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == CODIGO_PERMISO_UBICACION) {
            boolean concedido = false;

            for (int resultado : grantResults) {
                if (resultado == PackageManager.PERMISSION_GRANTED) {
                    concedido = true;
                    break;
                }
            }

            if (concedido) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(
                        this,
                        "La alarma local funcionó, pero debes conceder "
                                + "el permiso de ubicación para compartirla.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    /**
     * Verifica la disponibilidad física de los hardware de localización (GPS o Antenas de Red),
     * instancia el escuchador en segundo plano para obtener el cambio de coordenadas y configura
     * un temporizador (timeout) de 20 segundos para detener la búsqueda en caso de falta de señal.
     */
    private void obtenerUbicacionActual() {
        boolean tieneUbicacionFina =
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean tieneUbicacionAproximada =
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (!tieneUbicacionFina && !tieneUbicacionAproximada) {
            return;
        }

        try {
            locationManager = (LocationManager)
                    getSystemService(LOCATION_SERVICE);

            if (locationManager == null) {
                mostrarErrorUbicacion();
                return;
            }

            boolean gpsDisponible = locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
            );

            boolean redDisponible = locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
            );

            if (!gpsDisponible && !redDisponible) {
                Toast.makeText(
                        this,
                        "Activa la ubicación del teléfono e inténtalo otra vez.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location ubicacion) {
                    handler.removeCallbacks(tiempoLimiteUbicacion);

                    try {
                        locationManager.removeUpdates(this);
                    } catch (SecurityException ignored) {
                    }

                    compartirUbicacion(ubicacion);
                }

                @Override
                public void onStatusChanged(
                        String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            tiempoLimiteUbicacion = () -> {
                try {
                    if (locationManager != null && locationListener != null) {
                        locationManager.removeUpdates(locationListener);
                    }
                } catch (SecurityException ignored) {
                }

                Toast.makeText(
                        this,
                        "No se pudo obtener una ubicación actual. "
                                + "Comprueba el GPS e inténtalo nuevamente.",
                        Toast.LENGTH_LONG
                ).show();
            };

            if (gpsDisponible && tieneUbicacionFina) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0,
                        locationListener,
                        Looper.getMainLooper()
                );
            }

            if (redDisponible
                    && (tieneUbicacionFina || tieneUbicacionAproximada)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0,
                        locationListener,
                        Looper.getMainLooper()
                );
            }

            handler.postDelayed(tiempoLimiteUbicacion, 20000);

        } catch (SecurityException e) {
            Toast.makeText(
                    this,
                    "Android no permitió acceder a la ubicación.",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            mostrarErrorUbicacion();
        }
    }

    /**
     * Muestra un mensaje emergente de tipo Toast notificando que ocurrió un problema inesperado
     * o fallo genérico del sistema al intentar consultar el servicio de posicionamiento.
     */
    private void mostrarErrorUbicacion() {
        Toast.makeText(
                this,
                "No se pudo obtener la ubicación del dispositivo.",
                Toast.LENGTH_LONG
        ).show();
    }
}
