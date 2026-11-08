package com.supermercado;

import com.supermercado.entities.*;
import com.supermercado.entities.movimiento.*;
import com.supermercado.game.Juego;
import com.supermercado.levels.*;
import com.supermercado.products.Producto;

/** Regresiones funcionales de las tareas U1, U2 y U3. Sin dependencias externas. */
public final class PruebasJuan {
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        comprobaciones++;
    }

    private static void cerca(double actual, double esperado, String mensaje) {
        verificar(Math.abs(actual - esperado) < 1e-7, mensaje);
    }

    public static void ejecutar() throws Exception {
        estrategias();
        System.out.println("OK Juan: " + comprobaciones + " comprobaciones.");
    }

    private static void estrategias() {
        double[][] desplazamientos = {{88.0/60, 0}, {0, 88.0/60}, {-88.0/60, 0}, {0, -88.0/60}};
        for (int direccion = 0; direccion < 4; direccion++) {
            Cliente c = new Cliente(100, 200, direccion);
            c.update();
            cerca(c.getDesplazamientoX(), desplazamientos[direccion][0], "Conserva movimiento X original");
            cerca(c.getDesplazamientoY(), desplazamientos[direccion][1], "Conserva movimiento Y original");
            cerca(c.getX(), 100, "Strategy no cambia directamente la posicion");
        }
        Cliente normalizado = new Cliente(100, 200, -1);
        verificar(normalizado.getDireccion() == 3, "Conserva normalizacion de direccion");

        MovimientoCliente estrategiaCompartida = new PatrullaConPausas();
        Cliente a = new Cliente(100, 200, 0, estrategiaCompartida);
        Cliente b = new Cliente(400, 200, 1, estrategiaCompartida);
        for (int i = 0; i < 120; i++) {
            a.update();
            verificar(a.getDesplazamientoX() > 0, "Marcha durante dos segundos activos");
        }
        for (int i = 0; i < 60; i++) {
            a.update();
            cerca(a.getDesplazamientoX(), 0, "Descansa durante un segundo activo");
        }
        b.update();
        verificar(b.getDesplazamientoY() > 0, "Dos clientes comparten estrategia sin compartir reloj");
        a.update();
        verificar(a.getDesplazamientoX() > 0, "Reanuda marcha al finalizar pausa");

        ConfiguracionNivel configuracion = ConfiguracionNivel.crear(1, "Prueba de colisiones Strategy")
                .estante(Producto.FRUTAS, 170, 60).producto("A", Producto.FRUTAS, 100, 220)
                .caja(860, 400, 30, 60)
                .cliente(() -> new Cliente(900, 400, 0, new PatrullaConPausas())).construir();
        Nivel nivel = new Nivel(configuracion);
        Cliente c = (Cliente) nivel.getPersonajes().get(1);
        boolean invirtio = false;
        for (int i = 0; i < 300; i++) {
            nivel.actualizarPersonajes();
            verificar(nivel.posicionLibre(c.getX(), c.getY()), "Strategy respeta limites y colisiones de Nivel");
            if (c.getDireccion() == 2) invirtio = true;
        }
        verificar(invirtio, "Cliente invierte direccion al bloquearse");
        nivel.cargar();
        Cliente nuevo = (Cliente) nivel.getPersonajes().get(1);
        verificar(nuevo != c, "Recarga construye otro cliente");
        nuevo.update();
        verificar(nuevo.getDesplazamientoX() > 0, "Recarga reinicia ciclo de marcha y pausa");

        Juego juego = new Juego(new CatalogoNiveles(java.util.List.of(configuracion)));
        juego.iniciarPartida();
        for (int i = 0; i < 120; i++) juego.actualizar();
        Cliente pausado = (Cliente) juego.getNivel().getPersonajes().get(1);
        juego.pausar();
        double xPausa = pausado.getX();
        for (int i = 0; i < 240; i++) juego.actualizar();
        cerca(pausado.getX(), xPausa, "Pausa general congela cliente");
        juego.alternarPausa();
        juego.actualizar();
        cerca(pausado.getDesplazamientoX(), 0, "Pausa general no consume descanso del cliente");

        boolean rechazo = false;
        try { new PatrullaConPausas(0, 1); } catch (IllegalArgumentException e) { rechazo = true; }
        verificar(rechazo, "Rechaza duracion de marcha invalida");
    }
}
