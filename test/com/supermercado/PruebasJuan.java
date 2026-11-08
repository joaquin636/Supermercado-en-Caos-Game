package com.supermercado;

import com.supermercado.entities.*;
import com.supermercado.entities.movimiento.*;
import com.supermercado.game.Juego;
import com.supermercado.levels.*;
import com.supermercado.products.Producto;
import com.supermercado.obstacles.*;
import com.supermercado.ui.GamePanel;
import com.supermercado.ui.GameWindow;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Regresiones funcionales de las tareas U1, U2 y U3. Sin dependencias externas. */
public final class PruebasJuan {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--demo-orden")) {
            // Configuracion de demostracion, no agrega un nivel a la campana.
            SwingUtilities.invokeLater(() -> new GameWindow(juegoObjetivo(true)));
        } else if (args.length == 1 && args[0].equals("--demo-nivel2")) {
            // Acceso de verificacion: prepara la victoria del nivel 1 sin cambiar la campana.
            SwingUtilities.invokeLater(() -> new GameWindow(entrarNivelDos()));
        } else if (args.length == 0) {
            System.setProperty("java.awt.headless", "true");
            ejecutar();
        } else {
            throw new IllegalArgumentException("Uso: PruebasJuan [--demo-orden | --demo-nivel2]");
        }
    }

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
        nivelDos();
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
        verificar(!a.estaDescansando(), "No anuncia descanso antes de primera actualizacion");
        for (int i = 0; i < 120; i++) {
            a.update();
            verificar(a.getDesplazamientoX() > 0, "Marcha durante dos segundos activos");
        }
        for (int i = 0; i < 60; i++) {
            a.update();
            cerca(a.getDesplazamientoX(), 0, "Descansa durante un segundo activo");
        }
        b.update();
        verificar(a.estaDescansando(), "HUD puede consultar descanso sin conocer estrategia concreta");
        verificar(b.getDesplazamientoY() > 0, "Dos clientes comparten estrategia sin compartir reloj");
        a.update();
        verificar(a.getDesplazamientoX() > 0, "Reanuda marcha al finalizar pausa");
        verificar(!a.estaDescansando(), "Indicador desaparece al reanudar marcha");

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

    private static Juego entrarNivelDos() {
        Juego j = new Juego(); j.iniciarPartida();
        // Solo prepara el acceso por victoria normal; el recorrido del nivel 2 no teletransporta.
        for (Producto p : j.getNivel().getProductos()) entregar(j, p);
        verificar(j.avanzarNivel() && j.getNivelActual() == 2, "Nivel 2 accesible tras victoria del primero");
        return j;
    }

    private static void nivelDos() throws Exception {
        Juego j = entrarNivelDos();
        Nivel nivel = j.getNivel();
        verificar(nivel.getConfiguracion().getIdentificacion().equals("Pasillo concurrido"), "Se sustituye la demo por nivel 2");
        verificar(nivel.getObjetivoEntregas() == 6 && !nivel.getConfiguracion().isEntregasEnOrden(), "Seis entregas libres en nivel 2");
        int continuos = 0, conPausas = 0;
        for (Personaje personaje : nivel.getPersonajes()) {
            if (personaje instanceof Cliente cliente) {
                if (cliente.getMovimiento() instanceof PatrullaConPausas) conPausas++;
                if (cliente.getMovimiento() instanceof PatrullaRebote) continuos++;
            }
        }
        verificar(continuos == 3 && conPausas == 1, "Nivel 2 configura cuatro clientes y las dos estrategias");
        Cliente sube = (Cliente) nivel.getPersonajes().get(3);
        Cliente baja = (Cliente) nivel.getPersonajes().get(4);
        verificar(sube.getDireccion() == 3 && baja.getDireccion() == 1, "Hay patrullas verticales en ambos sentidos");
        capturar(j, "juan-nivel2-inicio.png");
        capturar(j, "juan-nivel2-800.png", 800, 500);
        double ySube = sube.getY(), yBaja = baja.getY();
        for (int i = 0; i < 121; i++) j.actualizar();
        verificar(((Cliente) nivel.getPersonajes().get(2)).estaDescansando(), "Cliente con pausas descansa en partida real");
        verificar(sube.getY() < ySube && baja.getY() > yBaja, "Clientes avanzan realmente arriba y abajo");
        capturar(j, "juan-nivel2-cliente-descansando.png");
        for (Producto p : nivel.getProductos()) {
            caminar(j, p.getBounds()); j.interactuar();
            verificar(nivel.getJugador().getProductoEnMano() == p, "Recorrido permite recoger " + p.getNombre());
            Estante destino = null;
            for (Obstaculo o : nivel.getObstaculos()) {
                if (o instanceof Estante e && e.getCategoria().equals(p.getCategoria())) destino = e;
            }
            caminar(j, Objects.requireNonNull(destino).getBounds()); j.interactuar();
            verificar(p.isEntregado(), "Recorrido permite entregar " + p.getNombre());
        }
        verificar(j.getEstado() == Juego.Estado.VICTORIA && j.getPuntuacion() == 1200,
                "Gana nivel 2 con 600 puntos propios y conserva 600 del primero");
        verificar(!j.isCampanaFinalizada() && !j.avanzarNivel(), "Nivel 3 sigue pendiente; no inventa final de campana");
        System.out.printf("Nivel 2 recorrido a velocidad real: 6/6, %.1f segundos restantes.%n", j.getTiempoRestante());
        capturar(j, "juan-nivel2-victoria.png");

        // La demo de orden se puede inspeccionar sin editar el catalogo del producto.
        Juego demo = juegoObjetivo(true);
        recoger(demo, demo.getNivel().getProductos().get(1));
        acercarEstante(demo, Producto.LACTEOS); demo.interactuar();
        capturar(demo, "juan-orden-rechazado.png");

        Juego derrota = entrarNivelDos();
        recoger(derrota, derrota.getNivel().getProductos().get(0));
        acercarEstante(derrota, Producto.FRUTAS); derrota.interactuar();
        for (int i = 0; i < 10000 && derrota.getEstado() == Juego.Estado.JUGANDO; i++) derrota.actualizar();
        verificar(derrota.getEstado() == Juego.Estado.DERROTA, "Nivel 2 se puede perder por reloj");
        verificar(derrota.reintentarNivel() && derrota.getPuntuacion() == 600, "Reintenta nivel 2 conservando solo puntos previos");
        verificar(derrota.getNivel().getEntregados() == 0 && derrota.getTiempoRestante() == 135, "Reintento restaura objetos y tiempo de nivel 2");
        Cliente cliente = (Cliente) derrota.getNivel().getPersonajes().get(2);
        cliente.update();
        verificar(cliente.getDesplazamientoX() < 0, "Reintento restaura fase inicial de cliente con pausas");
    }

    /** BFS solo planea rutas estaticas. Cada arista se ejecuta mediante direccion y ticks del Juego. */
    private static void caminar(Juego juego, Rectangle2D objetivo) {
        Nivel nivel = juego.getNivel(); Jugador jugador = nivel.getJugador();
        final int celda = 7;
        double origenX = jugador.getX() % celda, origenY = jugador.getY() % celda;
        int columnas = (int) ((Nivel.ANCHO - Personaje.TAMANO - origenX) / celda) + 1;
        int filas = (int) ((Nivel.ALTO - Personaje.TAMANO - origenY) / celda) + 1;
        int inicio = (int) Math.round((jugador.getY() - origenY) / celda) * columnas
                + (int) Math.round((jugador.getX() - origenX) / celda);
        int[] anteriores = new int[columnas * filas]; Arrays.fill(anteriores, -1);
        anteriores[inicio] = inicio;
        ArrayDeque<Integer> cola = new ArrayDeque<>(); cola.add(inicio);
        int[][] direcciones = {{1,0},{-1,0},{0,1},{0,-1}};
        int fin = -1;
        while (!cola.isEmpty()) {
            int actual = cola.remove(); int gx = actual % columnas, gy = actual / columnas;
            double x = origenX + gx * celda, y = origenY + gy * celda;
            if (Obstaculo.distancia(new Rectangle2D.Double(x, y, 30, 30), objetivo) <= 20) { fin = actual; break; }
            for (int[] direccion : direcciones) {
                int nx = gx + direccion[0], ny = gy + direccion[1];
                if (nx < 0 || ny < 0 || nx >= columnas || ny >= filas) continue;
                int siguiente = ny * columnas + nx;
                if (anteriores[siguiente] != -1) continue;
                boolean libre = true;
                for (int paso = 1; paso <= 2; paso++) {
                    if (!nivel.posicionLibre(x + direccion[0] * 3.5 * paso, y + direccion[1] * 3.5 * paso)) libre = false;
                }
                if (libre) { anteriores[siguiente] = actual; cola.add(siguiente); }
            }
        }
        verificar(fin != -1, "Existe ruta accesible a objetivo del nivel 2");
        List<Integer> ruta = new ArrayList<>();
        for (int v = fin; v != inicio; v = anteriores[v]) ruta.add(v);
        Collections.reverse(ruta);
        for (int v : ruta) {
            double x = origenX + (v % columnas) * celda, y = origenY + (v / columnas) * celda;
            juego.setDireccion((int) Math.signum(x - jugador.getX()), (int) Math.signum(y - jugador.getY()));
            juego.actualizar(); juego.actualizar(); // 210/60 = 3.5 unidades por tick: dos ticks por arista.
            verificar(juego.getEstado() == Juego.Estado.JUGANDO, "Reloj permite llegar a cada objetivo");
            cerca(jugador.getX(), x, "Jugador avanza horizontalmente sin teletransporte");
            cerca(jugador.getY(), y, "Jugador avanza verticalmente sin teletransporte");
        }
        juego.setDireccion(0, 0);
    }

    private static void capturar(Juego juego, String nombre) throws Exception {
        capturar(juego, nombre, 1280, 800);
    }

    private static void capturar(Juego juego, String nombre, int ancho, int alto) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                GamePanel panel = new GamePanel(juego, false); panel.setSize(ancho, alto);
                BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = imagen.createGraphics(); panel.paint(g); g.dispose();
                Path destino = Path.of("build/capturas-pruebas", nombre);
                Files.createDirectories(destino.getParent());
                verificar(ImageIO.write(imagen, "png", destino.toFile()), "Captura Swing generada");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
