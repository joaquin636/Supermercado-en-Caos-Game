package com.supermercado.game.objetivos;

import com.supermercado.products.Producto;
import java.util.List;

public final class EntregasLibres implements ReglaEntregas {
    @Override
    public boolean permite(Producto producto, List<Producto> productos) {
        return producto != null && productos.contains(producto) && !producto.isEntregado();
    }

    @Override
    public Producto siguiente(List<Producto> productos) { return null; }
}
