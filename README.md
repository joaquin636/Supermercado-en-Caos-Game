# 🛒 Supermercado en Caos

## Descripción

**Supermercado en Caos** es un videojuego 2D desarrollado como proyecto final
del curso de Programación Orientada a Objetos.

El jugador asumirá el papel de un trabajador de supermercado que deberá
organizar productos que se encuentran fuera de lugar antes de que termine
el tiempo.

Durante la partida tendrá que recorrer diferentes zonas del supermercado,
recoger productos, colocarlos en los estantes correspondientes y evitar
obstáculos como carritos, clientes, cajas abandonadas y derrames.

A medida que se avanza de nivel, aumenta la cantidad de productos,
los obstáculos y la dificultad.

---

##  Objetivo del juego

El objetivo principal es organizar la mayor cantidad de productos posible
antes de que el tiempo llegue a cero.

El jugador obtiene puntos por colocar correctamente los productos y puede
perder puntos o tiempo al cometer errores o colisionar con determinados
obstáculos.

---

##  Mecánicas principales

- Movimiento del jugador por el supermercado.
- Recolección de productos.
- Identificación del estante correcto.
- Colocación de productos.
- Sistema de puntuación.
- Temporizador.
- Colisiones con obstáculos.
- Personajes secundarios.
- Diferentes niveles de dificultad.
- Pantalla de pausa.
- Pantalla de Game Over.
- Pantalla de victoria.

---

##  Personaje principal

El personaje principal será un trabajador del supermercado.

### Acciones

- Caminar.
- Recoger productos.
- Transportar productos.
- Colocar productos en estantes.
- Evitar obstáculos.

---

##  Personajes secundarios

El supermercado contará con diferentes personajes secundarios.

### Cliente

Camina por los pasillos y puede convertirse en un obstáculo para el jugador.

### Trabajador

Puede desplazarse dentro del supermercado realizando diferentes acciones.

### Supervisor

Puede aparecer en determinados niveles y asignar objetivos o indicar
el progreso del jugador.

---

##  Productos

Los productos estarán divididos por categorías.

Ejemplos:

| Categoría | Productos |
|---|---|
| Bebidas | Agua, gaseosa, jugo |
| Frutas | Manzana, plátano, naranja |
| Limpieza | Detergente, jabón |
| Snacks | Galletas, papas |
| Lácteos | Leche, yogurt |

Cada producto tendrá asociado un estante específico.

El jugador deberá identificar dónde corresponde colocar cada producto.

---

##  Obstáculos

Durante la partida pueden aparecer diferentes obstáculos:

- Carritos de supermercado.
- Clientes caminando.
- Cajas en los pasillos.
- Productos caídos.
- Derrames.
- Obstáculos móviles.

Las colisiones pueden provocar una reducción de puntos o una penalización
de tiempo.

---

#  Niveles

## Nivel 1 — Primer día

Introducción a las mecánicas.

- Pocos productos.
- Pocos obstáculos.
- Clientes lentos.
- Tiempo amplio.

## Nivel 2 — Hora punta

La cantidad de clientes aumenta.

- Más productos.
- Más obstáculos.
- Clientes más rápidos.
- Menor tiempo disponible.

## Nivel 3 — Supermercado en Caos

Máxima dificultad.

- Muchos productos fuera de lugar.
- Gran cantidad de clientes.
- Obstáculos móviles.
- Menor tiempo.
- Mayor puntuación necesaria para ganar.

---

# 🏆 Sistema de puntuación

Ejemplo inicial:

| Acción | Puntos |
|---|---:|
| Producto colocado correctamente | +100 |
| Producto incorrecto | -25 |
| Chocar con obstáculo | -10 |
| Completar el nivel | +500 |
| Tiempo restante | Bonificación |

La puntuación final puede utilizarse para crear una tabla de récords.

---

# ⏱️ Sistema de tiempo

Cada nivel tendrá un tiempo máximo.

Ejemplo:

Nivel 1 → 120 segundos  
Nivel 2 → 90 segundos  
Nivel 3 → 60 segundos  

Cuando el tiempo llegue a cero se evaluará si el jugador consiguió
el objetivo mínimo del nivel.

---

#  Programación Orientada a Objetos

El proyecto utilizará los principales conceptos de POO.

## Encapsulamiento

Los atributos importantes de las clases serán privados y se accederá
a ellos mediante métodos controlados.
