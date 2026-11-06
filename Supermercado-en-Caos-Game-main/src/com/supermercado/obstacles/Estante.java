package com.supermercado.obstacles;

import com.supermercado.products.Producto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Estante extends Obstaculo {
    private final String categoria;
    private final List<Producto> recibidos = new ArrayList<>();

    public Estante(String categoria, double x, double y) {
        super(x, y, 180, 76);
        this.categoria = categoria;
    }

    public boolean aceptaProducto(Producto p) {
        return p != null && categoria.equals(p.getCategoria()) && !p.isEntregado();
    }

    public boolean recibirProducto(Producto p) {
        if (!aceptaProducto(p) || !p.isRecogido() || recibidos.contains(p)) return false;
        p.marcarEntregado();
        recibidos.add(p);
        return true;
    }

    public String getCategoria() { return categoria; }
    public List<Producto> getRecibidos() { return Collections.unmodifiableList(recibidos); }
}
