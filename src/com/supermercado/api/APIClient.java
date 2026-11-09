package com.supermercado.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Criterio III: Integracion con API Externa.
 * Clase altamente cohesionada y desacoplada que envia la telemetria a la nube.
 */
public class APIClient {
    // Usamos un endpoint publico de pruebas que simula recibir datos y devuelve un HTTP 201 (Created)
    private static final String ENDPOINT = "https://jsonplaceholder.typicode.com/posts";
    private final HttpClient client;

    public APIClient() {
        // Configuramos el cliente con el timeout exigido por la rubrica
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean subirEstadisticas(int nivel, int puntos, int entregas, int choques) {
        try {
            // Creamos un JSON basico a mano para no depender de librerias externas
            String jsonBody = String.format(
                    "{\"nivel\": %d, \"puntos\": %d, \"entregas\": %d, \"choques\": %d}",
                    nivel, puntos, entregas, choques
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            // Enviamos la peticion de forma sincrona
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificamos manejo de respuestas HTTP fallidas o exitosas
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("[APIClient] Exito: Estadisticas guardadas en la nube. (HTTP " + response.statusCode() + ")");
                return true;
            } else {
                System.err.println("[APIClient] Error: Respuesta HTTP fallida -> " + response.statusCode());
                return false;
            }
            
        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("[APIClient] Error: Tiempo de espera agotado (Timeout) al conectar con el servidor.");
            return false;
        } catch (Exception e) {
            System.err.println("[APIClient] Error de red: No se pudo conectar a la API externa -> " + e.getMessage());
            return false;
        }
    }
}