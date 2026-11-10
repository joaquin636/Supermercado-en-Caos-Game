package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class EstadoResultado implements EstadoJuego {
    private Juego juego;
    private boolean victoria;

    public EstadoResultado(Juego juego, boolean victoria) {
        this.juego = juego;
        this.victoria = victoria;
    }

    @Override
    public void actualizar() {
        // Pantalla estática de resultados
    }

    @Override
    public void dibujar(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, 800, 600);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        
        if (victoria) {
            g2d.drawString("¡NIVEL SUPERADO!", 250, 220);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Presiona ENTER para ir al siguiente nivel", 240, 280);
        } else {
            g2d.drawString("¡TIEMPO AGOTADO / DERROTA!", 170, 220);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Presiona ENTER para reintentar el nivel", 240, 280);
            g2d.drawString("Se descartan los puntos del intento fallido.", 230, 310);
        }
        g2d.drawString("Presiona R para volver a la nueva campaña", 240, 370);
    }

    @Override
    public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed) {
            if (keyCode == KeyEvent.VK_ENTER) {
                if (victoria) {
                    juego.avanzarNivel();
                } else {
                    juego.reintentarNivel();
                }
            } else if (keyCode == KeyEvent.VK_R) {
                juego.iniciarPartida();
            }
        }
    }
}