package com.supermercado;

import com.supermercado.entities.*;
import com.supermercado.game.Juego;
import com.supermercado.levels.*;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import com.supermercado.ui.GamePanel;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Fixtures sinteticas prueban la campana de seis etapas; no son niveles del producto. */
public final class PruebasCampana {
    private static int comprobaciones;
    private static void verificar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje); comprobaciones++;
    }
    private static void cerca(double a, double b, String mensaje) { verificar(Math.abs(a-b)<1e-7, mensaje); }
    private static void rechaza(Runnable accion, String mensaje) {
        boolean rechazo = false;
        try { accion.run(); } catch (IllegalArgumentException e) { rechazo = true; }
        verificar(rechazo, mensaje);
    }
    private static ConfiguracionNivel fixture(int numero) {
        return ConfiguracionNivel.crear(numero, "FIXTURE de prueba " + numero).tiempo(10)
            .estante(Producto.FRUTAS,170,60)
            .producto("A",Producto.FRUTAS,104,220).producto("B",Producto.FRUTAS,430,455).construir();
    }
    private static void entregar(Juego juego, int indice) {
        Producto p = juego.getNivel().getProductos().get(indice);
        Jugador jugador = juego.getNivel().getJugador();
        jugador.setPosicion(p.getX(),p.getY()); juego.interactuar();
        verificar(jugador.getProductoEnMano()==p,"Recoge producto de prueba");
        for (Obstaculo o : juego.getNivel().getObstaculos())
            if (o instanceof Estante && ((Estante)o).getCategoria().equals(p.getCategoria())) {
                jugador.setPosicion(o.getX()+15,o.getY()+o.getAlto()+2); juego.interactuar(); break;
            }
        verificar(p.isEntregado(),"Entrega producto de prueba");
    }
    private static void completar(Juego j) {
        for (int i=0;i<j.getNivel().getObjetivoEntregas();i++) entregar(j,i);
        verificar(j.getEstado()==Juego.Estado.VICTORIA,"Victoria real mediante entregas");
    }
    private static void agotar(Juego j) {
        for (int i=0;i<12000 && j.getEstado()==Juego.Estado.JUGANDO;i++) j.actualizar();
        verificar(j.getEstado()==Juego.Estado.DERROTA && j.getTiempoRestante()==0,"Derrota por tiempo");
    }
    public static void ejecutar() throws Exception {
        reloj(); configuraciones(); campana();
        SwingUtilities.invokeAndWait(() -> {
            try { interfaz(); } catch(Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("OK J1/J2/J3: " + comprobaciones + " comprobaciones adicionales de infraestructura y fixtures de prueba.");
    }
    private static void reloj() {
        Juego j = PartidasPrueba.crear(); verificar(j.getEstado()==Juego.Estado.MENU,"Inicio en menu");
        j.iniciarPartida(); cerca(j.getTiempoRestante(),180,"Nivel 1 empieza en 180 segundos");
        for(int i=0;i<60;i++) j.actualizar();
        cerca(j.getTiempoRestante(),179,"60 pasos descuentan un segundo sin choques");
        j.pausar(); double tiempo=j.getTiempoRestante();
        for(int i=0;i<60;i++)j.actualizar(); cerca(j.getTiempoRestante(),tiempo,"Pausa congela reloj");
        j.alternarPausa(); agotar(j);
        for(int i=0;i<60;i++)j.actualizar(); cerca(j.getTiempoRestante(),0,"Derrota detiene reloj");
        j.iniciarPartida(); completar(j); tiempo=j.getTiempoRestante();
        for(int i=0;i<60;i++)j.actualizar(); cerca(j.getTiempoRestante(),tiempo,"Victoria detiene reloj");
        j.terminarPartida(); verificar(j.getEstado()==Juego.Estado.VICTORIA,"Derrota no sobreescribe victoria");
    }
    private static void configuraciones() {
        CatalogoNiveles c=CatalogoNiveles.predeterminado();
        Nivel uno=new Nivel(c.obtener(1)), otro=new Nivel(c.obtener(1)), dos=new Nivel(c.obtener(2));
        verificar(uno.getProductos().size()==6 && uno.getPersonajes().size()==4 && uno.getObstaculos().size()==5,"Conserva mapa original");
        verificar(dos.getProductos().size()==6 && dos.getTiempoLimite()==135,"Nivel 2 con seis entregas y tiempo propio");
        verificar(uno.getJugador()!=otro.getJugador() && uno.getProductos().get(0)!=otro.getProductos().get(0)
            && uno.getPersonajes().get(1)!=otro.getPersonajes().get(1) && uno.getObstaculos().get(0)!=otro.getObstaculos().get(0),"Entidades independientes entre cargas");
        Producto anterior=uno.getProductos().get(0); anterior.marcarRecogido(); uno.cargar();
        verificar(uno.getProductos().get(0).estaDisponible() && otro.getProductos().get(0).estaDisponible(),"Recarga no contamina receta ni otras partidas");
        ConfiguracionNivel.Builder b=ConfiguracionNivel.crear(1,"Copia defensiva").producto("A",Producto.FRUTAS,100,200).estante(Producto.FRUTAS,170,60);
        ConfiguracionNivel receta=b.construir(); b.producto("B",Producto.FRUTAS,400,400);
        verificar(receta.getProductos().size()==1,"Builder no altera configuracion construida");
        rechaza(() -> new Nivel(7),"Rechaza nivel fuera de campana");
        rechaza(() -> ConfiguracionNivel.crear(1,"Tiempo invalido").tiempo(0).construir(),"Valida tiempo");
        rechaza(() -> ConfiguracionNivel.crear(1,"Sin objetivo").construir(),"Rechaza nivel vacio");
        rechaza(() -> new CatalogoNiveles(List.of(fixture(1),fixture(1))),"Rechaza numeros duplicados");
        rechaza(() -> new Nivel(ConfiguracionNivel.crear(1,"Sin estante").producto("A",Producto.FRUTAS,100,200).construir()),"Rechaza producto sin destino");
        rechaza(() -> new Nivel(ConfiguracionNivel.crear(1,"Jugador bloqueado").jugador(180,70)
            .estante(Producto.FRUTAS,170,60).producto("A",Producto.FRUTAS,100,200).construir()),"Valida posicion inicial libre");
        final int[] fabricados={0};
        ConfiguracionNivel clientes=ConfiguracionNivel.crear(1,"Fabrica compatible con Strategy")
            .estante(Producto.FRUTAS,170,60).producto("A",Producto.FRUTAS,100,200)
            .cliente(() -> {fabricados[0]++; return new Cliente(500,300,1);}).construir();
        Nivel conCliente=new Nivel(clientes); conCliente.cargar();
        verificar(fabricados[0]==2,"Invoca fabrica de comportamiento por cada intento");
        // Fixture pequeno ordenado: el nivel 2 del producto ahora es libre y tiene seis entregas.
        ConfiguracionNivel demoOrdenada=ConfiguracionNivel.crear(2,"Fixture de secuencia")
            .tiempo(120).entregasEnOrden(true)
            .estante(Producto.FRUTAS,170,60).estante(Producto.LACTEOS,430,60)
            .producto("Manzana",Producto.FRUTAS,104,220).producto("Leche",Producto.LACTEOS,440,270).construir();
        Juego orden=PartidasPrueba.crear(new CatalogoNiveles(List.of(c.obtener(1), demoOrdenada)));
        orden.iniciarPartida(); completar(orden); verificar(orden.avanzarNivel(),"Avanza a fixture ordenado");
        Producto segundo=orden.getNivel().getProductos().get(1);
        orden.getNivel().getJugador().setPosicion(segundo.getX(),segundo.getY()); orden.interactuar();
        verificar(orden.getNivel().getJugador().getProductoEnMano()==segundo && orden.getPuntuacion()==600,"Recoger fuera de orden no suma");
        verificar(!orden.getNivel().permiteEntrega(segundo),"Regla de entrega ordenada rechaza segundo");
        orden.soltarProducto();
        completar(orden); verificar(orden.getPuntuacion()==800,"Demo ordenada se completa");
        verificar(!orden.avanzarNivel() && orden.getNivelActual()==2 && orden.getEstado()==Juego.Estado.VICTORIA
            && !orden.isCampanaFinalizada(),"Falta nivel 3: no salta ni declara campana finalizada");
        ConfiguracionNivel parcial=ConfiguracionNivel.crear(1,"Objetivo parcial").objetivoEntregas(1)
            .estante(Producto.FRUTAS,170,60).producto("A",Producto.FRUTAS,100,200).producto("B",Producto.FRUTAS,400,400).construir();
        Juego objetivo=PartidasPrueba.crear(new CatalogoNiveles(List.of(parcial)));objetivo.iniciarPartida();entregar(objetivo,0);
        verificar(objetivo.getEstado()==Juego.Estado.VICTORIA,"Objetivo configurable independiente de cantidad total");
    }
    private static void campana() {
        List<ConfiguracionNivel> fixtures=new ArrayList<>();
        for(int i=1;i<=6;i++)fixtures.add(fixture(i));
        Juego j=PartidasPrueba.crear(new CatalogoNiveles(fixtures));j.iniciarPartida();
        verificar(j.getNivelActual()==1 && j.getPuntuacion()==0 && j.getPuntuacionInicioNivel()==0,"Nueva campana limpia");
        verificar(!j.avanzarNivel() && !j.reintentarNivel(),"No avanza ni reintenta jugando");
        completar(j); verificar(j.getPuntuacion()==200,"Puntos de nivel 1");
        verificar(j.avanzarNivel() && j.getNivelActual()==2 && j.getPuntuacionInicioNivel()==200,"Victoria avanza 1 a 2 y guarda puntaje");
        Nivel segundo=j.getNivel(); verificar(!j.avanzarNivel() && j.getNivel()==segundo,"Una victoria no avanza dos veces");
        entregar(j,0); verificar(j.getPuntuacion()==300,"Puntos provisionales del intento"); agotar(j);
        verificar(j.reintentarNivel() && j.getNivelActual()==2 && j.getNivel()!=segundo,"Reintento reconstruye mismo nivel");
        verificar(j.getPuntuacion()==200 && j.getPuntuacionInicioNivel()==200,"Descarta puntos fallidos y conserva anteriores");
        verificar(j.getNivel().getEntregados()==0 && j.getNivel().getJugador().getProductoEnMano()==null
            && !j.getNivel().getJugador().esInmune(),"Reintento restaura productos mano e inmunidad");
        cerca(j.getTiempoRestante(),10,"Restablece tiempo del nivel actual");
        cerca(j.getNivel().getJugador().getX(),48,"Restablece posicion X");
        cerca(j.getNivel().getJugador().getY(),42,"Restablece posicion Y");
        verificar(!j.reintentarNivel(),"No repite reintento en partida activa");
        entregar(j,0); agotar(j);j.reintentarNivel();verificar(j.getPuntuacion()==200,"Reintentos repetidos no explotan puntaje");
        completar(j);verificar(j.getPuntuacion()==400,"Nuevo intento solo suma una vez");
        for(int i=3;i<=6;i++) {
            verificar(j.avanzarNivel() && j.getNivelActual()==i,"Avanza secuencialmente con fixture");
            verificar(j.getPuntuacionInicioNivel()==(i-1)*200,"Conserva puntuacion acumulada");
            completar(j);
        }
        verificar(j.isCampanaFinalizada() && j.getNivelActual()==6 && j.getPuntuacion()==1200,"Finaliza solo al completar sexto nivel");
        verificar(!j.avanzarNivel(),"No existe avance al nivel 7");
        j.interactuar();j.actualizar(); verificar(j.getPuntuacion()==1200,"Final no duplica puntos");
        j.iniciarPartida();verificar(j.getNivelActual()==1 && j.getPuntuacion()==0 && j.getPuntuacionInicioNivel()==0
            && !j.isCampanaFinalizada() && j.getEstado()==Juego.Estado.JUGANDO,"Reinicio completo tras final");
        entregar(j,0);agotar(j);j.iniciarPartida();verificar(j.getPuntuacion()==0,"Nueva campana desde derrota elimina puntos");
    }
    private static void pulsar(GamePanel p, int tecla) {
        Object abajo=p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke(tecla,0,false));
        Object arriba=p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke(tecla,0,true));
        p.getActionMap().get(abajo).actionPerformed(new ActionEvent(p,0,"test"));
        p.avanzarFotograma(0);
        p.getActionMap().get(arriba).actionPerformed(new ActionEvent(p,0,"test"));
    }
    private static void captura(GamePanel panel, String nombre) throws Exception {
        panel.setSize(1280,800);BufferedImage imagen=new BufferedImage(1280,800,BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g=imagen.createGraphics();panel.paint(g);g.dispose();
        Path destino=Path.of("build/capturas-pruebas",nombre);Files.createDirectories(destino.getParent());
        verificar(ImageIO.write(imagen,"png",destino.toFile()),"Render Swing genera PNG: "+nombre);
    }
    private static void interfaz() throws Exception {
        // Le damos a la prueba un catalogo de 2 niveles para que pueda probar la pantalla de "Falta Nivel"
        CatalogoNiveles c = CatalogoNiveles.predeterminado();
        Juego j = PartidasPrueba.crear(new CatalogoNiveles(List.of(c.obtener(1), c.obtener(2))));
        GamePanel panel=new GamePanel(j,false);pulsar(panel,KeyEvent.VK_ENTER);
        verificar(panel.getTextoTiempo().equals("TIEMPO 03:00"),"HUD lee 180 del modelo");
        panel.avanzarFotograma(.1); cerca(j.getTiempoRestante(),179.9,"Panel actualiza modelo a pasos fijos");
        panel.avanzarFotograma(.5); cerca(j.getTiempoRestante(),179.4,"Fotograma lento no pierde tiempo real");
        completar(j);pulsar(panel,KeyEvent.VK_ENTER);
        verificar(j.getNivelActual()==2 && j.getPuntuacion()==600 && panel.getTextoTiempo().equals("TIEMPO 02:15"),"Enter avanza y HUD sincroniza tiempo y puntaje");
        captura(panel,"08_nivel2.png");
        entregar(j,0);agotar(j);pulsar(panel,KeyEvent.VK_ENTER);
        verificar(j.getNivelActual()==2 && j.getPuntuacion()==600 && j.getTiempoRestante()==135,"Enter reintenta sin perder progreso previo");
        captura(panel,"09_reintento.png");
        panel.avanzarFotograma(.5); cerca(j.getTiempoRestante(),134.5,"Fotograma lento tras reintento conserva tiempo real");
        completar(j);pulsar(panel,KeyEvent.VK_ENTER);verificar(j.getNivelActual()==2,"UI conserva victoria cuando falta nivel");
        captura(panel,"10_pendiente.png");pulsar(panel,KeyEvent.VK_R);
        verificar(j.getNivelActual()==1 && j.getPuntuacion()==0,"R inicia nueva campana desde victoria");
        agotar(j);pulsar(panel,KeyEvent.VK_R);verificar(j.getNivelActual()==1 && j.getPuntuacion()==0,"R inicia nueva desde derrota");
        List<ConfiguracionNivel> fixtures=new ArrayList<>();for(int i=1;i<=6;i++)fixtures.add(fixture(i));
        Juego finalJuego=PartidasPrueba.crear(new CatalogoNiveles(fixtures));finalJuego.iniciarPartida();
        for(int i=1;i<=6;i++){completar(finalJuego);if(i<6)finalJuego.avanzarNivel();}
        GamePanel finalPanel=new GamePanel(finalJuego,false);captura(finalPanel,"11_final_fixture.png");
        pulsar(finalPanel,KeyEvent.VK_ENTER);verificar(finalJuego.getNivelActual()==1 && !finalJuego.isCampanaFinalizada(),"Enter tras final reinicia");
        ConfiguracionNivel.Builder larga=ConfiguracionNivel.crear(1,"Lista larga de prueba").estante(Producto.FRUTAS,170,60);
        for(int i=0;i<9;i++)larga.producto("Fruta "+i,Producto.FRUTAS,40+i*50,400);
        Juego lista=PartidasPrueba.crear(new CatalogoNiveles(List.of(larga.construir())));lista.iniciarPartida();
        captura(new GamePanel(lista,false),"12_lista_fixture.png");
    }
}
