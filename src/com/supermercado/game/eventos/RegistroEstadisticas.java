package com.supermercado.game.eventos;

import com.supermercado.api.APIClient;
import java.util.Objects;

/** Observador del modelo. Los contadores describen el intento actual y los finales la campana. */
public class RegistroEstadisticas implements ObservadorJuego {
    public record ResumenIntento(int nivel, int puntos, int puntosIntento, int entregas,
            int entregasIncorrectas, int choques, String resultado) {}

    private int entregasCorrectas;
    private int entregasIncorrectas;
    private int choquesClientes;
    private int nivelesFinalizados;
    private int puntuacionInicioIntento;
    private EventoJuego ultimoEvento;
    private ResumenIntento ultimoResumen;
    private boolean intentoFinalizado;
    private long versionIntento;
    private volatile String mensajeEnvio = "Sin envios en este intento.";
    private final APIClient apiClient;

    public RegistroEstadisticas() { this(new APIClient()); }
    public RegistroEstadisticas(APIClient apiClient) { this.apiClient = Objects.requireNonNull(apiClient); }

    @Override public synchronized void onEvento(EventoJuego evento) {
        if (evento == null || intentoFinalizado) return;
        ultimoEvento = evento;
        switch (evento.getTipo()) {
            case ENTREGA_CORRECTA -> entregasCorrectas++;
            case ENTREGA_INCORRECTA -> entregasIncorrectas++;
            case CHOQUE_CLIENTE -> choquesClientes++;
            case FIN_NIVEL -> finalizarIntento(evento);
        }
    }

    private void finalizarIntento(EventoJuego evento) {
        intentoFinalizado = true;
        nivelesFinalizados++;
        ResumenIntento resumen = new ResumenIntento(evento.getNivel(), evento.getPuntuacionTotal(),
                evento.getPuntuacionTotal() - puntuacionInicioIntento, entregasCorrectas,
                entregasIncorrectas, choquesClientes, evento.getDetalle());
        ultimoResumen = resumen;
        long version = versionIntento;
        mensajeEnvio = "Enviando resumen...";
        apiClient.subirEstadisticasAsync(resumen.nivel(), resumen.puntos(), resumen.entregas(), resumen.choques())
                .whenComplete((aceptado, error) -> {
                    synchronized (this) {
                        // Una respuesta del intento anterior no sustituye al aviso de un intento nuevo.
                        if (version != versionIntento) return;
                        mensajeEnvio = error == null && Boolean.TRUE.equals(aceptado)
                                ? "Peticion aceptada, sin guardado permanente."
                                : "No se pudo enviar. Puedes continuar jugando.";
                    }
                });
    }

    public synchronized void iniciarIntento(int puntuacionInicial) {
        entregasCorrectas = 0;
        entregasIncorrectas = 0;
        choquesClientes = 0;
        puntuacionInicioIntento = puntuacionInicial;
        ultimoEvento = null;
        intentoFinalizado = false;
        versionIntento++;
        mensajeEnvio = "Sin envios en este intento.";
    }

    public synchronized int getEntregasCorrectas() { return entregasCorrectas; }
    public synchronized int getEntregasIncorrectas() { return entregasIncorrectas; }
    public synchronized int getChoquesClientes() { return choquesClientes; }
    public synchronized int getNivelesFinalizados() { return nivelesFinalizados; }
    public synchronized EventoJuego getUltimoEvento() { return ultimoEvento; }
    public synchronized ResumenIntento getUltimoResumen() { return ultimoResumen; }
    public String getMensajeEnvio() { return mensajeEnvio; }

    public synchronized void reiniciar() {
        iniciarIntento(0);
        nivelesFinalizados = 0;
        ultimoResumen = null;
    }
}
