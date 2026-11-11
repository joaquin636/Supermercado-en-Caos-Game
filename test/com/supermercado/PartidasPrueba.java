package com.supermercado;

import com.supermercado.api.APIClient;
import com.supermercado.game.Juego;
import com.supermercado.game.eventos.RegistroEstadisticas;
import com.supermercado.levels.CatalogoNiveles;
import java.util.concurrent.CompletableFuture;

/** Las pruebas de gameplay no transmiten partidas sinteticas a Internet. */
final class PartidasPrueba {
    private static final APIClient SIN_RED = new APIClient() {
        @Override public CompletableFuture<Boolean> subirEstadisticasAsync(int n, int p, int e, int c) {
            return CompletableFuture.completedFuture(true);
        }
    };
    static Juego crear() { return crear(CatalogoNiveles.predeterminado()); }
    static Juego crear(CatalogoNiveles catalogo) {
        return new Juego(catalogo, new RegistroEstadisticas(SIN_RED) {
            @Override public String getMensajeEnvio() { return "Transporte simulado para pruebas."; }
        });
    }
}
