package com.supermercado.input;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/** Key Bindings: funcionan aunque el panel no sea el componente enfocado. */
public final class ControlTeclado {
    private final Set<Integer> pulsadas = new HashSet<>();
    private final Set<Integer> acciones = new HashSet<>();

    public void instalar(JComponent panel) {
        int[] teclas = {KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
            KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT,
            KeyEvent.VK_E, KeyEvent.VK_P, KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_R};
        for (int tecla : teclas) {
            registrar(panel, tecla, false);
            registrar(panel, tecla, true);
        }
    }
    private void registrar(JComponent panel, int tecla, boolean soltar) {
        String nombre = tecla + (soltar ? ":up" : ":down");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla, 0, soltar), nombre);
        panel.getActionMap().put(nombre, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (soltar) pulsadas.remove(tecla);
                else if (pulsadas.add(tecla)) acciones.add(tecla);
            }
        });
    }
    private boolean pulsada(int a, int b) { return pulsadas.contains(a) || pulsadas.contains(b); }
    public int ejeX() { return (pulsada(KeyEvent.VK_D, KeyEvent.VK_RIGHT) ? 1 : 0) - (pulsada(KeyEvent.VK_A, KeyEvent.VK_LEFT) ? 1 : 0); }
    public int ejeY() { return (pulsada(KeyEvent.VK_S, KeyEvent.VK_DOWN) ? 1 : 0) - (pulsada(KeyEvent.VK_W, KeyEvent.VK_UP) ? 1 : 0); }
    public boolean consumir(int tecla) { return acciones.remove(tecla); }
    public void limpiar() { pulsadas.clear(); acciones.clear(); }
}
