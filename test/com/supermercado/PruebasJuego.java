package com.supermercado;

import com.supermercado.entities.*;
import com.supermercado.game.Juego;
import com.supermercado.levels.Nivel;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import com.supermercado.ui.GamePanel;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Pruebas sin dependencias: se ejecutan incluso sin escritorio grafico. */
public final class PruebasJuego {
    private static int comprobaciones;
    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) throw new AssertionError(descripcion);
        comprobaciones++;
    }
    private static Juego nuevo() { Juego j = new Juego(); j.iniciarPartida(); return j; }
    private static void pulsar(GamePanel panel, int tecla, boolean soltar) {
        Object nombre = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke(tecla, 0, soltar));
        verificar(nombre != null, "Tecla registrada");
        panel.getActionMap().get(nombre).actionPerformed(new ActionEvent(panel, 0, "test"));
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        movimiento(); entregas(); estados(); recorridoCompleto();
        SwingUtilities.invokeAndWait(() -> {
            try { teclado(); renderizar(args.length == 0 ? "docs/capturas" : args[0]); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("OK: " + comprobaciones + " comprobaciones. Movimiento, colisiones, entrega, partida, teclado y render.");
    }
    private static void movimiento() {
        Nivel n = new Nivel(1); Jugador p = n.getJugador();
        p.setPosicion(40, 340); p.setDireccion(1, 0);
        for (int i = 0; i < 30; i++) n.actualizarPersonajes();
        double recto = p.getX() - 40;
        p.setPosicion(40, 340); p.setDireccion(1, 1);
        for (int i = 0; i < 30; i++) n.actualizarPersonajes();
        verificar(Math.abs(Math.hypot(p.getX() - 40, p.getY() - 340) - recto) < .0001, "Diagonal normalizada");
        p.setPosicion(270, 260); n.mover(p, 500, 0);
        verificar(p.getX() + Personaje.TAMANO <= 310, "No atraviesa caja con desplazamiento grande");
        p.setPosicion(180, 145); n.mover(p, 0, -100);
        verificar(p.getY() >= 136, "Estante bloquea paso");
        p.setPosicion(5, 5); n.mover(p, -100, -100);
        verificar(p.getX() >= 0 && p.getY() >= 0, "Limites superior e izquierdo");
        p.setPosicion(925, 505); n.mover(p, 100, 100);
        verificar(p.getX() <= 930 && p.getY() <= 510, "Limites inferior y derecho");
        for (int i = 0; i < 2500; i++) n.actualizarPersonajes();
        for (Personaje c : n.getPersonajes()) verificar(n.posicionLibre(c.getX(), c.getY()), "Personaje termina en posicion valida");
        Cliente c = new Cliente(50, 50, 0); c.update(); double dx = c.getDesplazamientoX(); c.cambiarDireccion(); c.update();
        verificar(dx > 0 && c.getDesplazamientoX() < 0, "Cliente invierte patrulla");
    }
    private static void entregas() {
        Juego j = nuevo(); Jugador p = j.getNivel().getJugador();
        j.interactuar(); verificar(p.getProductoEnMano() == null, "No recoge a distancia");
        Producto fruta = j.getNivel().getProductos().get(0);
        p.setPosicion(fruta.getX(), fruta.getY()); j.interactuar();
        verificar(p.getProductoEnMano() == fruta, "Recogida correcta");
        p.setPosicion(440, 270); j.interactuar(); verificar(p.getProductoEnMano() == fruta, "Solo un producto a la vez");
        p.setPosicion(445, 138); j.interactuar();
        verificar(!fruta.isEntregado() && j.getPuntuacion() == 0, "Categoria equivocada no suma");
        p.setPosicion(185, 138); j.interactuar();
        verificar(fruta.isEntregado() && p.getProductoEnMano() == null && j.getPuntuacion() == 100, "Entrega vacia mano y suma");
        j.interactuar(); verificar(j.getPuntuacion() == 100, "No repite puntaje");
        Estante e = (Estante) j.getNivel().getObstaculos().get(0);
        verificar(!e.recibirProducto(fruta), "Estante rechaza entrega duplicada");
    }
    private static void estados() {
        Juego j = nuevo();
        double tiempo = j.getTiempoRestante(); j.alternarPausa(); j.setDireccion(1, 0);
        double x = j.getNivel().getJugador().getX();
        for (int i = 0; i < 100; i++) j.actualizar();
        verificar(j.getTiempoRestante() == tiempo && j.getNivel().getJugador().getX() == x, "Pausa congela tiempo y personaje");
        j.alternarPausa(); j.setDireccion(0, 0); j.actualizar();
        verificar(j.getTiempoRestante() < tiempo, "Reanuda reloj");
        Personaje cliente = j.getNivel().getPersonajes().get(1);
        j.getNivel().getJugador().setPosicion(cliente.getX(), cliente.getY());
        double antes = j.getTiempoRestante(); j.actualizar();
        verificar(antes - j.getTiempoRestante() >= 5, "Choque descuenta tiempo");
        antes = j.getTiempoRestante(); j.actualizar();
        verificar(antes - j.getTiempoRestante() < 1, "Inmunidad evita penalizacion por cada frame");
        j = nuevo();
        for (int i = 0; i < 10810; i++) j.actualizar();
        verificar(j.getEstado() == Juego.Estado.DERROTA && j.getTiempoRestante() == 0, "Derrota al agotarse reloj");
        j.iniciarPartida(); verificar(j.getEstado() == Juego.Estado.JUGANDO && j.getPuntuacion() == 0 && j.getNivel().getEntregados() == 0, "Reinicio limpio");
    }

    /** Busca y recorre rutas reales por el mapa; no teletransporta entre los objetivos. */
    private static void recorridoCompleto() {
        Juego j = nuevo(); j.getNivel().getJugador().setPosicion(48, 40);
        for (Producto p : j.getNivel().getProductos()) {
            caminar(j, p.getBounds()); j.interactuar();
            verificar(j.getNivel().getJugador().getProductoEnMano() == p, "Ruta permite recoger " + p.getNombre());
            Estante destino = null;
            for (Obstaculo o : j.getNivel().getObstaculos()) if (o instanceof Estante && ((Estante) o).getCategoria().equals(p.getCategoria())) destino = (Estante) o;
            caminar(j, Objects.requireNonNull(destino).getBounds()); j.interactuar();
            verificar(p.isEntregado(), "Ruta permite entregar " + p.getNombre());
        }
        verificar(j.getEstado() == Juego.Estado.VICTORIA && j.getPuntuacion() == 600, "Partida completa ganable con 600 puntos");
        System.out.printf("Partida recorrida: 6/6, %d puntos, %.1f segundos restantes.%n", j.getPuntuacion(), j.getTiempoRestante());
    }
    private static void caminar(Juego j, Rectangle2D objetivo) {
        Nivel n = j.getNivel(); Jugador p = n.getJugador();
        // Rejilla desde la posicion real: las aristas se comprueban a intervalos de 2 px.
        double ox = p.getX(), oy = p.getY();
        int lado = 241, centro = 120, inicio = centro * lado + centro;
        int[] anterior = new int[lado * lado]; Arrays.fill(anterior, -1); anterior[inicio] = inicio;
        ArrayDeque<Integer> cola = new ArrayDeque<>(); cola.add(inicio); int fin = -1;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!cola.isEmpty()) {
            int v = cola.remove(); int gx = v % lado - centro, gy = v / lado - centro;
            double x = ox + gx * 8, y = oy + gy * 8;
            if (Obstaculo.distancia(new Rectangle2D.Double(x,y,30,30),objetivo) <= 22) {fin=v;break;}
            for (int[] d : dirs) {
                int nx = gx + d[0] + centro, ny = gy + d[1] + centro;
                if (nx<0||ny<0||nx>=lado||ny>=lado) continue;
                int siguiente = ny*lado+nx;
                if(anterior[siguiente]!=-1)continue;
                boolean libre=true;
                for(int s=1;s<=4;s++) if(!n.posicionLibre(x+d[0]*2*s,y+d[1]*2*s))libre=false;
                if(libre){anterior[siguiente]=v;cola.add(siguiente);}
            }
        }
        verificar(fin!=-1,"Existe ruta hasta objetivo");
        java.util.List<Integer> ruta=new ArrayList<>();
        for(int v=fin;v!=inicio;v=anterior[v])ruta.add(v);
        Collections.reverse(ruta);
        // Cada segmento se recorre con el movimiento y colisiones reales del modelo.
        for(int v:ruta){
            double tx=ox+(v%lado-centro)*8, ty=oy+(v/lado-centro)*8;
            // Mover usa el mismo resolvedor que actualizarPersonajes, con pasos de <=4px.
            n.mover(p,tx-p.getX(),ty-p.getY());
            j.setDireccion(0,0);
            for(int k=0;k<3;k++)j.actualizar();
            verificar(Math.abs(p.getX()-tx)<.01 && Math.abs(p.getY()-ty)<.01,"Recorrido sin bloqueo");
            verificar(j.getEstado()==Juego.Estado.JUGANDO,"Partida sigue activa durante recorrido");
        }
    }
    private static void teclado() {
        Juego j = new Juego(); GamePanel panel = new GamePanel(j, false);
        pulsar(panel, KeyEvent.VK_ENTER, false); panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getEstado() == Juego.Estado.JUGANDO, "Enter inicia");
        double x = j.getNivel().getJugador().getX();
        pulsar(panel, KeyEvent.VK_D, false); panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getNivel().getJugador().getX() > x, "D mueve mediante Key Bindings");
        pulsar(panel, KeyEvent.VK_RIGHT, false); pulsar(panel, KeyEvent.VK_D, true);
        x = j.getNivel().getJugador().getX(); panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getNivel().getJugador().getX() > x, "Flecha sigue activa al soltar D");
        pulsar(panel, KeyEvent.VK_RIGHT, true); x = j.getNivel().getJugador().getX(); panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getNivel().getJugador().getX() == x, "Soltar detiene");
        j.getNivel().getJugador().setPosicion(104,220);
        pulsar(panel,KeyEvent.VK_E,false);panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getNivel().getJugador().getProductoEnMano()!=null,"E recoge");
        j.getNivel().getJugador().setPosicion(185,138);
        pulsar(panel,KeyEvent.VK_E,false);panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getPuntuacion()==0,"Mantener E no repite accion");
        pulsar(panel,KeyEvent.VK_E,true);pulsar(panel,KeyEvent.VK_E,false);panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getPuntuacion()==100,"Nueva pulsacion E entrega");
        panel.perderFoco(); x=j.getNivel().getJugador().getX();panel.avanzarFotograma(.1);
        verificar(j.getEstado()==Juego.Estado.PAUSA && j.getNivel().getJugador().getX()==x,"Perder foco pausa y limpia entrada");
        pulsar(panel,KeyEvent.VK_P,false);panel.avanzarFotograma(Personaje.PASO);
        verificar(j.getEstado()==Juego.Estado.JUGANDO,"P reanuda");
    }
    private static void renderizar(String carpeta) throws Exception {
        File dir=new File(carpeta); if (!dir.isDirectory() && !dir.mkdirs()) throw new IllegalStateException("No se pudo crear carpeta de capturas");
        Juego j=new Juego();GamePanel panel=new GamePanel(j,false);panel.setSize(1280,800);
        guardar(panel,new File(dir,"01_menu.png"));
        j.iniciarPartida();guardar(panel,new File(dir,"02_partida.png"));
        j.getNivel().getJugador().setPosicion(104,220);j.interactuar();guardar(panel,new File(dir,"03_producto.png"));
        j.pausar();guardar(panel,new File(dir,"04_pausa.png"));
    }
    private static void guardar(GamePanel panel,File destino)throws Exception{
        BufferedImage imagen=new BufferedImage(1280,800,BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g=imagen.createGraphics();panel.paint(g);g.dispose();ImageIO.write(imagen,"png",destino);
    }
}
