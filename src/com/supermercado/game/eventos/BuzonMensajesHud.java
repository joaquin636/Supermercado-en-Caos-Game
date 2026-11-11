package com.supermercado.game.eventos;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/** Guarda avisos recientes y solicita repintado sin conservar referencias a una ventana. */
public class BuzonMensajesHud implements ObservadorJuego {
    private static final int CAPACIDAD = 32;
    private final ArrayDeque<String> historial = new ArrayDeque<>();
    private final Runnable alRecibir;
    private EventoJuego ultimo;

    public BuzonMensajesHud() { this(() -> {}); }
    public BuzonMensajesHud(Runnable alRecibir) { this.alRecibir = Objects.requireNonNull(alRecibir); }

    @Override public void onEvento(EventoJuego evento) {
        if (evento == null) return;
        synchronized (this) {
            ultimo = evento;
            if (historial.size() == CAPACIDAD) historial.removeFirst();
            historial.addLast("[" + evento.getTipo().name() + "] " + evento.getDetalle());
        }
        alRecibir.run();
    }

    public synchronized EventoJuego getUltimoEvento() { return ultimo; }
    public synchronized String getUltimoMensaje() { return historial.isEmpty() ? "" : historial.getLast(); }
    public synchronized List<String> getHistorial() { return List.copyOf(historial); }
    public synchronized void limpiar() { ultimo = null; historial.clear(); }
}
