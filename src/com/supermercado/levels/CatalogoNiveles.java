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
        // U3: tiempo inicial de balance. Mismo objetivo, rutas distintas y dos estrategias reales.
        ConfiguracionNivel dos = ConfiguracionNivel.crear(2, "Pasillo concurrido")
            .tiempo(165).jugador(48, 42)
            .estante(Producto.FRUTAS, 90, 60).estante(Producto.LACTEOS, 390, 60)
            .estante(Producto.LIMPIEZA, 690, 60)
            .caja(280, 240, 90, 100).caja(580, 330, 100, 70)
            .cliente(240, 190, 0)
            .cliente(() -> new Cliente(480, 300, 2, new PatrullaConPausas(2, 1)))
            .cliente(830, 450, 2)
            .producto("Manzana", Producto.FRUTAS, 100, 230).producto("Platano", Producto.FRUTAS, 760, 440)
            .producto("Leche", Producto.LACTEOS, 450, 250).producto("Yogur", Producto.LACTEOS, 140, 450)
            .producto("Jabon", Producto.LIMPIEZA, 800, 230).producto("Detergente", Producto.LIMPIEZA, 470, 430)
            .construir();
        return new CatalogoNiveles(List.of(uno, dos));
    }
}
