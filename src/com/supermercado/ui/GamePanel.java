package com.supermercado.ui;

import com.supermercado.entities.*;
import com.supermercado.game.Juego;
import com.supermercado.input.ControlTeclado;
import com.supermercado.levels.Nivel;
import com.supermercado.obstacles.*;
import com.supermercado.products.Producto;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/** Toda la presentacion vive aqui; las ocho clases del UML conservan la logica. */
@SuppressWarnings("serial")
public final class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int ANCHO = 1280, ALTO = 800;
    private static final Color TINTA = new Color(26, 49, 49);
    private static final Color VERDE = new Color(25, 112, 91);
    private static final Color FONDO = new Color(245, 244, 235);
    private final Juego juego;
    private final ControlTeclado teclado = new ControlTeclado();
    private final Timer timer;
    private final boolean automatico;
    private long ultimoTick = System.nanoTime();
    private double acumulador;
    private int seleccion;
    private boolean instrucciones;
    private double animacion;
    private final java.awt.image.BufferedImage escena = new java.awt.image.BufferedImage(640,400,java.awt.image.BufferedImage.TYPE_INT_RGB);

    public GamePanel() { this(new Juego(), true); }
    public GamePanel(Juego juego, boolean automatico) {
        this.juego = juego;
        this.automatico = automatico;
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setMinimumSize(new Dimension(800, 500));
        setBackground(FONDO);
        teclado.instalar(this);
        timer = new Timer(16, e -> {
            long ahora = System.nanoTime();
            avanzarFotograma((ahora - ultimoTick) / 1_000_000_000.0);
            ultimoTick = ahora;
        });
        timer.setCoalesce(true);
    }
    @Override public void addNotify() {
        super.addNotify(); ultimoTick = System.nanoTime();
        if (automatico) timer.start();
    }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    /** Se separa del Timer para poder comprobar controles y simulacion sin una ventana. */
    public void avanzarFotograma(double segundos) {
        animacion += Math.max(0, Math.min(segundos, .15));
        boolean arriba = teclado.consumir(KeyEvent.VK_UP) | teclado.consumir(KeyEvent.VK_W);
        boolean abajo = teclado.consumir(KeyEvent.VK_DOWN) | teclado.consumir(KeyEvent.VK_S);
        if (juego.getEstado() == Juego.Estado.MENU) {
            if (arriba) seleccion = (seleccion + 2) % 3;
            if (abajo) seleccion = (seleccion + 1) % 3;
        }
        boolean confirmar = teclado.consumir(KeyEvent.VK_ENTER);
        boolean reiniciar = teclado.consumir(KeyEvent.VK_R);
        boolean pausa = teclado.consumir(KeyEvent.VK_P);
        pausa |= teclado.consumir(KeyEvent.VK_ESCAPE);
        boolean interactuar = teclado.consumir(KeyEvent.VK_E);
        Juego.Estado estado = juego.getEstado();
        if (estado == Juego.Estado.MENU) {
            if (instrucciones) { if (confirmar || pausa) instrucciones = false; repaint(); return; }
            if (confirmar && seleccion == 1) { instrucciones = true; repaint(); return; }
            if (confirmar && seleccion == 2) {
                java.awt.Window ventana = SwingUtilities.getWindowAncestor(this);
                if (ventana != null) ventana.dispose();
                repaint(); return;
            }
        }
        if ((confirmar && estado != Juego.Estado.JUGANDO && estado != Juego.Estado.PAUSA)
                || (reiniciar && (estado == Juego.Estado.VICTORIA || estado == Juego.Estado.DERROTA))) {
            juego.iniciarPartida(); teclado.limpiar(); acumulador = 0;
        } else if (pausa) juego.alternarPausa();
        juego.setDireccion(teclado.ejeX(), teclado.ejeY());
        if (interactuar) juego.interactuar();
        if (juego.getEstado() == Juego.Estado.JUGANDO) {
            acumulador += Math.min(Math.max(segundos, 0), .15);
            while (acumulador + 1e-10 >= Personaje.PASO) {
                juego.actualizar(); acumulador -= Personaje.PASO;
            }
        } else acumulador = 0;
        repaint();
    }

    public void perderFoco() {
        teclado.limpiar(); juego.setDireccion(0, 0); juego.pausar(); acumulador = 0;
    }

    private static final Color OSCURO = new Color(28,39,43), CREMA = new Color(244,224,174), ORO = new Color(229,169,74);
    private void rect(Graphics2D g, int x,int y,int w,int h,int rgb) {g.setColor(new Color(rgb));g.fillRect(x,y,w,h);}
    private void texto(Graphics2D g,String t,int x,int y,int size,Color c) {g.setColor(c);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,size));g.drawString(t,x,y);}
    private void marco(Graphics2D g,int x,int y,int w,int h) {
        g.setColor(OSCURO);g.fillRect(x,y,w,h);g.setColor(ORO);g.drawRect(x+4,y+4,w-9,h-9);g.setColor(new Color(92,97,73));g.drawRect(x+8,y+8,w-17,h-17);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // Escala entera de la ilustracion; sin suavizado de sprites.
        Graphics2D g=escena.createGraphics();
        rect(g,0,0,640,400,0x17282c);
        mundo(g);
        hud(g);
        if(juego.getEstado()!=Juego.Estado.JUGANDO) pantalla(g);
        g.dispose();
        Graphics2D out=(Graphics2D)graphics.create();
        out.setColor(OSCURO);out.fillRect(0,0,getWidth(),getHeight());
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double escala=Math.min(getWidth()/640.0,getHeight()/400.0);
        int w=(int)(640*escala),h=(int)(400*escala);
        out.drawImage(escena,(getWidth()-w)/2,(getHeight()-h)/2,w,h,null);out.dispose();
    }
    private void mundo(Graphics2D g) {
        // Los limites visuales del suelo coinciden con los del modelo (960 x 540).
        rect(g,12,52,488,280,0x101f26);
        rect(g,16,56,480,270,0xa5ad91);
        for(int y=0;y<18;y++)for(int x=0;x<32;x++) {
            int px=16+x*15,py=56+y*15;
            rect(g,px,py,14,14,((x+y)%2==0)?0xc9c7a4:0xbebf9c);
            rect(g,px+1,py+1,12,1,0xd4d1b0);
            if((x*13+y*7)%41==0)rect(g,px+9,py+10,2,1,0xa7ac8c);
        }
        // Fachada y rotulos quedan fuera de la zona transitable.
        rect(g,12,40,488,12,0x5c7463);rect(g,16,42,480,3,0x91aa7e);
        texto(g,"MERCADO LA ESQUINA",26,49,8,CREMA);
        texto(g,"REPOSICION / TURNO 01",336,49,7,CREMA);
        Graphics2D m=(Graphics2D)g.create();m.translate(16,56);m.scale(.5,.5);
        for(Obstaculo o:juego.getNivel().getObstaculos()) estante(m,o);
        for(Producto p:juego.getNivel().getProductos())if(p.estaDisponible()) {
            int x=(int)p.getX(),y=(int)p.getY();
            rect(m,x-4,y+21,33,5,0x91977c);
            producto(m,p,x,y-(int)(Math.sin(animacion*4)*2));
            if(juego.getNivel().productoCercano()==p)texto(m,"[E]",x-2,y-9,14,OSCURO);
        }
        java.util.List<Personaje> orden=new java.util.ArrayList<>(juego.getNivel().getPersonajes());
        orden.sort(java.util.Comparator.comparingDouble(Personaje::getY));
        for(Personaje p:orden)persona(m,p);
        m.dispose();
        rect(g,18,318,50,8,0x647f70);texto(g,"ENTRADA",23,324,7,CREMA);
    }
    private int color(String c) {return Producto.FRUTAS.equals(c)?0x8ca654:Producto.LACTEOS.equals(c)?0x84b5bd:0xad87ae;}
    private void estante(Graphics2D g,Obstaculo o) {
        int x=(int)o.getX(),y=(int)o.getY(),w=o.getAncho(),h=o.getAlto();
        rect(g,x+4,y+h,w,8,0x92997f);
        if(o instanceof Estante) {
            Estante e=(Estante)o;
            rect(g,x,y,w,h,0x394e49);rect(g,x+4,y+4,w-8,h-12,0x846b4e);
            for(int fila=0;fila<2;fila++) {
                for(int i=0;i<10;i++) {
                    int px=x+8+i*16,py=y+7+fila*26;
                    rect(g,px,py+2,10,17,color(e.getCategoria()));
                    rect(g,px+2,py,6,3,0xf3dfae);rect(g,px+2,py+7,6,4,0xded7b4);
                    rect(g,px+8,py+3,2,15,0x4a6655);
                }
                rect(g,x+3,y+27+fila*26,w-6,4,0xc4a071);
            }
            rect(g,x+2,y+57,w-4,16,color(e.getCategoria()));
            texto(g,e.getCategoria()+" "+e.getRecibidos().size()+"/2",x+8,y+70,16,OSCURO);
            Producto mano=juego.getNivel().getJugador().getProductoEnMano();
            if(mano!=null&&e.aceptaProducto(mano)) {g.setColor(ORO);g.drawRect(x-3,y-3,w+6,h+6);}
        } else {
            rect(g,x,y,w,h,0x77553e);rect(g,x+3,y+3,w-6,h-10,0xb58b59);
            rect(g,x+5,y+5,w-10,3,0xd2ad73);rect(g,x+w/2-4,y+2,8,h-6,0xd8bc80);
            rect(g,x+12,y+23,21,17,0xe9d5a0);texto(g,"UP",x+14,y+36,11,OSCURO);
            rect(g,x+2,y+h-9,w-4,7,0x966d47);
        }
    }
    private void producto(Graphics2D g,Producto p,int x,int y) {
        if(Producto.FRUTAS.equals(p.getCategoria())) {
            rect(g,x+6,y+5,15,18,0x943e3c);rect(g,x+3,y+8,21,11,0xc96049);
            rect(g,x+7,y+6,4,5,0xed9760);rect(g,x+12,y+1,3,6,0x594936);rect(g,x+15,y+1,7,3,0x63814d);
        } else {
            rect(g,x+4,y+5,18,20,0x354e51);rect(g,x+6,y+5,14,18,color(p.getCategoria()));
            rect(g,x+8,y+1,10,5,0xf1dfb3);rect(g,x+8,y+11,10,6,0xf6e9c8);
        }
    }
    private void persona(Graphics2D g,Personaje p) {
        int x=(int)p.getX(),y=(int)p.getY();boolean j=p instanceof Jugador;
        rect(g,x+2,y+26,28,5,0x8d967c);
        if(j&&((Jugador)p).esInmune()&&((int)(animacion*12)%2==0))return;
        int paso=(Math.abs(p.getDesplazamientoX())+Math.abs(p.getDesplazamientoY())>.01)?((int)(animacion*9)%2)*3:0;
        rect(g,x+6,y+22,7,8-paso,0x283d42);rect(g,x+19,y+22,7,5+paso,0x283d42);
        rect(g,x+3,y+11,25,13,j?0x456d70:0xb4684f);
        rect(g,x+9,y+13,13,13,j?0xd8b66c:0x775c71);
        rect(g,x+2,y+15,5,8,0xdfad7c);rect(g,x+26,y+15,4,8,0xdfad7c);
        rect(g,x+7,y-4,18,17,0x443d36);rect(g,x+8,y,16,12,0xe6b783);
        rect(g,x+6,y-5,20,6,j?0x527d67:0x644837);rect(g,x+5,y,23,3,j?0x7ca178:0x644837);
        boolean espalda=p.getDesplazamientoY()<0;
        if(espalda)rect(g,x+8,y+3,16,7,0x574738);
        else {rect(g,x+11,y+5,2,3,0x2e3938);rect(g,x+20,y+5,2,3,0x2e3938);}
        if(j&&((Jugador)p).getProductoEnMano()!=null)producto(g,((Jugador)p).getProductoEnMano(),x+23,y+12);
    }
    private void hud(Graphics2D g) {
        texto(g,"SUPERMERCADO",16,17,10,CREMA);texto(g,"EN CAOS",16,31,13,ORO);
        int t=(int)Math.ceil(juego.getTiempoRestante());
        texto(g,String.format("TIEMPO %02d:%02d",t/60,t%60),226,23,11,t<=30?new Color(235,119,83):CREMA);
        texto(g,"PTS "+String.format("%03d",juego.getPuntuacion()),421,23,11,ORO);
        marco(g,508,40,120,292);
        texto(g,"LA LISTA",521,62,11,ORO);texto(g,"ORDENA 6",521,81,9,CREMA);texto(g,"PRODUCTOS",521,94,9,CREMA);
        for(int i=0;i<6;i++) {
            Producto p=juego.getNivel().getProductos().get(i);int y=119+i*23;
            rect(g,520,y-8,7,7,p.isEntregado()?0x8ca654:0x596362);
            texto(g,p.getNombre(),533,y,8,CREMA);
        }
        texto(g,"EN MANO",521,270,9,ORO);
        Producto p=juego.getNivel().getJugador().getProductoEnMano();
        texto(g,p==null?"Vacia":p.getNombre(),521,286,8,CREMA);
        texto(g,p==null?"Busca en suelo":p.getCategoria(),521,302,8,CREMA);
        marco(g,12,340,616,48);
        String mensaje=juego.getMensaje();
        // Ajuste por palabras para evitar que los mensajes invadan los bordes.
        int linea=0;String actual="";
        for(String palabra:mensaje.split(" ")) {
            if((actual+palabra).length()>92){texto(g,actual,24,355+linea*11,9,CREMA);linea++;actual="";}
            actual+=palabra+" ";
        }
        texto(g,actual,24,355+linea*11,9,CREMA);
        texto(g,"WASD / FLECHAS  MOVER     E  INTERACTUAR     P / ESC  PAUSA",24,379,8,ORO);
    }
    private void pantalla(Graphics2D g) {
        g.setColor(new Color(14,25,29,190));g.fillRect(0,0,640,400);
        marco(g,106,65,428,272);
        Juego.Estado e=juego.getEstado();
        if(e==Juego.Estado.MENU&&!instrucciones) {
            texto(g,"MERCADO LA ESQUINA PRESENTA",158,91,9,ORO);
            texto(g,"SUPERMERCADO",166,127,25,CREMA);texto(g,"EN CAOS",238,155,23,ORO);
            texto(g,"Un turno para poner todo en su lugar",161,177,9,CREMA);
            String[] opciones={"JUGAR / INICIAR","INSTRUCCIONES","SALIR"};
            for(int i=0;i<3;i++) {
                int y=208+i*29;
                if(seleccion==i){rect(g,174,y-14,292,23,0x3e5c52);texto(g,">",184,y,12,ORO);}
                texto(g,opciones[i],218,y,12,seleccion==i?ORO:CREMA);
            }
            texto(g,"ARRIBA / ABAJO   ELEGIR     ENTER   ACEPTAR",143,312,9,CREMA);
        } else if(instrucciones) {
            texto(g,"COMO JUGAR",240,103,17,ORO);
            String[] lineas={"WASD o flechas: caminar por los pasillos.","E: recoger un producto cercano.","E junto al estante correcto: entregar.","Solo puedes llevar un producto a la vez.","6 entregas en 180 segundos = victoria.","Cada cliente que tocas resta 5 segundos.","P / ESC: pausar. Cambiar ventana pausa.","ENTER / ESC: volver al menu."};
            for(int i=0;i<lineas.length;i++)texto(g,lineas[i],128,137+i*23,9,CREMA);
        } else {
            String titulo=e==Juego.Estado.PAUSA?"TURNO EN PAUSA":e==Juego.Estado.VICTORIA?"TODO EN SU LUGAR":"SE ACABO EL TURNO";
            texto(g,titulo,156,132,20,ORO);
            texto(g,"Entregas: "+juego.getNivel().getEntregados()+" / 6   Puntos: "+juego.getPuntuacion(),163,178,12,CREMA);
            texto(g,e==Juego.Estado.PAUSA?"P / ESC para continuar":"ENTER / R para volver a jugar",180,234,12,CREMA);
            texto(g,e==Juego.Estado.VICTORIA?"La tienda esta lista para abrir.":e==Juego.Estado.PAUSA?"El reloj y los clientes estan detenidos.":"Prueba rutas cortas y evita los clientes.",147,286,9,CREMA);
        }
    }
}
