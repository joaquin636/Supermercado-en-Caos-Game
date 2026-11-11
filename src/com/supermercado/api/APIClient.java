package com.supermercado.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Transporte de resumenes al servicio de demostracion JSONPlaceholder. */
public class APIClient {
    private static final URI ENDPOINT = URI.create("https://jsonplaceholder.typicode.com/posts");
    private final URI endpoint;
    private final Duration espera;
    private CompletableFuture<HttpClient> cliente;

    public APIClient() { this(ENDPOINT, Duration.ofSeconds(5)); }

    /** Permite probar el mismo transporte con un servidor HTTP local. */
    public APIClient(URI endpoint, Duration espera) {
        this.endpoint = Objects.requireNonNull(endpoint);
        this.espera = Objects.requireNonNull(espera);
        if (espera.isZero() || espera.isNegative()) throw new IllegalArgumentException("Espera positiva requerida");
        if (!"http".equals(endpoint.getScheme()) && !"https".equals(endpoint.getScheme()))
            throw new IllegalArgumentException("La API necesita HTTP o HTTPS");
    }

    // La inicializacion tambien queda fuera del hilo de Swing y puede recuperarse de un fallo.
    private synchronized CompletableFuture<HttpClient> obtenerCliente() {
        if (cliente == null || cliente.isCompletedExceptionally()) {
            cliente = CompletableFuture.supplyAsync(() -> HttpClient.newBuilder().connectTimeout(espera).build());
        }
        return cliente;
    }

    /** Puntos es el acumulado valido de la campana; entregas y choques pertenecen al intento finalizado. */
    public CompletableFuture<Boolean> subirEstadisticasAsync(int nivel, int puntos, int entregas, int choques) {
        String json = String.format(Locale.ROOT,
                "{\"nivel\": %d, \"puntos\": %d, \"entregas\": %d, \"choques\": %d}",
                nivel, puntos, entregas, choques);
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(espera)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return obtenerCliente().thenCompose(http -> http.sendAsync(request, HttpResponse.BodyHandlers.discarding()))
                    .orTimeout(Math.max(1, espera.toMillis()), TimeUnit.MILLISECONDS)
                    .handle((respuesta, error) -> {
                        if (error != null) return fallo(error);
                        int estado = respuesta.statusCode();
                        boolean aceptado = estado >= 200 && estado < 300;
                        if (aceptado) System.out.println("[APIClient] HTTP " + estado + ": peticion aceptada.");
                        else System.err.println("[APIClient] HTTP " + estado + ": peticion rechazada.");
                        return aceptado;
                    });
        } catch (RuntimeException error) {
            return CompletableFuture.completedFuture(fallo(error));
        }
    }

    private static boolean fallo(Throwable error) {
        Throwable causa = error;
        while (causa instanceof CompletionException && causa.getCause() != null) causa = causa.getCause();
        String detalle = causa instanceof HttpTimeoutException || causa instanceof TimeoutException
                ? "Tiempo de espera agotado." : "Envio fallido: " + causa;
        System.err.println("[APIClient] " + detalle);
        return false;
    }

    /** Adaptador para consumidores sincronos. La partida utiliza exclusivamente la variante asincrona. */
    public boolean subirEstadisticas(int nivel, int puntos, int entregas, int choques) {
        return subirEstadisticasAsync(nivel, puntos, entregas, choques).join();
    }
}
