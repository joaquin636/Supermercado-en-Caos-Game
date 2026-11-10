package com.supermercado.game.estados;

import com.supermercado.game.Juego;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class EstadoFinalCampana implements EstadoJuego {
    private Juego juego;

    public EstadoFinalCampana(Juego juego) {
        this.juego = juego;
    }

    @Override
    public void actualizar() {}

    @Override
    public void dibujar(Graphics2D g2d) {
        g2d.setColor(new Color(10, 30, 10, 230));
        g2d.fillRect(0, 0, 800, 600);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("¡CAMPAÑA COMPLETADA!", 180, 200);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Has superado los seis niveles del supermercado.", 200, 260);
        g2d.drawString("Puntuación final: " + juego.getPuntuacion(), 300, 310);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Presiona R o ENTER para reiniciar la campaña", 240, 380);
    }

    @Override
    public void manejarTeclado(int keyCode, boolean pressed) {
        if (pressed && (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_R)) {
            juego.iniciarPartida();
        }
    }
}