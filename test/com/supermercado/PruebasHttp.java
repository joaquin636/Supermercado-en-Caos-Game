package com.supermercado;

import com.sun.net.httpserver.HttpServer;
import com.supermercado.api.APIClient;
import com.supermercado.game.Juego;
import com.supermercado.game.eventos.RegistroEstadisticas;
import com.supermercado.levels.CatalogoNiveles;
import com.supermercado.levels.ConfiguracionNivel;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;

/** El servidor es local. Ninguna prueba de esta clase envia partidas a Internet. */
final class PruebasHttp {
    private static int comprobaciones;

    private static void verificar(boolean valor, String detalle) {
        if (!valor) throw new AssertionError(detalle);
        comprobaciones++;
    }

    private static final class ClientePrueba extends APIClient {
        final AtomicInteger llamadas = new AtomicInteger();
        CompletableFuture<Boolean> envio;
        ClientePrueba(URI destino) { super(destino, Duration.ofSeconds(8)); }
        @Override public CompletableFuture<Boolean> subirEstadisticasAsync(int n, int p, int e, int c) {
            llamadas.incrementAndGet();
            envio = super.subirEstadisticasAsync(n, p, e, c);
            return envio;
        }
    }

    private static final class ClienteControlado extends APIClient {
        final List<String> solicitudes = new ArrayList<>();
        final List<CompletableFuture<Boolean>> respuestas = new ArrayList<>();
        @Override public CompletableFuture<Boolean> subirEstadisticasAsync(int n, int p, int e, int c) {
            solicitudes.add(n + "/" + p + "/" + e + "/" + c);
            var respuesta = new CompletableFuture<Boolean>();
            respuestas.add(respuesta);
            return respuesta;
        }
    }

    static void ejecutar() throws Exception {
        comprobaciones = 0;
        SwingUtilities.invokeAndWait(PruebasHttp::resumenesPorIntento);
        transporteLocal();
        System.out.println("OK HTTP y estadisticas: " + comprobaciones
                + " comprobaciones. EDT activo, envio unico, JSON, reintentos, errores y timeout.");
    }

    private static void entregar(Juego juego, Producto producto) {
        var jugador = juego.getNivel().getJugador();
        jugador.setPosicion(producto.getX(), producto.getY());
        juego.interactuar();
        var estante = juego.getNivel().getObstaculos().stream().filter(Estante.class::isInstance)
                .map(Estante.class::cast).filter(e -> e.getCategoria().equals(producto.getCategoria()))
                .findFirst().orElseThrow();
        jugador.setPosicion(estante.getX() + 15, estante.getY() + estante.getAlto() + 2);
        juego.interactuar();
        verificar(producto.isEntregado(), "Producto entregado durante prueba de estadisticas");
    }

    private static void resumenesPorIntento() {
        ClienteControlado cliente = new ClienteControlado();
        RegistroEstadisticas registro = new RegistroEstadisticas(cliente);
        ConfiguracionNivel uno = ConfiguracionNivel.crear(1, "Fixture de intentos")
                .estante(Producto.FRUTAS, 170, 60)
                .producto("Manzana", Producto.FRUTAS, 104, 220)
                .producto("Platano", Producto.FRUTAS, 430, 455).construir();
        ConfiguracionNivel dos = ConfiguracionNivel.crear(2, "Fixture de puntuacion previa")
                .estante(Producto.LACTEOS, 430, 60)
                .producto("Leche", Producto.LACTEOS, 440, 270).construir();
        Juego juego = new Juego(new CatalogoNiveles(List.of(uno, dos)), registro);
        juego.iniciarPartida();
        entregar(juego, juego.getNivel().getProductos().get(0));
        juego.terminarPartida();
        juego.terminarPartida();
        verificar(cliente.solicitudes.equals(List.of("1/100/1/0")), "Un resumen con datos exactos del intento perdido");
        var fallido = registro.getUltimoResumen();
        verificar(fallido.puntosIntento() == 100 && fallido.resultado().equals("DERROTA"),
                "Snapshot conserva puntos provisionales y resultado del intento perdido");
        verificar(juego.reintentarNivel() && registro.getEntregasCorrectas() == 0 && juego.getPuntuacion() == 0,
                "Reintento restablece contadores y puntos del intento");
        cliente.respuestas.get(0).complete(false);
        verificar(registro.getMensajeEnvio().equals("Sin envios en este intento."),
                "Respuesta tardia no modifica el mensaje del intento nuevo");
        for (Producto p : juego.getNivel().getProductos()) entregar(juego, p);
        verificar(cliente.solicitudes.get(1).equals("1/200/2/0"), "Resumen del reintento excluye entregas del intento fallido");
        cliente.respuestas.get(1).complete(true);
        verificar(registro.getMensajeEnvio().contains("sin guardado permanente"), "Aceptacion de demo expresa limite de persistencia");
        verificar(juego.avanzarNivel() && registro.getEntregasCorrectas() == 0, "Nivel siguiente comienza contadores nuevos");
        juego.terminarPartida();
        verificar(cliente.solicitudes.get(2).equals("2/200/0/0")
                && registro.getUltimoResumen().puntosIntento() == 0, "Conserva puntos previos y reporta intento vacio");
        verificar(juego.reintentarNivel(), "Permite reintentar el segundo nivel");
        entregar(juego, juego.getNivel().getProductos().get(0));
        verificar(cliente.solicitudes.get(3).equals("2/300/1/0"), "Nivel dos informa una entrega y puntos acumulados validos");
        verificar(registro.getNivelesFinalizados() == 4, "Contador de finales incluye intentos de toda la campana");
        registro.onEvento(registro.getUltimoEvento());
        verificar(cliente.solicitudes.size() == 4, "Registro tambien rechaza evento FIN duplicado");
        cliente.respuestas.get(3).completeExceptionally(new IllegalStateException("Fallo de transporte de prueba"));
        verificar(registro.getMensajeEnvio().contains("Puedes continuar"), "Excepcion asincrona no interrumpe el modelo");
        juego.iniciarPartida();
        verificar(registro.getNivelesFinalizados() == 0 && registro.getUltimoResumen() == null, "Campana nueva limpia estadisticas");
        cliente.respuestas.get(2).complete(true);
        verificar(registro.getMensajeEnvio().equals("Sin envios en este intento."), "Respuesta de campana anterior no reaparece");
    }

    private static void transporteLocal() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var ejecutor = Executors.newCachedThreadPool();
        servidor.setExecutor(ejecutor);
        URI base = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort());
        CountDownLatch recibida = new CountDownLatch(1), responder = new CountDownLatch(1);
        CountDownLatch cerrarLenta = new CountDownLatch(1);
        AtomicInteger posts = new AtomicInteger();
        AtomicReference<String> contenido = new AtomicReference<>();
        servidor.createContext("/posts", peticion -> {
            try (peticion) {
                posts.incrementAndGet();
                contenido.set(peticion.getRequestMethod() + " " + peticion.getRequestHeaders().getFirst("Content-Type")
                        + " " + new String(peticion.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                recibida.countDown();
                try { responder.await(10, TimeUnit.SECONDS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                peticion.sendResponseHeaders(201, -1);
            }
        });
        servidor.createContext("/error", p -> { try (p) { p.sendResponseHeaders(503, -1); } });
        servidor.createContext("/lenta", p -> {
            try (p) {
                try { cerrarLenta.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        servidor.start();
        try {
            ClientePrueba cliente = new ClientePrueba(base.resolve("/posts"));
            Juego juego = new Juego(CatalogoNiveles.predeterminado(), new RegistroEstadisticas(cliente));
            SwingUtilities.invokeAndWait(() -> {
                juego.iniciarPartida();
                entregar(juego, juego.getNivel().getProductos().get(0));
                var jugador = juego.getNivel().getJugador();
                var npc = juego.getNivel().getPersonajes().get(1);
                jugador.setPosicion(npc.getX(), npc.getY()); juego.actualizar();
                juego.terminarPartida(); juego.terminarPartida(); juego.actualizar();
            });
            verificar(recibida.await(4, TimeUnit.SECONDS), "El servidor recibio POST desde el juego");
            SwingUtilities.invokeAndWait(() -> {
                verificar(!cliente.envio.isDone(), "El EDT procesa otro turno con la respuesta pendiente");
                verificar(juego.getEstadisticas().getMensajeEnvio().contains("Enviando"), "HUD puede consultar envio pendiente");
                verificar(juego.reintentarNivel(), "Reintento responde antes de recibir HTTP");
                juego.actualizar();
                verificar(juego.getTiempoRestante() < 180, "El reloj sigue activo con POST pendiente");
            });
            responder.countDown();
            verificar(cliente.envio.get(5, TimeUnit.SECONDS), "HTTP 201 se acepta");
            verificar(cliente.llamadas.get() == 1 && posts.get() == 1, "La finalizacion repetida envia exactamente una vez");
            verificar(contenido.get().equals("POST application/json {\"nivel\": 1, \"puntos\": 100, \"entregas\": 1, \"choques\": 1}"),
                    "Metodo, cabecera y valores del intento coinciden con el juego: " + contenido.get());
            verificar(!enviar(base.resolve("/error"), 2000), "HTTP 503 se informa como fallo");
            verificar(!enviar(base.resolve("/lenta"), 500), "Servidor lento agota timeout");
        } finally {
            responder.countDown();
            cerrarLenta.countDown();
            servidor.stop(0);
            ejecutor.shutdownNow();
        }
        verificar(!enviar(base.resolve("/posts"), 2000), "Servidor desconectado se informa como fallo");
    }

    private static boolean enviar(URI url, int milisegundos) throws Exception {
        APIClient cliente = new APIClient(url, Duration.ofMillis(milisegundos));
        return cliente.subirEstadisticasAsync(1, 0, 0, 0).get(4, TimeUnit.SECONDS);
    }
}
