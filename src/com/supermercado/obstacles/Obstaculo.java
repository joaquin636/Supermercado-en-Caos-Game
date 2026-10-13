package com.supermercado.obstacles;

import java.awt.geom.Rectangle2D;

/** Caja u otro objeto solido. Estante especializa esta clase mediante herencia. */
public class Obstaculo {
    private final double x;
    private final double y;
    private final int ancho;
    private final int alto;

    public Obstaculo(double x, double y, int ancho, int alto) {
        this.x = x; this.y = y; this.ancho = ancho; this.alto = alto;
    }

    public boolean bloqueaPaso() { return true; }
    public Rectangle2D.Double getBounds() { return new Rectangle2D.Double(x, y, ancho, alto); }
    public double distanciaA(Rectangle2D otro) { return distancia(getBounds(), otro); }

    /** Distancia minima entre dos rectangulos: cero cuando se tocan. */
    public static double distancia(Rectangle2D a, Rectangle2D b) {
        double dx = Math.max(0, Math.max(a.getMinX() - b.getMaxX(), b.getMinX() - a.getMaxX()));
        double dy = Math.max(0, Math.max(a.getMinY() - b.getMaxY(), b.getMinY() - a.getMaxY()));
        return Math.hypot(dx, dy);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
}
