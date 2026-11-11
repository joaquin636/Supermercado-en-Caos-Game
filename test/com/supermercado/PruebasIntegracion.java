package com.supermercado;

import com.supermercado.game.Juego;
import com.supermercado.levels.Nivel;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;
import com.supermercado.ui.GamePanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.SwingUtilities;

final class PruebasIntegracion {
    private static int comprobaciones;
    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        comprobaciones++;
    }

    public static void main(String[] args) throws Exception { ejecutar(); }

    static void ejecutar() throws Exception {
        comprobaciones = 0;
        SwingUtilities.invokeAndWait(PruebasIntegracion::probarHud);
        recorrerCampana();
        System.out.println("OK integracion: " + comprobaciones + " comprobaciones de HUD, seis niveles y resultados.");
    }

    private static int pixelesDestaque(GamePanel panel) {
        BufferedImage imagen = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        panel.paint(g);
        g.dispose();
        int cuenta = 0;
        for (int y = 341; y < 371; y++) {
            for (int x = 24; x < 610; x++) {
                if ((imagen.getRGB(x, y) & 0xffffff) == 0xe5a94a) cuenta++;
            }
        }
        return cuenta;
    }

    private static void provocarChoque(Juego juego) {
        var cliente = juego.getNivel().getPersonajes().get(1);
        juego.getNivel().getJugador().setPosicion(cliente.getX(), cliente.getY());
        juego.actualizar();
    }

    private static void probarHud() {
        Juego juego = PartidasPrueba.crear();
        GamePanel panel = new GamePanel(juego, false);
        panel.setSize(640, 400);
        panel.addNotify();
        try {
            juego.iniciarPartida();
            provocarChoque(juego);
            exigir(pixelesDestaque(panel) > 0, "El panel pinta el evento de choque recibido");
            juego.interactuar();
            exigir(pixelesDestaque(panel) == 0, "La ayuda posterior sustituye al evento anterior");
        } finally {
            panel.removeNotify();
        }
        juego.iniciarPartida();
        provocarChoque(juego);
        exigir(pixelesDestaque(panel) == 0, "El panel retirado no recibe eventos");
        panel.addNotify();
        try {
            exigir(pixelesDestaque(panel) == 0, "Al volver no muestra eventos obsoletos");
            juego.iniciarPartida();
            provocarChoque(juego);
            exigir(pixelesDestaque(panel) > 0, "El panel reconectado vuelve a mostrar eventos");
        } finally {
            panel.removeNotify();
        }
        System.out.println("OK HUD Observer: aviso pintado en Swing, reemplazo, retirada y reconexion.");
    }

    private static Estante estante(Nivel nivel, Producto producto) {
        return nivel.getObstaculos().stream().filter(Estante.class::isInstance).map(Estante.class::cast)
                .filter(e -> e.getCategoria().equals(producto.getCategoria())).findFirst().orElseThrow();
    }

    private static void recorrerCampana() throws Exception {
        Juego juego = PartidasPrueba.crear();
        juego.iniciarPartida();
        List<String> secciones = List.of(Producto.FRUTAS, Producto.LACTEOS, Producto.LIMPIEZA,
                Producto.FRUTAS, Producto.LACTEOS, Producto.LIMPIEZA);
        List<String> cierre = List.of(Producto.LACTEOS, Producto.FRUTAS, Producto.LIMPIEZA,
                Producto.LACTEOS, Producto.FRUTAS, Producto.LIMPIEZA);
        for (int n = 1; n <= 6; n++) {
            Nivel nivel = juego.getNivel();
            exigir(nivel.getNumero() == n, "Campana sin saltos");
            if (n >= 3) {
                PruebasJuan.capturar(juego, "nivel-" + n + "-inicio.png");
                int base = juego.getPuntuacion();
                for (int paso = 0; paso < 12000 && juego.getEstado() == Juego.Estado.JUGANDO; paso++) juego.actualizar();
                exigir(juego.getEstado() == Juego.Estado.DERROTA && juego.reintentarNivel(),
                        "Derrota por reloj y reintento en nivel " + n);
                nivel = juego.getNivel();
                exigir(nivel.getNumero() == n && nivel.getEntregados() == 0 && juego.getPuntuacion() == base,
                        "Reintento conserva puntos anteriores y repone productos");
            }
            if (n == 4) {
                Producto leche = nivel.getProductos().stream().filter(p -> p.getCategoria().equals(Producto.LACTEOS))
                        .findFirst().orElseThrow();
                PruebasJuan.caminar(juego, leche.getBounds());
                juego.interactuar();
                PruebasJuan.caminar(juego, estante(nivel, leche).getBounds());
                juego.interactuar();
                exigir(nivel.getJugador().getProductoEnMano() == leche && !leche.isEntregado()
                        && juego.getPuntuacion() == 1800 && nivel.getEntregados() == 0,
                        "Rechazar lacteos antes de frutas conserva la mano y los puntos");
                juego.soltarProducto();
                exigir(leche.estaDisponible(), "Se puede recuperar el producto que se dejo");
            }
            double tiempoPausa = juego.getTiempoRestante();
            double xPausa = nivel.getJugador().getX();
            juego.pausar();
            juego.setDireccion(1, 0);
            for (int paso = 0; paso < 60; paso++) juego.actualizar();
            exigir(juego.getEstado() == Juego.Estado.PAUSA && juego.getTiempoRestante() == tiempoPausa
                    && nivel.getJugador().getX() == xPausa, "State pausa reloj y movimiento en nivel " + n);
            juego.alternarPausa();
            juego.setDireccion(0, 0);
            int finalesAntes = juego.getEstadisticas().getNivelesFinalizados();
            for (int i = 0; i < nivel.getProductos().size(); i++) {
                Producto p = nivel.getProductos().get(i);
                if (n == 4 || n == 6) {
                    List<String> esperada = n == 4 ? secciones : cierre;
                    exigir(nivel.getSiguienteProducto().getCategoria().equals(esperada.get(i)),
                            "Categoria esperada en entrega " + (i + 1) + " del nivel " + n);
                }
                PruebasJuan.caminar(juego, p.getBounds());
                juego.interactuar();
                exigir(nivel.getJugador().getProductoEnMano() == p, "Recogida caminando: " + p.getNombre());
                PruebasJuan.caminar(juego, estante(nivel, p).getBounds());
                juego.interactuar();
                exigir(p.isEntregado(), "Entrega caminando: " + p.getNombre());
            }
            exigir(juego.getEstado() == Juego.Estado.VICTORIA && nivel.getEntregados() == 6
                    && juego.getPuntuacion() == 600 * n, "Victoria y puntaje del nivel " + n);
            juego.interactuar();
            juego.terminarPartida();
            exigir(juego.getEstadisticas().getNivelesFinalizados() == finalesAntes + 1,
                    "Una sola notificacion de victoria");
            System.out.printf("Nivel %d: 6/6, %d puntos acumulados, %.1f s restantes.%n",
                    n, juego.getPuntuacion(), juego.getTiempoRestante());
            if (n >= 3) PruebasJuan.capturar(juego, "nivel-" + n + "-victoria.png");
            if (n < 6) {
                exigir(!juego.isCampanaFinalizada() && juego.avanzarNivel(), "Avance tras victoria sin finalizar antes del sexto");
            } else {
                exigir(juego.isCampanaFinalizada() && !juego.avanzarNivel() && !juego.reintentarNivel(),
                        "El sexto finaliza una sola vez y no permite nivel 7 ni reintento de victoria");
                exigir(juego.getEstadisticas().getEntregasCorrectas() == 6, "Estadisticas reflejan las seis entregas del intento final");
                juego.iniciarPartida();
                exigir(juego.getNivelActual() == 1 && juego.getPuntuacion() == 0 && !juego.isCampanaFinalizada()
                        && juego.getNivel().getEntregados() == 0, "Nueva campana limpia tras finalizacion");
            }
        }
    }
}
