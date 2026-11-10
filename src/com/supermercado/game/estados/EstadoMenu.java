package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class EstadoMenu implements EstadoJuego {
    private Juego juego;

    public EstadoMenu(Juego juego) {
        this.juego = juego;
    }

    @Override
    public void actualizar() {
        // El menú no requiere lógica de simulación continua
    }

    @Override
    public void dibujar(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 800, 600);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.drawString("SUPERMERCADO EN CAOS", 200, 220);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Presiona ENTER para Iniciar Partida", 260, 280);
    }

    @Override
    public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed && keyCode == KeyEvent.VK_ENTER) {
            juego.cambiarEstado(new EstadoJugando(juego));
        }
    }
}