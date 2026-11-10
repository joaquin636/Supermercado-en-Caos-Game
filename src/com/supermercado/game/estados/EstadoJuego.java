package com.supermercado.game.estados;

import java.awt.Graphics2D;

public interface EstadoJuego {
    void actualizar();
    void dibujar(Graphics2D g2d);
    void manejarTeclado(int keyCode, boolean pressed);
}