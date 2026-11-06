package com.supermercado.entities;

import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;

public final class Jugador extends Personaje {
    public static final double ALCANCE = 38;
    private final String nombre;
    private Producto productoEnMano;
    private int direccionX;
    private int direccionY;
    private double inmunidad;

    public Jugador(String nombre, double x, double y) {
        super(x, y, 210);
        this.nombre = nombre;
    }

    @Override
    public void update() {
        // Normalizar impide que la diagonal sea mas rapida que el movimiento recto.
        double longitud = Math.hypot(direccionX, direccionY);
        double factor = longitud == 0 ? 0 : getVelocidad() * PASO / longitud;
        solicitarMovimiento(direccionX * factor, direccionY * factor);
        inmunidad = Math.max(0, inmunidad - PASO);
    }

    public void setDireccion(int dx, int dy) {
        direccionX = Integer.signum(dx);
        direccionY = Integer.signum(dy);
    }

    public void recogerProducto(Producto p) {
        if (p != null && productoEnMano == null && p.estaDisponible()
                && p.distanciaA(getBounds()) <= ALCANCE) {
            p.marcarRecogido();
            productoEnMano = p;
        }
    }

    public boolean entregarProducto(Estante e) {
        if (productoEnMano == null || e == null
                || e.distanciaA(getBounds()) > ALCANCE) return false;
        if (!e.recibirProducto(productoEnMano)) return false;
        productoEnMano = null;
        return true;
    }

    /** Un choque solo penaliza una vez por segundo, no una vez por fotograma. */
    public boolean recibirImpacto() {
        if (inmunidad > 0) return false;
        inmunidad = 1.0;
        return true;
    }

    public String getNombre() { return nombre; }
    public Producto getProductoEnMano() { return productoEnMano; }
    public boolean esInmune() { return inmunidad > 0; }
}
