package com.supermercado.entities;

import com.supermercado.entities.movimiento.MovimientoCliente;
import com.supermercado.entities.movimiento.PatrullaRebote;
import java.util.Objects;

/** Contexto de Strategy: conserva datos individuales y delega el calculo del movimiento. */
public final class Cliente extends Personaje {
    private int direccion;
    private final MovimientoCliente movimiento;
    private long actualizaciones;

    public Cliente(double x, double y, int direccion) {
        this(x, y, direccion, new PatrullaRebote());
    }

    public Cliente(double x, double y, int direccion, MovimientoCliente movimiento) {
        super(x, y, 88);
        this.direccion = Math.floorMod(direccion, 4);
        this.movimiento = Objects.requireNonNull(movimiento);
    }

    @Override
    public void update() {
        MovimientoCliente.Desplazamiento paso = movimiento.calcular(
                direccion, getVelocidad() * PASO, actualizaciones * PASO);
        solicitarMovimiento(paso.dx(), paso.dy());
        actualizaciones++;
    }

    public void cambiarDireccion() { direccion = (direccion + 2) % 4; }
    public int getDireccion() { return direccion; }
    public MovimientoCliente getMovimiento() { return movimiento; }
    public boolean estaDescansando() {
        return actualizaciones > 0 && getDesplazamientoX() == 0 && getDesplazamientoY() == 0;
    }
}
