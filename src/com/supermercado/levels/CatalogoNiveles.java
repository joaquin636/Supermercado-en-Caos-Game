package com.supermercado.levels;

import com.supermercado.products.Producto;
import com.supermercado.entities.Cliente;
import com.supermercado.entities.movimiento.PatrullaConPausas;
import java.util.*;

/** Catalogo explicito: nunca sustituye un nivel faltante por otro inventado. */
public final class CatalogoNiveles {
    public static final int ULTIMO_NIVEL = 6;
    private final Map<Integer, ConfiguracionNivel> niveles;
    
    public CatalogoNiveles(Collection<ConfiguracionNivel> configuraciones) {
        Map<Integer, ConfiguracionNivel> mapa = new TreeMap<>();
        for (ConfiguracionNivel c : configuraciones)
            if (mapa.put(c.getNumero(), c) != null) throw new IllegalArgumentException("Nivel duplicado: " + c.getNumero());
        if (!mapa.containsKey(1)) throw new IllegalArgumentException("El catalogo necesita el nivel 1.");
        niveles = Collections.unmodifiableMap(mapa);
    }
    
    public boolean contiene(int numero) { return niveles.containsKey(numero); }
    
    public ConfiguracionNivel obtener(int numero) {
        ConfiguracionNivel c = niveles.get(numero);
        if (c == null) throw new IllegalArgumentException("Configuracion pendiente del nivel " + numero);
        return c;
    }
    
    public static CatalogoNiveles predeterminado() {
        ConfiguracionNivel uno = ConfiguracionNivel.crear(1, "Turno de reposicion")
            .tiempo(180).jugador(48, 42)
            .cliente(240, 190, 0).cliente(520, 285, 2).cliente(830, 455, 2)
            .estante(Producto.FRUTAS, 170, 60).estante(Producto.LACTEOS, 430, 60).estante(Producto.LIMPIEZA, 690, 60)
            .caja(310, 245, 90, 70).caja(600, 345, 100, 70)
            .producto("Manzana", Producto.FRUTAS, 104, 220).producto("Platano", Producto.FRUTAS, 430, 455)
            .producto("Leche", Producto.LACTEOS, 440, 270).producto("Yogur", Producto.LACTEOS, 140, 455)
            .producto("Jabon", Producto.LIMPIEZA, 780, 235).producto("Detergente", Producto.LIMPIEZA, 815, 405)
            .construir();

        ConfiguracionNivel dos = ConfiguracionNivel.crear(2, "Pasillo concurrido")
            .tiempo(135).jugador(48, 42)
            .estante(Producto.FRUTAS, 80, 60).estante(Producto.LACTEOS, 690, 230)
            .estante(Producto.LIMPIEZA, 100, 420)
            .caja(390, 100, 80, 160).caja(390, 340, 80, 140).caja(670, 70, 110, 70)
            .cliente(170, 300, 0)
            .cliente(() -> new Cliente(540, 380, 2, new PatrullaConPausas(2, 1)))
            .cliente(580, 470, 3).cliente(320, 170, 1)
            .producto("Manzana", Producto.FRUTAS, 100, 220).producto("Platano", Producto.FRUTAS, 820, 70)
            .producto("Leche", Producto.LACTEOS, 790, 410).producto("Yogur", Producto.LACTEOS, 250, 330)
            .producto("Jabon", Producto.LIMPIEZA, 550, 110).producto("Detergente", Producto.LIMPIEZA, 550, 450)
            .construir();

        ConfiguracionNivel tres = ConfiguracionNivel.crear(3, "Rutas cruzadas")
            .tiempo(165).jugador(48, 42)
            .estante(Producto.FRUTAS, 150, 60).estante(Producto.LACTEOS, 420, 60).estante(Producto.LIMPIEZA, 680, 60)
            .caja(250, 220, 70, 70).caja(550, 220, 70, 70).caja(400, 380, 90, 60)
            .cliente(200, 280, 0).cliente(580, 150, 1)
            .cliente(() -> new Cliente(420, 320, 2, new PatrullaConPausas(1.5, 1.5)))
            .producto("Manzana", Producto.FRUTAS, 100, 200).producto("Platano", Producto.FRUTAS, 700, 400)
            .producto("Leche", Producto.LACTEOS, 320, 150).producto("Yogur", Producto.LACTEOS, 750, 180)
            .producto("Jabon", Producto.LIMPIEZA, 150, 380).producto("Detergente", Producto.LIMPIEZA, 450, 450)
            .construir();

        ConfiguracionNivel cuatro = ConfiguracionNivel.crear(4, "Reposicion por secciones")
            .tiempo(180).jugador(48, 42)
            .entregasEnOrden(true) // Regla de la rúbrica aplicada exitosamente
            .estante(Producto.FRUTAS, 150, 60).estante(Producto.LACTEOS, 420, 60).estante(Producto.LIMPIEZA, 680, 60)
            .caja(300, 200, 80, 70).caja(580, 200, 80, 70)
            .cliente(320, 380, 0).cliente(650, 150, 2) // Cliente movido a Y=150 para evitar la caja
            .producto("Manzana", Producto.FRUTAS, 120, 220).producto("Platano", Producto.FRUTAS, 220, 420)
            .producto("Leche", Producto.LACTEOS, 480, 220).producto("Yogur", Producto.LACTEOS, 380, 420)
            .producto("Jabon", Producto.LIMPIEZA, 720, 220).producto("Detergente", Producto.LIMPIEZA, 680, 420)
            .construir();

        return new CatalogoNiveles(List.of(uno, dos, tres, cuatro));
    }
}