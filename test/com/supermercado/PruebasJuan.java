package com.supermercado;

import com.supermercado.entities.*;
import com.supermercado.entities.movimiento.*;
import com.supermercado.game.Juego;
import com.supermercado.levels.*;
import com.supermercado.products.Producto;
import com.supermercado.obstacles.*;
import com.supermercado.ui.GamePanel;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

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
        entregas();
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

    private static Juego juegoObjetivo(boolean ordenado) {
        ConfiguracionNivel c = ConfiguracionNivel.crear(1, "Secuencia fruta lacteo fruta")
                .entregasEnOrden(ordenado)
                .estante(Producto.FRUTAS, 170, 60).estante(Producto.LACTEOS, 430, 60)
                .producto("Manzana", Producto.FRUTAS, 100, 220)
                .producto("Leche", Producto.LACTEOS, 440, 270)
                .producto("Platano", Producto.FRUTAS, 400, 400).construir();
        Juego j = new Juego(new CatalogoNiveles(List.of(c)));
        j.iniciarPartida();
        return j;
    }

    private static void recoger(Juego j, Producto producto) {
        j.getNivel().getJugador().setPosicion(producto.getX(), producto.getY());
        j.interactuar();
        verificar(j.getNivel().getJugador().getProductoEnMano() == producto, "Recogida mediante interaccion real");
    }

    private static void acercarEstante(Juego j, String categoria) {
        for (Obstaculo o : j.getNivel().getObstaculos()) {
            if (o instanceof Estante && ((Estante) o).getCategoria().equals(categoria)) {
                j.getNivel().getJugador().setPosicion(o.getX() + 15, o.getY() + o.getAlto() + 2);
                return;
            }
        }
        throw new AssertionError("No hay estante destino");
    }

    private static void entregar(Juego j, Producto producto) {
        recoger(j, producto);
        acercarEstante(j, producto.getCategoria());
        j.interactuar();
        verificar(producto.isEntregado(), "Entrega aceptada por regla y estante");
    }

    private static void entregas() throws Exception {
        Juego j = juegoObjetivo(true);
        Nivel nivel = j.getNivel();
        Producto fruta = nivel.getProductos().get(0), leche = nivel.getProductos().get(1);
        Producto otraFruta = nivel.getProductos().get(2);
        verificar(nivel.getSiguienteProducto() == fruta, "Objetivo inicial consultable desde interfaz");
        verificar(!nivel.permiteEntrega(null)
                && !nivel.permiteEntrega(new Producto("Ajeno", Producto.FRUTAS, 100, 220)), "Rechaza productos ajenos");
        recoger(j, leche);
        acercarEstante(j, Producto.LACTEOS);
        j.interactuar();
        verificar(!leche.isEntregado() && j.getNivel().getJugador().getProductoEnMano() == leche,
                "Entrega fuera de orden conserva el producto");
        verificar(j.getPuntuacion() == 0 && nivel.getEntregados() == 0, "Entrega rechazada no suma ni avanza");
        verificar(j.getMensaje().contains("Fuera de orden"), "Explica el rechazo al jugador");
        j.pausar(); j.soltarProducto();
        verificar(nivel.getJugador().getProductoEnMano() == leche, "Soltar no modifica partida pausada");
        j.alternarPausa();

        // Verifica Q a traves del mismo mapa de acciones que usa la ventana.
        SwingUtilities.invokeAndWait(() -> {
            GamePanel panel = new GamePanel(j, false);
            Object accion = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .get(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, false));
            verificar(accion != null, "Q registrada");
            panel.getActionMap().get(accion).actionPerformed(new ActionEvent(panel, 0, "test"));
            panel.avanzarFotograma(0);
        });
        verificar(nivel.getJugador().getProductoEnMano() == null && leche.estaDisponible(), "Q recupera mano y producto disponible");
        cerca(leche.getX(), nivel.getJugador().getX(), "Producto queda donde esta el jugador");
        verificar(nivel.posicionLibre(leche.getX(), leche.getY()), "Producto soltado no queda dentro de caja");
        verificar(!leche.soltar(0, 0), "Soltar otra vez no teletransporta un producto del suelo");

        entregar(j, otraFruta);
        verificar(j.getPuntuacion() == 100 && nivel.getSiguienteProducto() == leche,
                "Cualquier fruta satisface la categoria; avanza a lacteos");
        entregar(j, leche);
        verificar(nivel.getSiguienteProducto() == fruta, "Categoria repetida sigue pendiente");
        entregar(j, fruta);
        verificar(j.getEstado() == Juego.Estado.VICTORIA && j.getPuntuacion() == 300, "Victoria completa con secuencia de categorias");
        verificar(nivel.getSiguienteProducto() == null && !nivel.permiteEntrega(fruta), "No hay objetivo despues de completar");
        j.interactuar(); j.soltarProducto();
        verificar(j.getPuntuacion() == 300 && !fruta.soltar(20, 20), "Victoria y producto entregado no se duplican");

        Juego libre = juegoObjetivo(false);
        verificar(libre.getNivel().getSiguienteProducto() == null, "Modo libre no impone siguiente categoria");
        entregar(libre, libre.getNivel().getProductos().get(1));
        verificar(libre.getPuntuacion() == 100, "Modo libre permite empezar por lacteos");

        Juego reinicio = juegoObjetivo(true);
        entregar(reinicio, reinicio.getNivel().getProductos().get(2));
        recoger(reinicio, reinicio.getNivel().getProductos().get(1));
        reinicio.soltarProducto(); reinicio.terminarPartida();
        verificar(reinicio.reintentarNivel() && reinicio.getPuntuacion() == 0, "Reintento descarta puntos provisionales");
        verificar(reinicio.getNivel().getSiguienteProducto().getCategoria().equals(Producto.FRUTAS), "Reintento vuelve a la primera categoria");
        cerca(reinicio.getNivel().getProductos().get(1).getY(), 270, "Recarga restaura ubicacion original de producto soltado");
    }
}
