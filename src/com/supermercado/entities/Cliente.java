package com.supermercado.entities;

/** Personaje secundario con patrulla sencilla: rebota al encontrar un bloqueo. */
public final class Cliente extends Personaje {
    private int direccion;

    public Cliente(double x, double y, int direccion) {
        super(x, y, 88);
        this.direccion = Math.floorMod(direccion, 4);
    }

    @Override
    public void update() {
        double distancia = getVelocidad() * PASO;
        double dx = direccion == 0 ? distancia : direccion == 2 ? -distancia : 0;
        double dy = direccion == 1 ? distancia : direccion == 3 ? -distancia : 0;
        solicitarMovimiento(dx, dy);
    }

    public void cambiarDireccion() { direccion = (direccion + 2) % 4; }
    public int getDireccion() { return direccion; }
}
