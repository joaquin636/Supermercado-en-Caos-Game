package com.supermercado.products;

import com.supermercado.obstacles.Obstaculo;
import java.awt.geom.Rectangle2D;

public final class Producto {
    public static final String FRUTAS = "FRUTAS";
    public static final String LACTEOS = "LACTEOS";
    public static final String LIMPIEZA = "LIMPIEZA";
    public static final int TAMANO = 24;
    private final String nombre;
    private final String categoria;
    private final int puntos;
    private boolean entregado;
    private boolean recogido;
    private final double x;
    private final double y;

    public Producto(String nombre, String categoria, double x, double y) {
        this.nombre = nombre; this.categoria = categoria;
        this.x = x; this.y = y; this.puntos = 100;
    }

    public void marcarRecogido() { if (!entregado) recogido = true; }
    public void marcarEntregado() { if (recogido) { entregado = true; recogido = false; } }
    public boolean estaDisponible() { return !entregado && !recogido; }
    public Rectangle2D.Double getBounds() { return new Rectangle2D.Double(x, y, TAMANO, TAMANO); }
    public double distanciaA(Rectangle2D otro) { return Obstaculo.distancia(getBounds(), otro); }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public int getPuntos() { return puntos; }
    public boolean isEntregado() { return entregado; }
    public boolean isRecogido() { return recogido; }
    public double getX() { return x; }
    public double getY() { return y; }
}
