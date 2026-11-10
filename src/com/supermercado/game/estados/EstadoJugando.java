package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class EstadoJugando implements EstadoJuego {
    private Juego juego;

    public EstadoJugando(Juego juego) {
        this.juego = juego;
    }

    @Override
    public void actualizar() {
        // Llama a la simulación real de la partida en el modelo
        juego.actualizarSimulacionPartida();
    }

    @Override
    public void dibujar(Graphics2D g2d) {
        // El dibujo del nivel y personajes lo maneja GamePanel 
        // a través del método getEstado() de compatibilidad, 
        // por lo que aquí no requiere llamadas adicionales.
    }

    @Override
    public void manejarTeclado(int keyCode, boolean pressed) {
        if (!pressed) return;
        
        if (keyCode == KeyEvent.VK_P) {
            juego.alternarPausa();
        } else if (keyCode == KeyEvent.VK_E) {
            juego.interactuar();
        } else if (keyCode == KeyEvent.VK_Q) {
            juego.soltarProducto();
        }
    }
}