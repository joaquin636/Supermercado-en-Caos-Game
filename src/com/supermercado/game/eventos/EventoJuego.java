package com.supermercado.game.eventos;

public final class EventoJuego {
    private final TipoEvento tipo;
    private final int nivel;
    private final int deltaPuntos;
    private final int puntuacionTotal;
    private final String detalle;

    public EventoJuego(TipoEvento tipo, int nivel, int deltaPuntos, int puntuacionTotal, String detalle) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de evento no puede ser nulo");
        }
        this.tipo = tipo;
        this.nivel = nivel;
        this.deltaPuntos = deltaPuntos;
        this.puntuacionTotal = puntuacionTotal;
        this.detalle = detalle == null ? "" : detalle;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public int getNivel() {
        return nivel;
    }

    public int getDeltaPuntos() {
        return deltaPuntos;
    }

    public int getPuntuacionTotal() {
        return puntuacionTotal;
    }

    public String getDetalle() {
        return detalle;
    }
}