package com.example.myapp;

/**
 * Clase de configuración final encargada de centralizar las credenciales de acceso
 * requeridas para establecer la comunicación con el backend de Supabase.
 * Contiene los parámetros del punto de acceso (endpoint) y la clave pública de la API.
 *
 * @author Francisco
 * @version 1.0
 */
public class SupabaseConfig {

    /**
     * Constructor privado para prevenir la instanciación de esta clase de configuración.
     */
    private SupabaseConfig() {
    }

    /** Dirección URL base del proyecto o servicio en la nube de Supabase. */
    public static final String PROJECT_URL =
            "https://uemvcziureaedtquvuhe.supabase.co";

    /** Clave pública de la API (Anon/Publishable Key) utilizada para autorizar las peticiones HTTP. */
    public static final String PUBLISHABLE_KEY =
            "sb_publishable_pK5Pa8xZO64MDxelEokdTA_-Wu7kccL";

}
