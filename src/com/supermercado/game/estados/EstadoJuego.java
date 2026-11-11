package com.supermercado.game.estados;

/** Comportamiento del modelo segun la pantalla activa. La presentacion vive en GamePanel. */
public interface EstadoJuego {
    void actualizar();
    void manejarTeclado(int keyCode, boolean pressed);
}
