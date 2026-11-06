# Arquitectura implementada

Juego coordina estado, reloj, puntos y un Nivel creado para cada partida.
Nivel crea y administra Jugador, Cliente, Producto, Obstaculo y Estante; resuelve movimiento y proximidad.
Personaje es abstracta. Jugador y Cliente sobrescriben update; Nivel.actualizarPersonajes llama personaje.update()
mediante la referencia de tipo Personaje. Estante hereda de Obstaculo y agrega reglas de recepcion.
Producto controla estados recogido y entregado. GamePanel dibuja, procesa entrada y ejecuta el paso fijo.
GameWindow aloja el panel; Main inicia Swing; ControlTeclado evita repetir acciones al mantener una tecla.

## Seis fundamentos

| Fundamento | Evidencia real |
|---|---|
| Asociacion | Jugador mantiene productoEnMano y entrega mediante Estante |
| Agregacion | Estante.recibidos incorpora referencias a Producto creados por Nivel; recibirProducto no los crea |
| Composicion | Juego crea su Nivel y lo reemplaza al reiniciar; Nivel crea los elementos de la partida |
| Encapsulamiento | Atributos privados y listas expuestas con Collections.unmodifiableList; transiciones mediante metodos |
| Herencia | Jugador y Cliente extienden Personaje; Estante extiende Obstaculo |
| Polimorfismo | Nivel.actualizarPersonajes invoca update de Jugador o Cliente desde Personaje |

Agregacion no significa solamente tener una lista: los productos existen antes de entrar al estante y Nivel conserva sus referencias.
Composicion expresa propiedad conceptual durante una partida; Java usa recoleccion de basura, no destruccion inmediata.
Los getters de objetos permiten interaccion del modelo; no se afirma inmutabilidad profunda.

## Presentacion

GamePanel conserva el modelo original. Dibuja en una superficie reutilizable de 640x400 y escala por vecino mas cercano.
El mapa tiene 960x540 unidades del modelo y se representa a la mitad dentro de esa superficie.
Los sprites procedurales estan en estante, producto y persona. No hay motor externo ni recursos protegidos.
Timer de Swing solicita fotogramas; el acumulador ejecuta pasos fijos de 1/60 s con limite de recuperacion de 0.15 s.
No se atribuyen patrones State, Observer ni MVC formal. Se separan modelo, entrada y presentacion por responsabilidad.
