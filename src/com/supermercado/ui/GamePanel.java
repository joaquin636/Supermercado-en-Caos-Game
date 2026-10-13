package com.supermercado.ui;

import com.supermercado.entities.*;
import com.supermercado.game.Juego;
import com.supermercado.input.ControlTeclado;
import com.supermercado.levels.Nivel;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/** Toda la presentacion vive aqui; las ocho clases del UML conservan la logica. */
public final class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int ANCHO = 1280, ALTO = 800;
    private static final int MAPA_X = 32, MAPA_Y = 176;
    private static final Color TINTA = new Color(26, 49, 49);
    private static final Color VERDE = new Color(25, 112, 91);
    private static final Color FONDO = new Color(245, 244, 235);
    private final Juego juego;
    private final ControlTeclado teclado = new ControlTeclado();
    private final Timer timer;
    private final boolean automatico;
    private long ultimoTick = System.nanoTime();
    private double acumulador;

    public GamePanel() { this(new Juego(), true); }
    public GamePanel(Juego juego, boolean automatico) {
        this.juego = juego;
        this.automatico = automatico;
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setMinimumSize(new Dimension(800, 500));
        setBackground(FONDO);
        teclado.instalar(this);
        timer = new Timer(16, e -> {
            long ahora = System.nanoTime();
            avanzarFotograma((ahora - ultimoTick) / 1_000_000_000.0);
            ultimoTick = ahora;
        });
        timer.setCoalesce(true);
    }
    @Override public void addNotify() {
        super.addNotify(); ultimoTick = System.nanoTime();
        if (automatico) timer.start();
    }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    /** Se separa del Timer para poder comprobar controles y simulacion sin una ventana. */
    public void avanzarFotograma(double segundos) {
        boolean confirmar = teclado.consumir(KeyEvent.VK_ENTER);
        boolean reiniciar = teclado.consumir(KeyEvent.VK_R);
        boolean pausa = teclado.consumir(KeyEvent.VK_P);
        pausa |= teclado.consumir(KeyEvent.VK_ESCAPE);
        boolean interactuar = teclado.consumir(KeyEvent.VK_E);
        Juego.Estado estado = juego.getEstado();
        if ((confirmar && estado != Juego.Estado.JUGANDO && estado != Juego.Estado.PAUSA)
                || (reiniciar && (estado == Juego.Estado.VICTORIA || estado == Juego.Estado.DERROTA))) {
            juego.iniciarPartida(); teclado.limpiar(); acumulador = 0;
        } else if (pausa) juego.alternarPausa();
        juego.setDireccion(teclado.ejeX(), teclado.ejeY());
        if (interactuar) juego.interactuar();
        if (juego.getEstado() == Juego.Estado.JUGANDO) {
            acumulador += Math.min(Math.max(segundos, 0), .15);
            while (acumulador + 1e-10 >= Personaje.PASO) {
                juego.actualizar(); acumulador -= Personaje.PASO;
            }
        } else acumulador = 0;
        repaint();
    }

    public void perderFoco() {
        teclado.limpiar(); juego.setDireccion(0, 0); juego.pausar(); acumulador = 0;
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double escala = Math.min(getWidth() / (double) ANCHO, getHeight() / (double) ALTO);
        g.translate((getWidth() - ANCHO * escala) / 2, (getHeight() - ALTO * escala) / 2);
        g.scale(escala, escala);
        g.setColor(FONDO); g.fillRect(0, 0, ANCHO, ALTO);
        cabecera(g);
        mapa(g);
        lateral(g);
        texto(g, juego.getMensaje(), 36, 755, 15, TINTA, false);
        texto(g, "WASD / FLECHAS  mover     E  recoger / entregar     P / ESC  pausa", 36, 782, 12, new Color(90, 104, 98), false);
        if (juego.getEstado() != Juego.Estado.JUGANDO) pantallaEstado(g);
        g.dispose();
    }

    private void cabecera(Graphics2D g) {
        texto(g, "SUPERMERCADO", 32, 42, 13, VERDE, true);
        texto(g, "En caos", 30, 83, 39, TINTA, true);
        texto(g, "Un pasillo. Seis productos. Todo en su lugar.", 32, 112, 15, new Color(101, 113, 104), false);
        tarjeta(g, 470, 30, 235, 91, Color.WHITE);
        texto(g, "PUNTUACIÓN", 492, 56, 11, VERDE, true);
        texto(g, String.format("%03d", juego.getPuntuacion()), 492, 94, 31, TINTA, true);
        tarjeta(g, 721, 30, 245, 91, Color.WHITE);
        texto(g, "TIEMPO RESTANTE", 743, 56, 11, VERDE, true);
        int segundos = (int) Math.ceil(juego.getTiempoRestante());
        Color reloj = segundos <= 30 ? new Color(194, 64, 47) : TINTA;
        texto(g, String.format("%02d:%02d", segundos / 60, segundos % 60), 743, 94, 31, reloj, true);
        tarjeta(g, 982, 30, 266, 91, VERDE);
        texto(g, "NIVEL 01", 1004, 56, 11, new Color(202, 237, 211), true);
        texto(g, "Turno de apertura", 1004, 91, 21, Color.WHITE, true);
        texto(g, "PLANO DEL SUPERMERCADO", 34, 157, 11, VERDE, true);
        texto(g, "TÚ: AZUL     CLIENTES: CORAL", 742, 157, 11, new Color(102, 112, 108), true);
    }

    private void mapa(Graphics2D original) {
        Graphics2D g = (Graphics2D) original.create();
        g.translate(MAPA_X, MAPA_Y);
        g.setColor(new Color(231, 234, 223)); g.fillRoundRect(-2, -2, Nivel.ANCHO + 4, Nivel.ALTO + 4, 18, 18);
        g.setClip(new RoundRectangle2D.Double(0, 0, Nivel.ANCHO, Nivel.ALTO, 16, 16));
        g.setColor(new Color(250, 249, 239)); g.fillRect(0, 0, Nivel.ANCHO, Nivel.ALTO);
        g.setColor(new Color(236, 235, 223));
        for (int x = 0; x <= Nivel.ANCHO; x += 48) g.drawLine(x, 0, x, Nivel.ALTO);
        for (int y = 0; y <= Nivel.ALTO; y += 48) g.drawLine(0, y, Nivel.ANCHO, y);
        texto(g, "ENTRADA", 26, 24, 10, new Color(133, 143, 125), true);
        for (Obstaculo o : juego.getNivel().getObstaculos()) dibujarObstaculo(g, o);
        for (Producto p : juego.getNivel().getProductos()) if (p.estaDisponible()) dibujarProducto(g, p);
        for (Personaje p : juego.getNivel().getPersonajes()) dibujarPersonaje(g, p);
        g.dispose();
    }
    private void dibujarObstaculo(Graphics2D g, Obstaculo o) {
        int x = (int) o.getX(), y = (int) o.getY(), w = o.getAncho(), h = o.getAlto();
        g.setColor(new Color(0, 0, 0, 24)); g.fillRoundRect(x + 3, y + 6, w, h, 10, 10);
        if (o instanceof Estante) {
            Estante e = (Estante) o;
            Color color = colorCategoria(e.getCategoria());
            g.setColor(new Color(236, 237, 225)); g.fillRoundRect(x, y, w, h, 9, 9);
            g.setColor(color); g.fillRoundRect(x, y, w, 28, 9, 9);
            texto(g, e.getCategoria(), x + 12, y + 19, 12, Color.WHITE, true);
            for (int i = 0; i < 7; i++) {
                g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 110));
                g.fillRoundRect(x + 12 + i * 23, y + 38, 15, 23, 4, 4);
            }
            texto(g, e.getRecibidos().size() + "/2", x + w - 33, y + 19, 11, Color.WHITE, true);
            Producto mano = juego.getNivel().getJugador().getProductoEnMano();
            if (mano != null && e.aceptaProducto(mano)) {
                g.setColor(color); g.setStroke(new BasicStroke(2));
                g.drawRoundRect(x - 5, y - 5, w + 10, h + 10, 12, 12); g.setStroke(new BasicStroke(1));
            }
        } else {
            g.setColor(new Color(189, 148, 100)); g.fillRoundRect(x, y, w, h, 6, 6);
            g.setColor(new Color(219, 183, 136)); g.fillRect(x + w / 2 - 7, y, 14, h);
            texto(g, "CAJAS", x + 12, y + h - 12, 11, new Color(89, 64, 36), true);
        }
    }
    private void dibujarProducto(Graphics2D g, Producto p) {
        int x = (int) p.getX(), y = (int) p.getY();
        g.setColor(new Color(0, 0, 0, 22)); g.fillOval(x - 4, y + 17, 33, 12);
        g.setColor(Color.WHITE); g.fillRoundRect(x - 4, y - 4, 32, 32, 10, 10);
        g.setColor(colorCategoria(p.getCategoria())); g.fillRoundRect(x, y, 24, 24, 7, 7);
        texto(g, p.getCategoria().substring(0, 1), x + 7, y + 17, 14, Color.WHITE, true);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int ancho = g.getFontMetrics().stringWidth(p.getNombre());
        texto(g, p.getNombre(), x + 12 - ancho / 2, y + 44, 11, TINTA, false);
        if (juego.getNivel().productoCercano() == p && juego.getNivel().getJugador().getProductoEnMano() == null) {
            g.setColor(VERDE); g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x - 7, y - 7, 38, 38, 12, 12); g.setStroke(new BasicStroke(1));
        }
    }
    private void dibujarPersonaje(Graphics2D g, Personaje p) {
        int x = (int) p.getX(), y = (int) p.getY();
        boolean jugador = p instanceof Jugador;
        g.setColor(new Color(0, 0, 0, 28)); g.fillOval(x - 2, y + 23, 35, 13);
        g.setColor(jugador ? new Color(55, 118, 208) : new Color(219, 108, 88));
        g.fillRoundRect(x, y, 30, 30, 11, 11);
        g.setColor(new Color(254, 226, 191)); g.fillOval(x + 7, y + 3, 16, 15);
        g.setColor(TINTA); g.fillOval(x + 11, y + 9, 2, 2); g.fillOval(x + 18, y + 9, 2, 2);
        if (jugador) {
            Jugador j = (Jugador) p;
            texto(g, "TÚ", x + 6, y - 9, 10, new Color(44, 91, 161), true);
            if (j.esInmune()) { g.setColor(new Color(244, 169, 65)); g.setStroke(new BasicStroke(3)); g.drawOval(x - 6, y - 6, 42, 42); g.setStroke(new BasicStroke(1)); }
            if (j.getProductoEnMano() != null) { g.setColor(colorCategoria(j.getProductoEnMano().getCategoria())); g.fillRoundRect(x + 23, y + 16, 15, 15, 4, 4); }
        }
    }
    private void lateral(Graphics2D g) {
        tarjeta(g, 1016, 176, 232, 540, Color.WHITE);
        texto(g, "TU MISIÓN", 1036, 205, 12, VERDE, true);
        texto(g, "Cada cosa", 1036, 240, 23, TINTA, true);
        texto(g, "en su lugar.", 1036, 268, 23, TINTA, true);
        texto(g, "Recoge un producto y", 1036, 299, 14, TINTA, false);
        texto(g, "llévalo a su categoría.", 1036, 320, 14, TINTA, false);
        int entregados = juego.getNivel().getEntregados();
        texto(g, "ENTREGADOS", 1036, 357, 11, VERDE, true);
        texto(g, entregados + " / 6", 1036, 394, 29, TINTA, true);
        g.setColor(new Color(229, 237, 229)); g.fillRoundRect(1036, 410, 190, 8, 8, 8);
        g.setColor(VERDE); g.fillRoundRect(1036, 410, 190 * entregados / 6, 8, 8, 8);
        texto(g, "EN TU MANO", 1036, 454, 11, VERDE, true);
        Producto p = juego.getNivel().getJugador().getProductoEnMano();
        texto(g, p == null ? "Sin producto" : p.getNombre(), 1036, 482, 19, TINTA, true);
        texto(g, p == null ? "Pulsa E cerca de uno." : p.getCategoria(), 1036, 505, 12, p == null ? TINTA : colorCategoria(p.getCategoria()), false);
        g.setColor(new Color(231, 236, 228)); g.drawLine(1036, 531, 1226, 531);
        texto(g, "+100 por entrega", 1036, 562, 15, VERDE, true);
        texto(g, "Cliente: -5 segundos", 1036, 589, 14, new Color(179, 76, 59), false);
        texto(g, "Solo llevas un producto.", 1036, 629, 12, TINTA, false);
        texto(g, "P / ESC para pausar.", 1036, 650, 12, TINTA, false);
        texto(g, "Evita cajas y estantes.", 1036, 671, 12, TINTA, false);
    }
    private void pantallaEstado(Graphics2D g) {
        g.setColor(new Color(14, 35, 32, 178)); g.fillRect(0, 0, ANCHO, ALTO);
        tarjeta(g, 350, 200, 580, 380, FONDO);
        Juego.Estado e = juego.getEstado();
        String etiqueta, titulo, linea1, linea2, boton;
        if (e == Juego.Estado.MENU) {
            etiqueta = "TURNO 01 / REPOSICIÓN"; titulo = "¿Listo para el caos?";
            linea1 = "Organiza 6 productos en 3 minutos.";
            linea2 = "WASD o flechas para moverte. E para interactuar."; boton = "ENTER  ·  COMENZAR";
        } else if (e == Juego.Estado.PAUSA) {
            etiqueta = "RELOJ DETENIDO"; titulo = "Una pequeña pausa";
            linea1 = "La partida y los clientes están detenidos.";
            linea2 = "También pausamos cuando cambias de ventana."; boton = "P / ESC  ·  CONTINUAR";
        } else if (e == Juego.Estado.VICTORIA) {
            etiqueta = "NIVEL COMPLETADO"; titulo = "Todo en su lugar";
            linea1 = "Entregaste los 6 productos. Puntaje: " + juego.getPuntuacion();
            linea2 = "Este hito incluye un nivel. ¡Puedes volver a jugar!"; boton = "ENTER / R  ·  NUEVA PARTIDA";
        } else {
            etiqueta = "FIN DEL TURNO"; titulo = "Se terminó el tiempo";
            linea1 = "Entregados: " + juego.getNivel().getEntregados() + "/6. Puntaje: " + juego.getPuntuacion();
            linea2 = "Busca rutas cortas y evita chocar con los clientes."; boton = "ENTER / R  ·  REINTENTAR";
        }
        centrado(g, etiqueta, 250, 12, VERDE, true);
        centrado(g, titulo, 306, 34, TINTA, true);
        centrado(g, linea1, 352, 17, TINTA, false);
        centrado(g, linea2, 385, 15, TINTA, false);
        tarjeta(g, 405, 427, 470, 60, VERDE);
        centrado(g, boton, 465, 16, Color.WHITE, true);
        centrado(g, "FRUTAS  /  LÁCTEOS  /  LIMPIEZA", 535, 12, new Color(105, 117, 106), true);
    }
    private static Color colorCategoria(String categoria) {
        if (Producto.FRUTAS.equals(categoria)) return new Color(57, 145, 97);
        if (Producto.LACTEOS.equals(categoria)) return new Color(67, 136, 186);
        return new Color(161, 111, 180);
    }
    private static void tarjeta(Graphics2D g, int x, int y, int w, int h, Color c) { g.setColor(c); g.fillRoundRect(x, y, w, h, 18, 18); }
    private static void texto(Graphics2D g, String s, int x, int y, int size, Color c, boolean bold) {
        g.setColor(c); g.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size)); g.drawString(s, x, y);
    }
    private static void centrado(Graphics2D g, String s, int y, int size, Color c, boolean bold) {
        g.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        texto(g, s, (ANCHO - g.getFontMetrics().stringWidth(s)) / 2, y, size, c, bold);
    }
}
