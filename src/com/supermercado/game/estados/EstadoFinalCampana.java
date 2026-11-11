package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.event.KeyEvent;

public final class EstadoFinalCampana implements EstadoJuego {
    private final Juego juego;

    public EstadoFinalCampana(Juego juego) {
        this.juego = juego;
    }

    @Override public void actualizar() {}

    @Override public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed && (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_R)) juego.iniciarPartida();
    }
}
