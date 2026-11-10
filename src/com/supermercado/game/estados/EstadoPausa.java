package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class EstadoPausa implements EstadoJuego {
    private Juego juego;
    private EstadoJuego estadoAnterior;

    public EstadoPausa(Juego juego, EstadoJuego estadoAnterior) {
        this.juego = juego;
        this.estadoAnterior = estadoAnterior;
    }

    @Override
    public void actualizar() {
        // En pausa no se actualiza la simulación del juego (reloj y personajes congelados)
    }

    @Override
    public void dibujar(Graphics2D g2d) {
        if (estadoAnterior != null) {
            estadoAnterior.dibujar(g2d);
        }
        
        // Capa translúcida oscura de pausa
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, 800, 600);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("JUEGO PAUSADO", 260, 260);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Presiona P para Reanudar", 300, 320);
    }

    @Override
    public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed && (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE)) {
            juego.cambiarEstado(estadoAnterior);
        }
    }
}