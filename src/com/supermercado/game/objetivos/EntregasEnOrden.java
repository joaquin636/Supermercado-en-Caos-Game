package com.supermercado.game.objetivos;

import com.supermercado.products.Producto;
import java.util.List;

/** Las categorias de la lista inicial definen la secuencia; productos iguales son intercambiables. */
public final class EntregasEnOrden implements ReglaEntregas {
    @Override
    public boolean permite(Producto producto, List<Producto> productos) {
        Producto esperado = siguiente(productos);
        return producto != null && productos.contains(producto) && !producto.isEntregado()
                && esperado != null && esperado.getCategoria().equals(producto.getCategoria());
    }

    @Override
    public Producto siguiente(List<Producto> productos) {
        int entregados = 0;
        for (Producto p : productos) if (p.isEntregado()) entregados++;
        if (entregados == productos.size()) return null;
        String categoria = productos.get(entregados).getCategoria();
        for (Producto p : productos) {
            if (!p.isEntregado() && categoria.equals(p.getCategoria())) return p;
        }
        return null;
    }
}
