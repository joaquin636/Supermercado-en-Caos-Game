package com.supermercado.game;

import com.supermercado.entities.*;
import com.supermercado.levels.*;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;

/** Coordinador del UML. No depende de ventanas ni de eventos Swing. */
public final class Juego {
    // Enum provisional: NO se presenta como implementacion del patron State.
    public enum Estado { MENU, JUGANDO, PAUSA, VICTORIA, DERROTA }
    private final CatalogoNiveles catalogo;
    private int puntuacion;
    private int puntuacionInicioNivel;
    private boolean campanaFinalizada;
    private double tiempoRestante;
    private Nivel nivel;
    private Estado estado = Estado.MENU;
    private String mensaje = "Organiza los seis productos antes de que termine el tiempo.";
    private double duracionMensaje;

    public Juego() { this(CatalogoNiveles.predeterminado()); }
    public Juego(CatalogoNiveles catalogo) {
        this.catalogo = java.util.Objects.requireNonNull(catalogo);
        nivel = new Nivel(catalogo.obtener(1)); tiempoRestante = nivel.getTiempoLimite();
    }

    private void cargarNivel(int numero) {
        Nivel nuevo = new Nivel(catalogo.obtener(numero));
        nivel = nuevo; puntuacionInicioNivel = puntuacion;
        tiempoRestante = nivel.getTiempoLimite(); estado = Estado.JUGANDO;
        avisar(nivel.getObjetivo());
    }

    /** Consume una victoria una sola vez; si falta la configuracion conserva el resultado. */
    public boolean avanzarNivel() {
        if (estado != Estado.VICTORIA || campanaFinalizada) return false;
        int siguiente = getNivelActual() + 1;
        if (!catalogo.contiene(siguiente)) {
            avisar("Nivel " + siguiente + " pendiente de integracion. R: nueva campana."); return false;
        }
        cargarNivel(siguiente); return true;
    }

    public boolean reintentarNivel() {
        if (estado != Estado.DERROTA) return false;
        int numero = getNivelActual();
        puntuacion = puntuacionInicioNivel; cargarNivel(numero); return true;
    }

    public int getNivelActual() { return nivel.getNumero(); }
    public int getPuntuacionInicioNivel() { return puntuacionInicioNivel; }
    public boolean isCampanaFinalizada() { return campanaFinalizada; }
    public boolean haySiguienteNivel() {
        return !campanaFinalizada && catalogo.contiene(getNivelActual() + 1);
    }


    public void iniciarPartida() {
        puntuacion = 0; campanaFinalizada = false; cargarNivel(1);
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
        if (tiempoRestante < 1e-9) { tiempoRestante = 0; terminarPartida(); }
    }

    public void interactuar() {
        if (estado != Estado.JUGANDO) return;
        Jugador j = nivel.getJugador();
        Producto enMano = j.getProductoEnMano();
        if (enMano == null) {
            Producto p = nivel.productoCercano();
            if (p == null) { avisar("Acercate un poco mas a un producto."); return; }
            if (!nivel.permiteEntrega(p)) { avisar("Recoge primero el siguiente producto de la lista."); return; }
            j.recogerProducto(p);
            avisar("Llevas " + p.getNombre() + ". Buscale el estante de " + p.getCategoria() + ".");
        } else {
            Estante e = nivel.estanteCercano();
            if (e == null) { avisar("Lleva el producto cerca de su estante y pulsa E."); return; }
            if (!e.aceptaProducto(enMano)) { avisar("Estante incorrecto: este producto pertenece a " + enMano.getCategoria() + "."); return; }
            if (!nivel.permiteEntrega(enMano)) { avisar("Entrega primero los productos anteriores de la lista."); return; }
            if (j.entregarProducto(e)) {
                puntuacion += enMano.getPuntos();
                avisar("Entrega correcta: +" + enMano.getPuntos() + " puntos.");
                if (nivel.estaCompletado()) {
                    estado = Estado.VICTORIA; j.setDireccion(0, 0);
                    campanaFinalizada = getNivelActual() == CatalogoNiveles.ULTIMO_NIVEL;
                }

            }
        }
    }

    public void alternarPausa() {
        if (estado == Estado.JUGANDO) { estado = Estado.PAUSA; nivel.getJugador().setDireccion(0, 0); }
        else if (estado == Estado.PAUSA) estado = Estado.JUGANDO;
    }
    public void pausar() { if (estado == Estado.JUGANDO) alternarPausa(); }
    public void terminarPartida() {
        if (estado != Estado.JUGANDO && estado != Estado.PAUSA) return;
        estado = Estado.DERROTA; nivel.getJugador().setDireccion(0, 0);
    }

    private void avisar(String texto) { mensaje = texto; duracionMensaje = 4.5; }
    public void setDireccion(int dx, int dy) { nivel.getJugador().setDireccion(dx, dy); }
    public Estado getEstado() { return estado; }
    public Nivel getNivel() { return nivel; }
    public int getPuntuacion() { return puntuacion; }
    public double getTiempoRestante() { return tiempoRestante; }
    public String getMensaje() {
        if (estado == Estado.VICTORIA) {
            if (campanaFinalizada) return "Campana completada: seis niveles superados. R / ENTER: nueva campana.";
            return haySiguienteNivel() ? "Nivel superado. ENTER: siguiente nivel. R: nueva campana."
                    : "Nivel " + (getNivelActual() + 1) + " pendiente de integracion. R: nueva campana.";
        }
        if (estado == Estado.DERROTA) return "ENTER: reintentar nivel. R: nueva campana. Se descartan los puntos del intento fallido.";
        return duracionMensaje > 0 ? mensaje : getAyudaContextual();
    }

    public String getAyudaContextual() {
        Jugador j = nivel.getJugador();
        if (j.getProductoEnMano() != null) return "E: entregar en el estante de " + j.getProductoEnMano().getCategoria();
        Producto p = nivel.productoCercano();
        return p == null ? "Busca productos en el suelo. WASD o flechas para moverte." : "E: recoger " + p.getNombre();
    }
}
