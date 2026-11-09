package com.supermercado.game.eventos;

public class RegistroEstadisticas implements ObservadorJuego {
    private int entregasCorrectas;
    private int entregasIncorrectas;
    private int choquesClientes;
    private int nivelesFinalizados;
    private EventoJuego ultimoEvento;

    @Override
    public void onEvento(EventoJuego evento) {
        if (evento == null) {
            return;
        }
        ultimoEvento = evento;
        switch (evento.getTipo()) {
            case ENTREGA_CORRECTA:
                entregasCorrectas++;
                break;
            case ENTREGA_INCORRECTA:
                entregasIncorrectas++;
                break;
            case CHOQUE_CLIENTE:
                choquesClientes++;
                break;
            case FIN_NIVEL:
                nivelesFinalizados++;
                break;
            default:
                break;
        }
    }

    public int getEntregasCorrectas() {
        return entregasCorrectas;
    }

    public int getEntregasIncorrectas() {
        return entregasIncorrectas;
    }

    public int getChoquesClientes() {
        return choquesClientes;
    }

    public int getNivelesFinalizados() {
        return nivelesFinalizados;
    }

    public EventoJuego getUltimoEvento() {
        return ultimoEvento;
    }

    public void reiniciar() {
        entregasCorrectas = 0;
        entregasIncorrectas = 0;
        choquesClientes = 0;
        nivelesFinalizados = 0;
        ultimoEvento = null;
    }
}