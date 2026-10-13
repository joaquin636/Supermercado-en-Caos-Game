package com.supermercado.game;

import com.supermercado.entities.*;
import com.supermercado.levels.Nivel;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;

/** Coordinador del UML. No depende de ventanas ni de eventos Swing. */
public final class Juego {
    // Enum provisional: NO se presenta como implementacion del patron State.
    public enum Estado { MENU, JUGANDO, PAUSA, VICTORIA, DERROTA }
    private int puntuacion;
    private double tiempoRestante;
    private Nivel nivel;
    private Estado estado = Estado.MENU;
    private String mensaje = "Organiza los seis productos antes de que termine el tiempo.";
    private double duracionMensaje;

    public Juego() { nivel = new Nivel(1); tiempoRestante = nivel.getTiempoLimite(); }

    public void iniciarPartida() {
        nivel = new Nivel(1); puntuacion = 0; tiempoRestante = nivel.getTiempoLimite();
        estado = Estado.JUGANDO;
        avisar("Acercate a un producto y pulsa E para recogerlo.");
    }

    /** Paso fijo de 1/60 s: la interfaz puede dibujar a otra frecuencia. */
    public void actualizar() {
        if (estado != Estado.JUGANDO) return;
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
        if (tiempoRestante <= 0) terminarPartida();
    }

    public void interactuar() {
        if (estado != Estado.JUGANDO) return;
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
            if (j.entregarProducto(e)) {
                puntuacion += enMano.getPuntos();
                avisar("Entrega correcta: +" + enMano.getPuntos() + " puntos.");
                if (nivel.estaCompletado()) { estado = Estado.VICTORIA; j.setDireccion(0, 0); }
            }
        }
    }

    public void alternarPausa() {
        if (estado == Estado.JUGANDO) { estado = Estado.PAUSA; nivel.getJugador().setDireccion(0, 0); }
        else if (estado == Estado.PAUSA) estado = Estado.JUGANDO;
    }
    public void pausar() { if (estado == Estado.JUGANDO) alternarPausa(); }
    public void terminarPartida() { estado = Estado.DERROTA; nivel.getJugador().setDireccion(0, 0); }
    private void avisar(String texto) { mensaje = texto; duracionMensaje = 4.5; }
    public void setDireccion(int dx, int dy) { nivel.getJugador().setDireccion(dx, dy); }
    public Estado getEstado() { return estado; }
    public Nivel getNivel() { return nivel; }
    public int getPuntuacion() { return puntuacion; }
    public double getTiempoRestante() { return tiempoRestante; }
    public String getMensaje() { return duracionMensaje > 0 ? mensaje : getAyudaContextual(); }
    public String getAyudaContextual() {
        Jugador j = nivel.getJugador();
        if (j.getProductoEnMano() != null) return "E: entregar en el estante de " + j.getProductoEnMano().getCategoria();
        Producto p = nivel.productoCercano();
        return p == null ? "Busca productos en el suelo. WASD o flechas para moverte." : "E: recoger " + p.getNombre();
    }
}
