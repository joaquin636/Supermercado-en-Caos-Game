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
        if (!Double.isFinite(segundos)) throw new IllegalArgumentException("Duracion no finita.");
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
        boolean soltar = teclado.consumir(KeyEvent.VK_Q);
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
        boolean transicion = false;
        if ((estado == Juego.Estado.MENU && confirmar)
                || (reiniciar && (estado == Juego.Estado.VICTORIA || estado == Juego.Estado.DERROTA))) {
            juego.iniciarPartida(); transicion = true;
        } else if (confirmar && estado == Juego.Estado.DERROTA) transicion = juego.reintentarNivel();
        else if (confirmar && estado == Juego.Estado.VICTORIA) {
            if (juego.isCampanaFinalizada()) { juego.iniciarPartida(); transicion = true; }
            else transicion = juego.avanzarNivel();
        } else if (pausa) juego.alternarPausa();
        if (transicion) {
            teclado.limpiar(); acumulador = 0;
            // El fotograma de confirmacion no mueve ni descuenta tiempo del nuevo nivel.
            repaint(); return;
        }
        juego.setDireccion(teclado.ejeX(), teclado.ejeY());
        if (soltar) juego.soltarProducto();
        else if (interactuar) juego.interactuar();
        if (juego.getEstado() == Juego.Estado.JUGANDO) {
            // Recuperar todos los pasos: un fotograma lento no debe regalar tiempo.
            acumulador += Math.max(segundos, 0);
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
    private void texto(Graphics2D g,String t,int x,int y,int size,Color c) {g.setColor(c);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size));g.drawString(t,x,y);}
    private void centrado(Graphics2D g, String t, int y, int size, Color c) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
        texto(g, t, (640 - g.getFontMetrics().stringWidth(t)) / 2, y, size, c);
    }
    private void marco(Graphics2D g,int x,int y,int w,int h) {
        g.setColor(OSCURO);g.fillRect(x,y,w,h);g.setColor(ORO);g.drawRect(x+4,y+4,w-9,h-9);g.setColor(new Color(92,97,73));g.drawRect(x+8,y+8,w-17,h-17);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // Solo el arte se amplifica por vecino cercano. El texto se dibuja despues, a resolucion de ventana.
        Graphics2D g=escena.createGraphics();
        rect(g,0,0,640,400,0x17282c);
        mundo(g);
        g.dispose();
        Graphics2D out=(Graphics2D)graphics.create();
        out.setColor(OSCURO);out.fillRect(0,0,getWidth(),getHeight());
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double escala=Math.min(getWidth()/640.0,getHeight()/400.0);
        int w=(int)(640*escala),h=(int)(400*escala);
        out.drawImage(escena,(getWidth()-w)/2,(getHeight()-h)/2,w,h,null);
        out.translate((getWidth()-w)/2, (getHeight()-h)/2);
        out.scale(w / 640.0, h / 400.0);
        out.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        out.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        etiquetasMapa(out);
        hud(out);
        if(juego.getEstado()!=Juego.Estado.JUGANDO) pantalla(out);
        out.dispose();
    }
    private void mundo(Graphics2D g) {
        // Los limites visuales del suelo coinciden con los del modelo (960 x 540).
        rect(g,12,52,488,280,0x101f26);
        boolean segundo = juego.getNivelActual() == 2;
        rect(g,16,56,480,270,segundo ? 0x859fa7 : 0xa5ad91);
        for(int y=0;y<18;y++)for(int x=0;x<32;x++) {
            int px=16+x*15,py=56+y*15;
            rect(g,px,py,14,14,((x+y)%2==0)?(segundo?0xb7cbd0:0xc9c7a4):(segundo?0xa7bfc6:0xbebf9c));
            rect(g,px+1,py+1,12,1,segundo?0xcfe0e0:0xd4d1b0);
            if((x*13+y*7)%41==0)rect(g,px+9,py+10,2,1,0xa7ac8c);
        }
        // Fachada y rotulos quedan fuera de la zona transitable.
        rect(g,12,40,488,12,0x5c7463);rect(g,16,42,480,3,0x91aa7e);
        Graphics2D m=(Graphics2D)g.create();m.translate(16,56);m.scale(.5,.5);
        for(Obstaculo o:juego.getNivel().getObstaculos()) estante(m,o);
        for(Producto p:juego.getNivel().getProductos())if(p.estaDisponible()) {
            int x=(int)p.getX(),y=(int)p.getY();
            rect(m,x-4,y+21,33,5,0x91977c);
            producto(m,p,x,y-(int)(Math.sin(animacion*4)*2));
        }
        java.util.List<Personaje> orden=new java.util.ArrayList<>(juego.getNivel().getPersonajes());
        orden.sort(java.util.Comparator.comparingDouble(Personaje::getY));
        for(Personaje p:orden)persona(m,p);
        m.dispose();
        rect(g,18,313,70,13,0x45685b);
    }
    private void etiquetasMapa(Graphics2D g) {
        texto(g,"MERCADO LA ESQUINA",23,50,10,Color.WHITE);
        texto(g,"ENTRADA",24,323,10,Color.WHITE);
        for (Obstaculo obstaculo : juego.getNivel().getObstaculos()) {
            if (obstaculo instanceof Estante e) {
                int x = 16 + (int)e.getX()/2, y = 56 + ((int)e.getY()+e.getAlto())/2;
                g.setColor(OSCURO); g.fillRect(x+1,y-14,e.getAncho()/2-2,14);
                texto(g,e.getCategoria()+" "+e.getRecibidos().size()+"/"+cantidadCategoria(e.getCategoria()),x+4,y-3,10,Color.WHITE);
            }
        }
        Producto cercano = juego.getNivel().productoCercano();
        if (cercano != null && juego.getNivel().getJugador().getProductoEnMano() == null) {
            int x=16+(int)cercano.getX()/2, y=56+(int)cercano.getY()/2;
            g.setColor(OSCURO);g.fillRect(x-2,y-15,18,13);texto(g,"E",x+3,y-4,11,Color.WHITE);
        }
        for (Personaje p : juego.getNivel().getPersonajes()) {
            if (p instanceof Cliente cliente && juego.getEstado()==Juego.Estado.JUGANDO && cliente.estaDescansando()) {
                texto(g,"z",22+(int)p.getX()/2,51+(int)p.getY()/2,11,OSCURO);
            }
        }
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
            Producto mano=juego.getNivel().getJugador().getProductoEnMano();
            if(mano!=null&&e.aceptaProducto(mano)) {g.setColor(ORO);g.drawRect(x-3,y-3,w+6,h+6);}
        } else {
            rect(g,x,y,w,h,0x77553e);rect(g,x+3,y+3,w-6,h-10,0xb58b59);
            rect(g,x+5,y+5,w-10,3,0xd2ad73);rect(g,x+w/2-4,y+2,8,h-6,0xd8bc80);
            rect(g,x+12,y+23,21,17,0xe9d5a0);
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
    private long cantidadCategoria(String categoria) {
        return juego.getNivel().getProductos().stream().filter(p -> p.getCategoria().equals(categoria)).count();
    }
    public String getTextoTiempo() {
        int t = (int)Math.ceil(juego.getTiempoRestante());
        return String.format("TIEMPO %02d:%02d", t / 60, t % 60);
    }
    private void hud(Graphics2D g) {

        texto(g,"SUPERMERCADO EN CAOS",16,18,13,CREMA);
        texto(g,"NIVEL "+juego.getNivelActual()+" - "+juego.getNivel().getConfiguracion().getIdentificacion(),16,34,12,ORO);
        int t=(int)Math.ceil(juego.getTiempoRestante());
        texto(g,getTextoTiempo(),364,21,13,t<=30?new Color(255,139,106):Color.WHITE);
        texto(g,"PUNTOS "+juego.getPuntuacion(),521,23,12,ORO);
        marco(g,508,40,120,292);
        texto(g,"TU LISTA",520,63,14,ORO);texto(g,"Entregados "+juego.getNivel().getEntregados()+"/"+juego.getNivel().getObjetivoEntregas(),520,82,11,Color.WHITE);
        Producto siguiente = juego.getNivel().getSiguienteProducto();
        texto(g,siguiente == null ? (juego.getNivel().estaCompletado()?"COMPLETADO":"ORDEN LIBRE") : "SIGUE " + siguiente.getCategoria(),520,99,10,ORO);
        java.util.List<Producto> lista=juego.getNivel().getProductos();
        int pendientes=0;
        for(int i=0;i<lista.size();i++) {
            if(!lista.get(i).isEntregado()) { pendientes=i; break; }
        }
        int inicio=Math.max(0, pendientes-5);
        for(int i=inicio;i<Math.min(lista.size(),inicio+6);i++) {
            Producto p=lista.get(i);int y=122+(i-inicio)*23;

            rect(g,520,y-8,7,7,p.isEntregado()?0x8ca654:0x596362);
            texto(g,p.getNombre(),533,y,11,Color.WHITE);
        }
        texto(g,"EN MANO",521,271,12,ORO);
        Producto p=juego.getNivel().getJugador().getProductoEnMano();
        texto(g,p==null?"Sin producto":p.getNombre(),521,290,11,Color.WHITE);
        texto(g,p==null?"E para recoger":p.getCategoria(),521,309,10,CREMA);
        marco(g,12,334,616,60);
        String mensaje=juego.getMensaje();
        // Ajustar por ancho real de la fuente; las letras se dibujan a resolucion nativa.
        int linea=0;String actual="";
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));
        for(String palabra:mensaje.split(" ")) {
            if(g.getFontMetrics().stringWidth(actual+palabra)>590){texto(g,actual,24,352+linea*14,11,Color.WHITE);linea++;actual="";}
            actual+=palabra+" ";
        }
        texto(g,actual,24,352+linea*14,11,Color.WHITE);
        texto(g,"WASD / FLECHAS: mover     E: interactuar     Q: dejar     P / ESC: pausa",24,384,10,ORO);
    }
    private void pantalla(Graphics2D g) {
        g.setColor(new Color(14,25,29,190));g.fillRect(0,0,640,400);
        marco(g,106,65,428,272);
        Juego.Estado e=juego.getEstado();
        if(e==Juego.Estado.MENU&&!instrucciones) {
            centrado(g,"MERCADO LA ESQUINA",95,12,ORO);
            centrado(g,"SUPERMERCADO",130,28,CREMA);centrado(g,"EN CAOS",160,26,ORO);
            centrado(g,"Un turno para poner todo en su lugar",181,12,CREMA);
            String[] opciones={"JUGAR / INICIAR","INSTRUCCIONES","SALIR"};
            for(int i=0;i<3;i++) {
                int y=208+i*29;
                if(seleccion==i){rect(g,174,y-14,292,23,0x3e5c52);texto(g,">",184,y,12,ORO);}
                texto(g,opciones[i],218,y,14,seleccion==i?ORO:CREMA);
            }
            centrado(g,"Arriba / Abajo: elegir     Enter: aceptar",312,12,CREMA);
        } else if(instrucciones) {
            centrado(g,"COMO JUGAR",103,22,ORO);
            String[] lineas={"WASD o flechas: caminar por los pasillos.","E: recoger o entregar. Q: dejar en suelo.","Entrega en el estante de su categoria.","Si hay orden: mira SIGUE en la lista.","Un producto en mano. +100 por entrega.","Choque: -5 s; proteccion durante 1 s.","P / ESC: pausar. Cambiar ventana pausa.","ENTER / ESC: volver al menu."};
            for(int i=0;i<lineas.length;i++)texto(g,lineas[i],128,137+i*23,12,CREMA);
        } else {
            String titulo=juego.isCampanaFinalizada()?"CAMPANA COMPLETA":e==Juego.Estado.PAUSA?"TURNO EN PAUSA":e==Juego.Estado.VICTORIA?"TODO EN SU LUGAR":"SE ACABO EL TURNO";
            centrado(g,titulo,130,24,ORO);
            centrado(g,"Nivel "+juego.getNivelActual()+" - "+juego.getNivel().getConfiguracion().getIdentificacion(),154,12,CREMA);
            centrado(g,"Entregas: "+juego.getNivel().getEntregados()+" / "+juego.getNivel().getObjetivoEntregas()+"   Puntos: "+juego.getPuntuacion(),184,14,CREMA);
            centrado(g,e==Juego.Estado.PAUSA?"P / ESC para continuar":e==Juego.Estado.DERROTA?"ENTER reintenta / R nueva":juego.isCampanaFinalizada()?"ENTER / R nueva campana":juego.haySiguienteNivel()?"ENTER: ir al nivel "+(juego.getNivelActual()+1)+" / R: nueva campana":"Pendiente / R nueva campana",236,14,CREMA);
            centrado(g,e==Juego.Estado.VICTORIA?(juego.haySiguienteNivel()?"Puntos conservados para el siguiente nivel.":juego.isCampanaFinalizada()?"Seis niveles superados.":"Faltan configuraciones del equipo."):e==Juego.Estado.PAUSA?"El reloj y los clientes estan detenidos.":"Prueba rutas cortas y evita los clientes.",286,12,CREMA);
        }
    }
}
