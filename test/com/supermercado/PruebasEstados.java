package com.supermercado;

import com.supermercado.game.Juego;
import com.supermercado.game.estados.EstadoResultado;
import com.supermercado.game.estados.EstadoJuego;
import com.supermercado.game.estados.EstadoMenu;
import com.supermercado.obstacles.Estante;
import com.supermercado.ui.GamePanel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/** Regresiones de State comprobadas desde el contrato del modelo y las acciones Swing. */
public final class PruebasEstados {
    private static int comprobaciones;
    private static final List<String> fallos = new ArrayList<>();

    private PruebasEstados() {}

    public static void main(String[] args) throws Exception { ejecutar(); }

    public static void ejecutar() throws Exception {
        comprobaciones = 0;
        fallos.clear();
        SwingUtilities.invokeAndWait(() -> {
            accionesEnPausa();
            transicionesDeResultado();
            menuYReinicio();
            resultadoUnico();
            comandosDelPanel();
            comandosDeMenu();
        });
        if (!fallos.isEmpty()) throw new AssertionError(String.join("\n", fallos));
        System.out.println("OK State: " + comprobaciones + " comprobaciones de permisos y transiciones.");
    }

    private static void verificar(boolean valor, String mensaje) {
        comprobaciones++;
        if (!valor) fallos.add(mensaje);
    }

    private static Juego nuevo() {
        Juego juego = PartidasPrueba.crear();
        juego.iniciarPartida();
        return juego;
    }

    private static void pulsar(GamePanel panel, int tecla) {
        Object accion = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(tecla, 0, false));
        panel.getActionMap().get(accion).actionPerformed(new ActionEvent(panel, 0, "prueba"));
        panel.avanzarFotograma(0);
        Object liberar = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(tecla, 0, true));
        panel.getActionMap().get(liberar).actionPerformed(new ActionEvent(panel, 0, "prueba"));
        panel.avanzarFotograma(0);
    }

    private static void recogerPrimero(Juego juego) {
        var producto = juego.getNivel().getProductos().get(0);
        juego.getNivel().getJugador().setPosicion(producto.getX(), producto.getY());
        juego.interactuar();
    }

    private static void completar(Juego juego) {
        for (var producto : juego.getNivel().getProductos()) {
            juego.getNivel().getJugador().setPosicion(producto.getX(), producto.getY());
            juego.interactuar();
            var estante = juego.getNivel().getObstaculos().stream()
                    .filter(Estante.class::isInstance).map(Estante.class::cast)
                    .filter(e -> e.getCategoria().equals(producto.getCategoria())).findFirst().orElseThrow();
            juego.getNivel().getJugador().setPosicion(estante.getX() + 15, estante.getY() + estante.getAlto() + 2);
            juego.interactuar();
        }
    }

    private static void accionesEnPausa() {
        Juego juego = nuevo();
        GamePanel panel = new GamePanel(juego, false);
        var jugador = juego.getNivel().getJugador();
        var producto = juego.getNivel().getProductos().get(0);
        jugador.setPosicion(producto.getX(), producto.getY());
        pulsar(panel, KeyEvent.VK_P);
        pulsar(panel, KeyEvent.VK_E);
        verificar(juego.getEstado() == Juego.Estado.PAUSA && jugador.getProductoEnMano() == null
                && producto.estaDisponible(), "E durante pausa debe impedir recoger");
        pulsar(panel, KeyEvent.VK_P);
        pulsar(panel, KeyEvent.VK_E);
        verificar(jugador.getProductoEnMano() == producto, "Al reanudar permite recoger");
        jugador.setPosicion(185, 138);
        pulsar(panel, KeyEvent.VK_ESCAPE);
        double tiempo = juego.getTiempoRestante();
        pulsar(panel, KeyEvent.VK_E);
        verificar(juego.getPuntuacion() == 0 && juego.getNivel().getEntregados() == 0
                && jugador.getProductoEnMano() == producto, "E durante pausa debe impedir entregar y sumar");
        pulsar(panel, KeyEvent.VK_Q);
        juego.actualizar();
        verificar(jugador.getProductoEnMano() == producto && juego.getTiempoRestante() == tiempo,
                "Pausa conserva producto y reloj");
        pulsar(panel, KeyEvent.VK_ESCAPE);
        pulsar(panel, KeyEvent.VK_E);
        verificar(producto.isEntregado() && juego.getPuntuacion() == 100, "Entrega unica despues de reanudar");
    }

    private static void transicionesDeResultado() {
        Juego derrota = nuevo();
        derrota.terminarPartida();
        verificar(!derrota.avanzarNivel() && derrota.getNivelActual() == 1
                && derrota.getEstado() == Juego.Estado.DERROTA, "Una derrota impide avanzar");
        verificar(derrota.getMensaje().contains("ENTER"), "Derrota explica reintento");
        verificar(derrota.reintentarNivel() && derrota.getPuntuacion() == 0, "Derrota permite reintentar");
        completar(derrota);
        verificar(derrota.getEstado() == Juego.Estado.VICTORIA, "Reintento puede ganarse");
        verificar(!derrota.reintentarNivel() && derrota.getPuntuacion() == 600, "Victoria impide reintento y conserva puntos");
        verificar(derrota.getMensaje().contains("ENTER"), "Victoria explica avance");
        derrota.terminarPartida();
        derrota.terminarPartida(false);
        verificar(derrota.getEstado() == Juego.Estado.VICTORIA, "Victoria es estable ante finalizacion repetida");
        verificar(derrota.avanzarNivel() && derrota.getNivelActual() == 2 && derrota.getPuntuacion() == 600,
                "Victoria permite avanzar una sola vez");
        verificar(!derrota.avanzarNivel(), "Una pulsacion no salta dos niveles");
        derrota.terminarPartida();
        verificar(derrota.getEstado() == Juego.Estado.DERROTA, "La victoria anterior no impide perder el nivel siguiente");
    }

    private static void menuYReinicio() {
        Juego juego = PartidasPrueba.crear();
        var producto = juego.getNivel().getProductos().get(0);
        juego.getNivel().getJugador().setPosicion(producto.getX(), producto.getY());
        juego.interactuar();
        verificar(producto.estaDisponible(), "Menu impide interactuar");
        juego.terminarPartida();
        juego.terminarPartida(false);
        verificar(juego.getEstado() == Juego.Estado.MENU, "Finalizar desde menu conserva el menu");
        GamePanel panel = new GamePanel(juego, false);
        pulsar(panel, KeyEvent.VK_ENTER);
        verificar(juego.getEstado() == Juego.Estado.JUGANDO && juego.getTiempoRestante() == 180,
                "Enter inicia con tiempo completo");
        completar(juego);
        pulsar(panel, KeyEvent.VK_R);
        verificar(juego.getNivelActual() == 1 && juego.getPuntuacion() == 0
                && juego.getNivel().getEntregados() == 0, "R inicia una campana limpia");
    }

    private static void resultadoUnico() {
        Juego juego = nuevo();
        juego.cambiarEstado(new EstadoResultado(juego, true));
        verificar(juego.getEstado() == Juego.Estado.VICTORIA, "EstadoResultado determina victoria sin bandera duplicada");
        juego.cambiarEstado(new EstadoResultado(juego, false));
        verificar(juego.getEstado() == Juego.Estado.DERROTA, "EstadoResultado determina derrota");
        var estado = juego.getEstadoActual();
        boolean rechazo = false;
        try { juego.cambiarEstado(null); } catch (NullPointerException e) { rechazo = true; }
        verificar(rechazo && juego.getEstadoActual() == estado, "Rechaza estado nulo sin corromper el estado actual");
    }
    private static void comandosDelPanel() {
        Juego juego = PartidasPrueba.crear();
        List<Integer> recibidas = new ArrayList<>();
        juego.cambiarEstado(new EstadoJuego() {
            @Override public void actualizar() {}
            @Override public void manejarTeclado(int tecla, boolean presionada) {
                if (presionada) recibidas.add(tecla);
            }
        });
        GamePanel panel = new GamePanel(juego, false);
        int[] teclas = {KeyEvent.VK_E, KeyEvent.VK_Q, KeyEvent.VK_P,
                KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_R};
        for (int tecla : teclas) pulsar(panel, tecla);
        verificar(recibidas.equals(List.of(KeyEvent.VK_E, KeyEvent.VK_Q, KeyEvent.VK_ESCAPE,
                KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_R)),
                "El mapa real del panel delega una vez cada accion al estado activo");
    }

    private static void comandosDeMenu() {
        Juego juego = nuevo();
        completar(juego);
        juego.cambiarEstado(new EstadoMenu(juego));
        juego.manejarTeclado(KeyEvent.VK_ENTER, true);
        verificar(juego.getEstado() == Juego.Estado.JUGANDO && juego.getPuntuacion() == 0
                && juego.getNivel().getEntregados() == 0, "EstadoMenu inicia mediante la operacion de campana limpia");

        Juego menu = PartidasPrueba.crear();
        GamePanel panel = new GamePanel(menu, false);
        pulsar(panel, KeyEvent.VK_DOWN);
        pulsar(panel, KeyEvent.VK_ENTER);
        verificar(menu.getEstado() == Juego.Estado.MENU, "Instrucciones no inicia la simulacion");
        pulsar(panel, KeyEvent.VK_ESCAPE);
        pulsar(panel, KeyEvent.VK_UP);
        pulsar(panel, KeyEvent.VK_ENTER);
        verificar(menu.getEstado() == Juego.Estado.JUGANDO && menu.getTiempoRestante() == 180,
                "Volver de instrucciones permite iniciar con tiempo completo");

        double tiempo = menu.getTiempoRestante();
        pulsar(panel, KeyEvent.VK_R);
        pulsar(panel, KeyEvent.VK_ENTER);
        verificar(menu.getEstado() == Juego.Estado.JUGANDO && menu.getTiempoRestante() == tiempo,
                "Enter y R durante partida no reinician ni saltan niveles");
    }

}
