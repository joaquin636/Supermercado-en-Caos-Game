package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.event.KeyEvent;

public final class EstadoJugando implements EstadoJuego {
    private final Juego juego;

    public EstadoJugando(Juego juego) {
        this.juego = juego;
    }

    @Override public void actualizar() {
        juego.actualizarSimulacionPartida();
    }

    @Override public void manejarTeclado(int keyCode, boolean pressed) {
        if (!pressed) return;
        switch (keyCode) {
            case KeyEvent.VK_P, KeyEvent.VK_ESCAPE -> juego.alternarPausa();
            case KeyEvent.VK_E -> juego.interactuar();
            case KeyEvent.VK_Q -> juego.soltarProducto();
            default -> { }
        }
    }
}
