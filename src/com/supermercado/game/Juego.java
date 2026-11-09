package com.supermercado.game;

import com.supermercado.entities.*;
import com.supermercado.game.eventos.*;
import com.supermercado.levels.*;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;
import java.util.ArrayList;
import java.util.List;

/** Coordinador del UML. No depende de ventanas ni de eventos Swing. */
public final class Juego {
    // Enum provisional: NO se presenta como implementacion del patron State.
    public enum Estado { MENU, JUGANDO, PAUSA, VICTORIA, DERROTA }
    private final CatalogoNiveles catalogo;
    private final List<ObservadorJuego> observadores = new ArrayList<>();
    private final RegistroEstadisticas estadisticas = new RegistroEstadisticas();
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
        agregarObservador(estadisticas);
        nivel = new Nivel(catalogo.obtener(1));
        tiempoRestante = nivel.getTiempoLimite();
    }

    public void agregarObservador(ObservadorJuego observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void retirarObservador(ObservadorJuego observador) {
        observadores.remove(observador);
    }

    public RegistroEstadisticas getEstadisticas() {
        return estadisticas;
    }

    private void notificarEvento(TipoEvento tipo, int deltaPuntos, String detalle) {
        EventoJuego evento = new EventoJuego(tipo, getNivelActual(), deltaPuntos, puntuacion, detalle);
        for (ObservadorJuego obs : new ArrayList<>(observadores)) {
            obs.onEvento(evento);
        }
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
        puntuacion = 0; campanaFinalizada = false;
        estadisticas.reiniciar();
        cargarNivel(1);
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
                String avisoChoque = "Choque con cliente: -5 segundos. Tienes 1 segundo de proteccion.";
                avisar(avisoChoque);
                notificarEvento(TipoEvento.CHOQUE_CLIENTE, 0, avisoChoque);
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
            j.recogerProducto(p);
            avisar("Llevas " + p.getNombre() + ". Buscale el estante de " + p.getCategoria() + ".");
        } else {
            Estante e = nivel.estanteCercano();
            if (e == null) { avisar("Lleva el producto cerca de su estante y pulsa E."); return; }
            if (!e.aceptaProducto(enMano)) {
                String msgError = "Estante incorrecto: este producto pertenece a " + enMano.getCategoria() + ".";
                avisar(msgError);
                notificarEvento(TipoEvento.ENTREGA_INCORRECTA, 0, msgError);
                return;
            }
            if (!nivel.permiteEntrega(enMano)) {
                Producto siguiente = nivel.getSiguienteProducto();
                String msgOrden = "Fuera de orden. Sigue: " + (siguiente == null ? "objetivo completado" : siguiente.getCategoria())
                        + ". Conservas el producto. Q: dejar en suelo.";
                avisar(msgOrden);
                notificarEvento(TipoEvento.ENTREGA_INCORRECTA, 0, msgOrden);
                return;
            }
            if (j.entregarProducto(e)) {
                int ganados = enMano.getPuntos();
                puntuacion += ganados;
                String msgOk = "Entrega correcta: +" + ganados + " puntos.";
                avisar(msgOk);
                notificarEvento(TipoEvento.ENTREGA_CORRECTA, ganados, msgOk);
                if (nivel.estaCompletado()) {
                    estado = Estado.VICTORIA; j.setDireccion(0, 0);
                    campanaFinalizada = getNivelActual() == CatalogoNiveles.ULTIMO_NIVEL;
                    notificarEvento(TipoEvento.FIN_NIVEL, 0, "VICTORIA");
                }
            }
        }
    }

    public void alternarPausa() {
        if (estado == Estado.JUGANDO) { estado = Estado.PAUSA; nivel.getJugador().setDireccion(0, 0); }
        else if (estado == Estado.PAUSA) estado = Estado.JUGANDO;
    }
    public void soltarProducto() {
        if (estado != Estado.JUGANDO) return;
        if (nivel.getJugador().soltarProducto()) avisar("Producto en el suelo. E: recoger de nuevo.");
    }
    public void pausar() { if (estado == Estado.JUGANDO) alternarPausa(); }
    public void terminarPartida() {
        if (estado != Estado.JUGANDO && estado != Estado.PAUSA) return;
        estado = Estado.DERROTA; nivel.getJugador().setDireccion(0, 0);
        notificarEvento(TipoEvento.FIN_NIVEL, 0, "DERROTA");
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
        Producto siguiente = nivel.getSiguienteProducto();
        String orden = siguiente == null ? "" : " Sigue: " + siguiente.getCategoria() + ".";
        if (j.getProductoEnMano() != null) return "E: entregar en " + j.getProductoEnMano().getCategoria()
                + ". Q: dejar en suelo." + orden;
        Producto p = nivel.productoCercano();
        return (p == null ? "Busca productos en el suelo. WASD o flechas para moverte." : "E: recoger " + p.getNombre()) + orden;
    }
}