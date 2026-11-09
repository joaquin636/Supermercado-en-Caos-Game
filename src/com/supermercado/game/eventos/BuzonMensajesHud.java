package com.supermercado.game.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Segundo observador concreto: almacena los avisos de eventos para la HUD. */
public class BuzonMensajesHud implements ObservadorJuego {
    private final List<String> historial = new ArrayList<>();
    private String ultimoMensaje = "";

    @Override
    public void onEvento(EventoJuego evento) {
        if (evento == null) {
            return;
        }
        ultimoMensaje = "[" + evento.getTipo().name() + "] " + evento.getDetalle();
        historial.add(ultimoMensaje);
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public List<String> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public void limpiar() {
        historial.clear();
        ultimoMensaje = "";
    }
}