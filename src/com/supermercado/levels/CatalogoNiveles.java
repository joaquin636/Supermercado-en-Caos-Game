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
            .entregasEnOrden(true) // Alterna frutas, lacteos y limpieza dos veces
            .estante(Producto.FRUTAS, 150, 60).estante(Producto.LACTEOS, 420, 60).estante(Producto.LIMPIEZA, 680, 60)
            .caja(300, 200, 80, 70).caja(580, 200, 80, 70)
            .cliente(320, 380, 0).cliente(650, 150, 2) // Cliente movido a Y=150 para evitar la caja
            .producto("Manzana", Producto.FRUTAS, 120, 220).producto("Leche", Producto.LACTEOS, 480, 220)
            .producto("Jabon", Producto.LIMPIEZA, 720, 220).producto("Platano", Producto.FRUTAS, 220, 420)
            .producto("Yogur", Producto.LACTEOS, 380, 420).producto("Detergente", Producto.LIMPIEZA, 680, 420)
            .construir();

        // Valores iniciales de balance. Las rutas y los ciclos se comprueban caminando en las pruebas.
        ConfiguracionNivel cinco = ConfiguracionNivel.crear(5, "Cruce de reposicion")
            .tiempo(155).jugador(48, 42)
            .estante(Producto.FRUTAS, 80, 60).estante(Producto.LACTEOS, 690, 230)
            .estante(Producto.LIMPIEZA, 90, 420)
            .caja(420, 100, 80, 220).caja(420, 400, 80, 60).caja(150, 250, 100, 70)
            .cliente(310, 170, 1).cliente(610, 470, 3).cliente(740, 370, 2)
            .cliente(() -> new Cliente(260, 365, 0, new PatrullaConPausas(1.4, .8)))
            .cliente(() -> new Cliente(550, 170, 1, new PatrullaConPausas(2.2, .6)))
            .producto("Manzana", Producto.FRUTAS, 780, 80).producto("Platano", Producto.FRUTAS, 580, 450)
            .producto("Leche", Producto.LACTEOS, 120, 200).producto("Yogur", Producto.LACTEOS, 300, 460)
            .producto("Jabon", Producto.LIMPIEZA, 590, 110).producto("Detergente", Producto.LIMPIEZA, 780, 410)
            .construir();

        ConfiguracionNivel seis = ConfiguracionNivel.crear(6, "Ultimo turno")
            .tiempo(100).jugador(48, 42).entregasEnOrden(true)
            .estante(Producto.FRUTAS, 90, 70).estante(Producto.LACTEOS, 650, 70)
            .estante(Producto.LIMPIEZA, 365, 430)
            .caja(350, 160, 80, 200).caja(550, 260, 80, 160)
            .caja(130, 320, 110, 70).caja(730, 330, 100, 70)
            .cliente(310, 210, 1).cliente(590, 460, 3).cliente(720, 180, 2).cliente(70, 370, 3)
            .cliente(() -> new Cliente(480, 200, 1, new PatrullaConPausas(1.2, .7)))
            .cliente(() -> new Cliente(850, 460, 2, new PatrullaConPausas(1.8, .6)))
            .producto("Leche", Producto.LACTEOS, 150, 250).producto("Manzana", Producto.FRUTAS, 790, 430)
            .producto("Jabon", Producto.LIMPIEZA, 700, 230).producto("Yogur", Producto.LACTEOS, 460, 350)
            .producto("Platano", Producto.FRUTAS, 280, 440).producto("Detergente", Producto.LIMPIEZA, 450, 120)
            .construir();

        return new CatalogoNiveles(List.of(uno, dos, tres, cuatro, cinco, seis));
    }
}
