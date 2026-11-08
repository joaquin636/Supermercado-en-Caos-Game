package com.supermercado.levels;

import com.supermercado.entities.*;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import java.util.*;
import java.util.function.Supplier;

/** Receta reutilizable: cada carga crea entidades nuevas, nunca comparte una partida. */
public final class ConfiguracionNivel {
    public record ProductoInicial(String nombre, String categoria, double x, double y) {
        public ProductoInicial {
            Objects.requireNonNull(nombre); Objects.requireNonNull(categoria);
            validarPosicion(x, y, Producto.TAMANO, Producto.TAMANO);
        }
        public Producto crear() { return new Producto(nombre, categoria, x, y); }
    }
    private final int numero;
    private final String identificacion;
    private final double tiempo;
    private final double jugadorX, jugadorY;
    private final boolean entregasEnOrden;
    private final int objetivoEntregas;
    private final List<ProductoInicial> productos;
    private final List<Supplier<? extends Obstaculo>> obstaculos;
    private final List<Supplier<? extends Personaje>> clientes;

    private ConfiguracionNivel(Builder b) {
        if (b.numero < 1 || b.numero > 6 || !Double.isFinite(b.tiempo) || b.tiempo <= 0)
            throw new IllegalArgumentException("Numero entre 1 y 6 y tiempo positivo requeridos.");
        validarPosicion(b.jugadorX, b.jugadorY, Personaje.TAMANO, Personaje.TAMANO);
        numero = b.numero; identificacion = Objects.requireNonNull(b.identificacion);
        tiempo = b.tiempo; jugadorX = b.jugadorX; jugadorY = b.jugadorY;
        entregasEnOrden = b.entregasEnOrden;
        productos = List.copyOf(b.productos); obstaculos = List.copyOf(b.obstaculos); clientes = List.copyOf(b.clientes);
        objetivoEntregas = b.objetivoEntregas == 0 ? productos.size() : b.objetivoEntregas;
        if (objetivoEntregas < 1 || objetivoEntregas > productos.size())
            throw new IllegalArgumentException("El objetivo debe corresponder a productos existentes.");
    }
    private static void validarPosicion(double x, double y, int ancho, int alto) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || ancho <= 0 || alto <= 0
                || x < 0 || y < 0 || x + ancho > Nivel.ANCHO || y + alto > Nivel.ALTO)
            throw new IllegalArgumentException("Entidad fuera del mapa o dimensiones invalidas.");
    }
    public static Builder crear(int numero, String identificacion) { return new Builder(numero, identificacion); }
    public int getNumero() { return numero; }
    public String getIdentificacion() { return identificacion; }
    public double getTiempo() { return tiempo; }
    public double getJugadorX() { return jugadorX; }
    public double getJugadorY() { return jugadorY; }
    public boolean isEntregasEnOrden() { return entregasEnOrden; }
    public int getObjetivoEntregas() { return objetivoEntregas; }
    public List<ProductoInicial> getProductos() { return productos; }
    public List<Obstaculo> crearObstaculos() {
        List<Obstaculo> nuevos = new ArrayList<>();
        for (Supplier<? extends Obstaculo> fabrica : obstaculos) {
            Obstaculo o = Objects.requireNonNull(fabrica.get(), "Fabrica sin obstaculo");
            validarPosicion(o.getX(), o.getY(), o.getAncho(), o.getAlto()); nuevos.add(o);
        }
        return nuevos;
    }
    public List<Personaje> crearClientes() {
        List<Personaje> nuevos = new ArrayList<>();
        for (Supplier<? extends Personaje> fabrica : clientes) {
            Personaje p = Objects.requireNonNull(fabrica.get(), "Fabrica sin cliente");
            validarPosicion(p.getX(), p.getY(), Personaje.TAMANO, Personaje.TAMANO); nuevos.add(p);
        }
        return nuevos;
    }
    /** Las fabricas deben devolver instancias nuevas; permiten configurar futuras estrategias. */
    public static final class Builder {
        private final int numero;
        private final String identificacion;
        private double tiempo = 180, jugadorX = 48, jugadorY = 42;
        private boolean entregasEnOrden;
        private int objetivoEntregas;
        private final List<ProductoInicial> productos = new ArrayList<>();
        private final List<Supplier<? extends Obstaculo>> obstaculos = new ArrayList<>();
        private final List<Supplier<? extends Personaje>> clientes = new ArrayList<>();
        private Builder(int numero, String identificacion) { this.numero = numero; this.identificacion = identificacion; }
        public Builder tiempo(double segundos) { tiempo = segundos; return this; }
        public Builder jugador(double x, double y) { jugadorX = x; jugadorY = y; return this; }
        public Builder entregasEnOrden(boolean valor) { entregasEnOrden = valor; return this; }
        public Builder objetivoEntregas(int cantidad) {
            if (cantidad < 1) throw new IllegalArgumentException("Objetivo positivo requerido");
            objetivoEntregas = cantidad; return this;
        }
        /** El orden de alta define la secuencia de categorias si entregasEnOrden es true. */
        public Builder producto(String nombre, String categoria, double x, double y) {
            productos.add(new ProductoInicial(nombre, categoria, x, y)); return this;
        }
        public Builder estante(String categoria, double x, double y) {
            Objects.requireNonNull(categoria); return obstaculo(() -> new Estante(categoria, x, y));
        }
        public Builder caja(double x, double y, int ancho, int alto) {
            return obstaculo(() -> new Obstaculo(x, y, ancho, alto));
        }
        public Builder obstaculo(Supplier<? extends Obstaculo> fabrica) {
            obstaculos.add(Objects.requireNonNull(fabrica)); return this;
        }
        public Builder cliente(double x, double y, int direccion) {
            return cliente(() -> new Cliente(x, y, direccion));
        }
        public Builder cliente(Supplier<? extends Personaje> fabrica) {
            clientes.add(Objects.requireNonNull(fabrica)); return this;
        }
        public ConfiguracionNivel construir() { return new ConfiguracionNivel(this); }
    }
}
