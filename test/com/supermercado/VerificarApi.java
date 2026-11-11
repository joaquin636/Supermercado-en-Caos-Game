package com.supermercado;

import com.supermercado.api.APIClient;
import com.supermercado.game.Juego;
import com.supermercado.game.eventos.RegistroEstadisticas;
import com.supermercado.levels.CatalogoNiveles;
import com.supermercado.obstacles.Estante;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Demostracion explicita de red. No se invoca desde la suite automatica. */
public final class VerificarApi {
    private VerificarApi() {}
    private static final class TransporteReal extends APIClient {
        private CompletableFuture<Boolean> envio;
        @Override public CompletableFuture<Boolean> subirEstadisticasAsync(int n, int p, int e, int c) {
            System.out.printf("POST de prueba real: nivel=%d, puntos=%d, entregas=%d, choques=%d%n", n, p, e, c);
            envio = super.subirEstadisticasAsync(n, p, e, c);
            return envio;
        }
    }
    public static void main(String[] args) throws Exception {
        TransporteReal cliente = new TransporteReal();
        RegistroEstadisticas registro = new RegistroEstadisticas(cliente);
        Juego juego = new Juego(CatalogoNiveles.predeterminado(), registro);
        juego.iniciarPartida();
        // Prepara entregas del modelo. Este lanzador comprueba red, no habilidad de un jugador humano.
        for (var producto : juego.getNivel().getProductos()) {
            juego.getNivel().getJugador().setPosicion(producto.getX(), producto.getY());
            juego.interactuar();
            var estante = juego.getNivel().getObstaculos().stream()
                    .filter(Estante.class::isInstance).map(Estante.class::cast)
                    .filter(e -> e.getCategoria().equals(producto.getCategoria())).findFirst().orElseThrow();
            juego.getNivel().getJugador().setPosicion(estante.getX() + 15, estante.getY() + estante.getAlto() + 2);
            juego.interactuar();
        }
        if (juego.getEstado() != Juego.Estado.VICTORIA || cliente.envio == null)
            throw new AssertionError("La demostracion necesita una victoria real del modelo");
        System.out.println("Resumen del modelo: " + registro.getUltimoResumen());
        boolean aceptado = cliente.envio.get(8, TimeUnit.SECONDS);
        System.out.println(aceptado ? "Aceptacion HTTP comprobada. JSONPlaceholder simula la creacion, sin persistencia."
                : "No se obtuvo aceptacion HTTP. Verificar conexion y salida de APIClient.");
        if (!aceptado) throw new IllegalStateException("Verificacion de red fallida");
    }
}
