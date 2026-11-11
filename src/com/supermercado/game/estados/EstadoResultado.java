package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.event.KeyEvent;

public final class EstadoResultado implements EstadoJuego {
    private final Juego juego;
    private final boolean victoria;

    public EstadoResultado(Juego juego, boolean victoria) {
        this.juego = juego;
        this.victoria = victoria;
    }

    public boolean isVictoria() { return victoria; }

    @Override public void actualizar() {}

    @Override public void manejarTeclado(int keyCode, boolean pressed) {
        if (!pressed) return;
        if (keyCode == KeyEvent.VK_R) {
            juego.iniciarPartida();
        } else if (keyCode == KeyEvent.VK_ENTER) {
            if (victoria) juego.avanzarNivel();
            else juego.reintentarNivel();
        }
    }
}
