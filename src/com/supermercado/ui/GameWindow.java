package com.supermercado.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private final GamePanel panel;

    public GameWindow() {
        super("Supermercado en Caos | Mercado La Esquina");
        panel = new GamePanel();
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double escala = Math.min(1, Math.min(area.width * .94 / GamePanel.ANCHO, (area.height - 80.0) / GamePanel.ALTO));
        panel.setPreferredSize(new Dimension((int) (GamePanel.ANCHO * escala), (int) (GamePanel.ALTO * escala)));
        pack();
        setLocationRelativeTo(null);
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { panel.perderFoco(); }
        });
        setVisible(true);
    }
}
