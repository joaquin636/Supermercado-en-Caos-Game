package com.supermercado.entities.movimiento;

/** Strategy: calcula una solicitud; nunca modifica posiciones ni resuelve colisiones. */
public interface MovimientoCliente {
    record Desplazamiento(double dx, double dy) { }

    Desplazamiento calcular(int direccion, double distancia, double tiempoActivo);
}
