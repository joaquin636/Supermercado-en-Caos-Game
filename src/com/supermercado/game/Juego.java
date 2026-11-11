package com.supermercado.game;

import com.supermercado.entities.*;
import com.supermercado.levels.*;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;
import com.supermercado.game.estados.*;

/** Coordinador del modelo de juego adaptado al Patrón State con compatibilidad de pruebas. */
public final class Juego {
    
    // Enum de compatibilidad para GamePanel y las pruebas automatizadas existentes
    public enum Estado { MENU, JUGANDO, PAUSA, VICTORIA, DERROTA }

    private final CatalogoNiveles catalogo;
    private int puntuacion;
    private int puntuacionInicioNivel;
    private boolean campanaFinalizada;
    private double tiempoRestante;
    private Nivel nivel;
    
    // Estado activo mediante el Patrón State
    private EstadoJuego estadoActual;
    
    private String mensaje = "Organiza los seis productos antes de que termine el tiempo.";
    private double duracionMensaje;

    public Juego() { this(CatalogoNiveles.predeterminado()); }
    
    public Juego(CatalogoNiveles catalogo) {
        this.catalogo = java.util.Objects.requireNonNull(catalogo);
        nivel = new Nivel(catalogo.obtener(1)); 
        tiempoRestante = nivel.getTiempoLimite();
        // Arranca en el Menú principal
        this.estadoActual = new EstadoMenu(this);
    }

    public void cambiarEstado(EstadoJuego nuevoEstado) {
        this.estadoActual = java.util.Objects.requireNonNull(nuevoEstado);
    }

    public EstadoJuego getEstadoActual() {
        return estadoActual;
    }

    public void cargarNivel(int numero) {
        Nivel nuevo = new Nivel(catalogo.obtener(numero));
        nivel = nuevo; 
        puntuacionInicioNivel = puntuacion;
        tiempoRestante = nivel.getTiempoLimite(); 
        avisar(nivel.getObjetivo());
        // Al cargar nivel, pasamos al estado de juego activo
        cambiarEstado(new EstadoJugando(this));
    }

    /** Consume una victoria una sola vez; si falta la configuracion conserva el resultado. */
    public boolean avanzarNivel() {
        if (!(estadoActual instanceof EstadoResultado resultado) || !resultado.isVictoria() || campanaFinalizada) return false;
        int siguiente = getNivelActual() + 1;
        if (!catalogo.contiene(siguiente)) {
            avisar("Nivel " + siguiente + " pendiente de integracion. R: nueva campana."); 
            return false;
        }
        cargarNivel(siguiente); 
        return true;
    }

    public boolean reintentarNivel() {
        if (!(estadoActual instanceof EstadoResultado resultado) || resultado.isVictoria()) return false;
        int numero = getNivelActual();
        puntuacion = puntuacionInicioNivel; 
        cargarNivel(numero); 
        return true;
    }

    public int getNivelActual() { return nivel.getNumero(); }
    public int getPuntuacionInicioNivel() { return puntuacionInicioNivel; }
    public boolean isCampanaFinalizada() { return campanaFinalizada; }
    public boolean haySiguienteNivel() {
        return !campanaFinalizada && catalogo.contiene(getNivelActual() + 1);
    }

    public void iniciarPartida() {
        puntuacion = 0; 
        campanaFinalizada = false; 
        cargarNivel(1);
        avisar("Acercate a un producto y pulsa E para recogerlo.");
    }

    /** Delegación al estado activo del Patrón State */
    public void actualizar() {
        estadoActual.actualizar();
    }

    public void manejarTeclado(int keyCode, boolean pressed) {
        estadoActual.manejarTeclado(keyCode, pressed);
    }

    // --- Lógica interna de simulación (llamada por EstadoJugando) ---
    public void actualizarSimulacionPartida() {
        nivel.actualizarPersonajes();
        duracionMensaje = Math.max(0, duracionMensaje - Personaje.PASO);
        Jugador jugador = nivel.getJugador();
        for (Personaje p : nivel.getPersonajes()) {
            if (p instanceof Cliente && p.getBounds().intersects(jugador.getBounds()) && jugador.recibirImpacto()) {
                tiempoRestante = Math.max(0, tiempoRestante - 5);
                avisar("Choque con cliente: -5 segundos. Tienes 1 segundo de proteccion.");
            }
        }
        tiempoRestante = Math.max(0, tiempoRestante - Personaje.PASO);
        if (tiempoRestante < 1e-9) { 
            tiempoRestante = 0; 
            terminarPartida(false); 
        }
    }

    public void interactuar() {
        if (!(estadoActual instanceof EstadoJugando)) return;
        Jugador j = nivel.getJugador();
        Producto enMano = j.getProductoEnMano();
        if (enMano == null) {
            Producto p = nivel.productoCercano();
            if (p == null) { avisar("Acercate un poco mas a un producto."); return; }
            j.recogerProducto(p);
            avisar("Llevas " + p.getNombre() + ". Buscale el estante de " + p.getCategoria() + ".");
        } else {
            Estante e = nivel.estanteCercano();
            if (e == null) { avisar("Lleva el producto cerca de su estante y pulsa E."); return; }
            if (!e.aceptaProducto(enMano)) { avisar("Estante incorrecto: este producto pertenece a " + enMano.getCategoria() + "."); return; }
            if (!nivel.permiteEntrega(enMano)) {
                Producto siguiente = nivel.getSiguienteProducto();
                avisar("Fuera de orden. Sigue: " + (siguiente == null ? "objetivo completado" : siguiente.getCategoria())
                        + ". Conservas el producto. Q: dejar en suelo.");
                return;
            }
            if (j.entregarProducto(e)) {
                puntuacion += enMano.getPuntos();
                avisar("Entrega correcta: +" + enMano.getPuntos() + " puntos.");
                if (nivel.estaCompletado()) {
                    j.setDireccion(0, 0);
                    campanaFinalizada = getNivelActual() == CatalogoNiveles.ULTIMO_NIVEL;
                    if (campanaFinalizada) {
                        cambiarEstado(new EstadoFinalCampana(this));
                    } else {
                        cambiarEstado(new EstadoResultado(this, true));
                    }
                }
            }
        }
    }

    public void alternarPausa() {
        if (estadoActual instanceof EstadoJugando) {
            nivel.getJugador().setDireccion(0, 0);
            cambiarEstado(new EstadoPausa(this, estadoActual));
        } else if (estadoActual instanceof EstadoPausa) {
            cambiarEstado(new EstadoJugando(this));
        }
    }

    public void soltarProducto() {
        // Solo permitir soltar el producto si estamos en pleno juego (no en pausa, menú ni resultados)
        if (estadoActual instanceof EstadoJugando) {
            if (nivel.getJugador().soltarProducto()) {
                avisar("Producto en el suelo. E: recoger de nuevo.");
            }
        }
    }

    public void pausar() { 
        if (estadoActual instanceof EstadoJugando) alternarPausa(); 
    }

    public void terminarPartida() {
        terminarPartida(false);
    }

    public void terminarPartida(boolean victoria) {
        if (!(estadoActual instanceof EstadoJugando) && !(estadoActual instanceof EstadoPausa)) return;
        nivel.getJugador().setDireccion(0, 0);
        if (victoria) {
            campanaFinalizada = getNivelActual() == CatalogoNiveles.ULTIMO_NIVEL;
            if (campanaFinalizada) {
                cambiarEstado(new EstadoFinalCampana(this));
                return;
            }
        }
        cambiarEstado(new EstadoResultado(this, victoria));
    }

    private void avisar(String texto) { mensaje = texto; duracionMensaje = 4.5; }
    public void setDireccion(int dx, int dy) { nivel.getJugador().setDireccion(dx, dy); }
    
    // Método adaptador para que GamePanel y las pruebas consulten el estado clásico
    public Estado getEstado() {
        if (estadoActual instanceof EstadoMenu) return Estado.MENU;
        if (estadoActual instanceof EstadoJugando) return Estado.JUGANDO;
        if (estadoActual instanceof EstadoPausa) return Estado.PAUSA;
        if (estadoActual instanceof EstadoResultado resultado) {
            return resultado.isVictoria() ? Estado.VICTORIA : Estado.DERROTA;
        }
        if (estadoActual instanceof EstadoFinalCampana) return Estado.VICTORIA;
        return Estado.JUGANDO;
    }

    // Métodos de consulta
    public Nivel getNivel() { return nivel; }
    public int getPuntuacion() { return puntuacion; }
    public double getTiempoRestante() { return tiempoRestante; }
    
    public String getMensaje() {
        if (getEstado() == Estado.VICTORIA) {
            if (campanaFinalizada) return "Campana completada: seis niveles superados. R / ENTER: nueva campana.";
            return haySiguienteNivel() ? "Nivel superado. ENTER: siguiente nivel. R: nueva campana."
                    : "Nivel " + (getNivelActual() + 1) + " pendiente de integracion. R: nueva campana.";
        }
        if (getEstado() == Estado.DERROTA) return "ENTER: reintentar nivel. R: nueva campana. Se descartan los puntos del intento fallido.";
        return duracionMensaje > 0 ? mensaje : getAyudaContextual();
    }

    public String getAyudaContextual() {
        Jugador j = nivel.getJugador();
        Producto siguiente = nivel.getSiguienteProducto();
        String orden = siguiente == null ? "" : " Sigue: " + siguiente.getCategoria() + ".";
        if (j.getProductoEnMano() != null) return "E: entregar en " + j.getProductoEnMano().getCategoria()
                + ". Q: dejar en suelo." + orden;
        Producto p = nivel.productoCercano();
        return (p == null ? "Busca productos en el suelo. WASD o flechas para moverte." : "E: recoger " + p.getNombre()) + orden;
    }
}