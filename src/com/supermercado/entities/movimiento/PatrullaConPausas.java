package com.supermercado.entities.movimiento;

/** Alterna marcha y descanso. Es inmutable: el reloj individual pertenece al Cliente. */
public final class PatrullaConPausas implements MovimientoCliente {
    private final double segundosMarcha;
    private final double segundosPausa;
    private final PatrullaRebote patrulla = new PatrullaRebote();

    public PatrullaConPausas() { this(2, 1); }

    public PatrullaConPausas(double segundosMarcha, double segundosPausa) {
        if (!Double.isFinite(segundosMarcha) || !Double.isFinite(segundosPausa)
                || segundosMarcha <= 0 || segundosPausa <= 0
                || !Double.isFinite(segundosMarcha + segundosPausa)) {
            throw new IllegalArgumentException("Marcha y pausa deben ser duraciones positivas y finitas.");
        }
        this.segundosMarcha = segundosMarcha;
        this.segundosPausa = segundosPausa;
    }

    @Override
    public Desplazamiento calcular(int direccion, double distancia, double tiempoActivo) {
        double fase = tiempoActivo % (segundosMarcha + segundosPausa);
        if (fase >= segundosMarcha) return new Desplazamiento(0, 0);
        return patrulla.calcular(direccion, distancia, tiempoActivo);
    }
}
