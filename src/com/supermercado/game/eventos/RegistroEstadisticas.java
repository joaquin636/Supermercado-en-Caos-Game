package com.supermercado.game.eventos;

import com.supermercado.api.APIClient;

public class RegistroEstadisticas implements ObservadorJuego {
    private int entregasCorrectas;
    private int entregasIncorrectas;
    private int choquesClientes;
    private int nivelesFinalizados;
    private EventoJuego ultimoEvento;
    private final APIClient apiClient; // Criterio III

    public RegistroEstadisticas() {
        this.apiClient = new APIClient();
    }

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
                // Enviamos los datos a la nube usando la API Externa al terminar el nivel
                apiClient.subirEstadisticas(evento.getNivel(), evento.getPuntuacionTotal(), entregasCorrectas, choquesClientes);
                break;
            default:
                break;
        }
    }

    public int getEntregasCorrectas() { return entregasCorrectas; }
    public int getEntregasIncorrectas() { return entregasIncorrectas; }
    public int getChoquesClientes() { return choquesClientes; }
    public int getNivelesFinalizados() { return nivelesFinalizados; }
    public EventoJuego getUltimoEvento() { return ultimoEvento; }

    public void reiniciar() {
        entregasCorrectas = 0;
        entregasIncorrectas = 0;
        choquesClientes = 0;
        nivelesFinalizados = 0;
        ultimoEvento = null;
    }
}