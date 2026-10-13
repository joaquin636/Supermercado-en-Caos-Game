package com.supermercado.entities;

import java.awt.geom.Rectangle2D;

/** Base abstracta del UML. La velocidad se expresa en pixeles por segundo. */
public abstract class Personaje {
    public static final double PASO = 1.0 / 60.0;
    public static final int TAMANO = 30;
    private double x;
    private double y;
    private final double velocidad;
    private double desplazamientoX;
    private double desplazamientoY;

    protected Personaje(double x, double y, double velocidad) {
        this.x = x;
        this.y = y;
        this.velocidad = velocidad;
    }

    /** Cada subtipo decide su movimiento; Nivel resuelve las colisiones. */
    public abstract void update();

    protected final void solicitarMovimiento(double dx, double dy) {
        desplazamientoX = dx;
        desplazamientoY = dy;
    }

    public final double getX() { return x; }
    public final double getY() { return y; }
    public final double getVelocidad() { return velocidad; }
    public final double getDesplazamientoX() { return desplazamientoX; }
    public final double getDesplazamientoY() { return desplazamientoY; }
    public final void setPosicion(double x, double y) { this.x = x; this.y = y; }
    public final Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, TAMANO, TAMANO);
    }
}
