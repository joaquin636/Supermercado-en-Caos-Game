package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.event.KeyEvent;
import java.util.Objects;

public final class EstadoPausa implements EstadoJuego {
    private final Juego juego;
    private final EstadoJuego estadoAnterior;

    public EstadoPausa(Juego juego, EstadoJuego estadoAnterior) {
        this.juego = juego;
        this.estadoAnterior = Objects.requireNonNull(estadoAnterior);
    }

    @Override public void actualizar() {}

    @Override public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed && (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE)) {
            juego.cambiarEstado(estadoAnterior);
        }
    }
}
