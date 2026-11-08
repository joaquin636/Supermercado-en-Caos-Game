package com.supermercado.entities.movimiento;

/** Patrulla continua. Nivel detecta bloqueos y pide al cliente invertir su direccion. */
public final class PatrullaRebote implements MovimientoCliente {
    @Override
    public Desplazamiento calcular(int direccion, double distancia, double tiempoActivo) {
        return switch (Math.floorMod(direccion, 4)) {
            case 0 -> new Desplazamiento(distancia, 0);
            case 1 -> new Desplazamiento(0, distancia);
            case 2 -> new Desplazamiento(-distancia, 0);
            default -> new Desplazamiento(0, -distancia);
        };
    }
}
