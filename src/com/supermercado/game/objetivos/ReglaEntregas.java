package com.supermercado.game.objetivos;

import com.supermercado.products.Producto;
import java.util.List;

/** Regla del nivel. El progreso se obtiene de los productos, sin contadores duplicados. */
public interface ReglaEntregas {
    boolean permite(Producto producto, List<Producto> productos);
    Producto siguiente(List<Producto> productos);
}
