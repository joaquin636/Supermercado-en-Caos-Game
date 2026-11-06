package com.supermercado;

import com.supermercado.ui.GameWindow;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameWindow::new);
    }
}
