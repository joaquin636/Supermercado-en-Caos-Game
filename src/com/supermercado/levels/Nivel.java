package com.supermercado.levels;

import com.supermercado.entities.*;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Composicion: Nivel crea sus personajes, productos y obstaculos. */
public final class Nivel {
    public static final int ANCHO = 960;
    public static final int ALTO = 540;
    private final int numero;
    private final double tiempoLimite;
    private Jugador jugador;
    private final List<Personaje> personajes = new ArrayList<>();
    private final List<Obstaculo> obstaculos = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();

    public Nivel(int numero) {
        if (numero != 1) throw new IllegalArgumentException("Este hito implementa solamente el nivel 1.");
        this.numero = numero;
        tiempoLimite = 180;
        cargar();
    }

    public void cargar() {
        personajes.clear(); obstaculos.clear(); productos.clear();
        jugador = new Jugador("Repositor", 48, 42);
        personajes.add(jugador);
        personajes.add(new Cliente(240, 190, 0));
        personajes.add(new Cliente(520, 285, 2));
        personajes.add(new Cliente(830, 455, 2));
        obstaculos.add(new Estante(Producto.FRUTAS, 170, 60));
        obstaculos.add(new Estante(Producto.LACTEOS, 430, 60));
        obstaculos.add(new Estante(Producto.LIMPIEZA, 690, 60));
        obstaculos.add(new Obstaculo(310, 245, 90, 70));
        obstaculos.add(new Obstaculo(600, 345, 100, 70));
        productos.add(new Producto("Manzana", Producto.FRUTAS, 104, 220));
        productos.add(new Producto("Platano", Producto.FRUTAS, 430, 455));
        productos.add(new Producto("Leche", Producto.LACTEOS, 440, 270));
        productos.add(new Producto("Yogur", Producto.LACTEOS, 140, 455));
        productos.add(new Producto("Jabon", Producto.LIMPIEZA, 780, 235));
        productos.add(new Producto("Detergente", Producto.LIMPIEZA, 815, 405));
    }

    public void actualizarPersonajes() {
        for (Personaje personaje : personajes) {
            // Una unica llamada polimorfica para Jugador y Cliente, como pide el curso.
            personaje.update();
            boolean bloqueado = mover(personaje, personaje.getDesplazamientoX(), personaje.getDesplazamientoY());
            if (bloqueado && personaje instanceof Cliente) ((Cliente) personaje).cambiarDireccion();
        }
    }

    /** Separar los ejes permite deslizarse junto a una pared sin quedar pegado. */
    public boolean mover(Personaje p, double dx, double dy) {
        int pasos = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 4));
        boolean bloqueado = false;
        for (int i = 0; i < pasos; i++) {
            double x = p.getX() + dx / pasos;
            if (posicionLibre(x, p.getY())) p.setPosicion(x, p.getY()); else bloqueado = true;
            double y = p.getY() + dy / pasos;
            if (posicionLibre(p.getX(), y)) p.setPosicion(p.getX(), y); else bloqueado = true;
        }
        return bloqueado;
    }

    public boolean posicionLibre(double x, double y) {
        if (x < 0 || y < 0 || x + Personaje.TAMANO > ANCHO || y + Personaje.TAMANO > ALTO) return false;
        Rectangle2D r = new Rectangle2D.Double(x, y, Personaje.TAMANO, Personaje.TAMANO);
        for (Obstaculo o : obstaculos) if (o.bloqueaPaso() && o.getBounds().intersects(r)) return false;
        return true;
    }

    public boolean estaCompletado() {
        return !productos.isEmpty() && getEntregados() == productos.size();
    }
    public int getEntregados() {
        int total = 0;
        for (Producto p : productos) if (p.isEntregado()) total++;
        return total;
    }
    public Producto productoCercano() {
        Producto cercano = null;
        double menor = Jugador.ALCANCE;
        for (Producto p : productos) {
            double d = p.distanciaA(jugador.getBounds());
            if (p.estaDisponible() && d <= menor) { cercano = p; menor = d; }
        }
        return cercano;
    }
    public Estante estanteCercano() {
        Estante cercano = null;
        double menor = Jugador.ALCANCE;
        for (Obstaculo o : obstaculos) {
            if (o instanceof Estante && o.distanciaA(jugador.getBounds()) <= menor) {
                cercano = (Estante) o; menor = o.distanciaA(jugador.getBounds());
            }
        }
        return cercano;
    }
    public int getNumero() { return numero; }
    public double getTiempoLimite() { return tiempoLimite; }
    public Jugador getJugador() { return jugador; }
    public List<Personaje> getPersonajes() { return Collections.unmodifiableList(personajes); }
    public List<Obstaculo> getObstaculos() { return Collections.unmodifiableList(obstaculos); }
    public List<Producto> getProductos() { return Collections.unmodifiableList(productos); }
}
