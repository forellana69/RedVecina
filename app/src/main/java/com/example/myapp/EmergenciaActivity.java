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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EmergenciaActivity extends AppCompatActivity {

    private static final int CODIGO_PERMISO_UBICACION = 1001;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tiempoLimiteUbicacion;
    private ToneGenerator generadorTono;
    private Vibrator vibrador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emergencia);

        NavigationHelper.configurarNavegacion(this);

        findViewById(R.id.btnEnviarAlerta)
                .setOnClickListener(v -> mostrarConfirmacion());

    }

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


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

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

    private void mostrarErrorUbicacion() {
        Toast.makeText(
                this,
                "No se pudo obtener la ubicación del dispositivo.",
                Toast.LENGTH_LONG
        ).show();
    }
}