# Relación entre el UML y el código

## Herencia y encapsulamiento

`Jugador extends Personaje`, `Cliente extends Personaje` y `Estante extends Obstaculo`. Los atributos del dominio son privados. Los hijos utilizan el constructor `super(...)`, getters y métodos protegidos; no necesitan acceder directamente a atributos protegidos.

En `Nivel.actualizarPersonajes()` se recorre `List<Personaje>` y se llama a `personaje.update()`. Java decide si ejecuta el comportamiento del jugador o del cliente. Este es el polimorfismo pedido en el curso.

## Composición y asociaciones

`Juego` crea un `Nivel` al iniciar. `Nivel.cargar()` crea su jugador, sus clientes, sus obstáculos/estantes y sus productos. Al reiniciar se crea un nivel nuevo. Las listas se exponen como vistas no modificables.

`Jugador.productoEnMano` referencia un producto del nivel; no crea una copia. `Estante.recibidos` también guarda las mismas referencias. El nivel conserva la propiedad de todos sus productos, entregados o no. Esto evita destruir accidentalmente productos al cambiar de mano a estante.

## Métodos auxiliares respecto del UML simplificado

El UML omite detalles para ser legible. En el código se añaden:

- Constructores y getters necesarios para inicializar y consultar datos.
- `Personaje.solicitarMovimiento(dx, dy)` y sus getters: cada personaje decide cuánto moverse. `Nivel` verifica si ese movimiento es posible.
- `Personaje.setPosicion(x,y)`: usado por el resolvedor de movimiento y las pruebas; la interfaz no debe llamarlo para mover al jugador directamente.
- `Nivel.mover()` y `posicionLibre()`: evitan atravesar obstáculos, incluso con desplazamientos grandes. Separan X/Y para deslizarse por paredes.
- `getBounds()` y `distanciaA()`: usan rectángulos de Java estándar para colisión/proximidad.
- `Jugador.setDireccion()` y `recibirImpacto()`: entrada y período de inmunidad.
- `Producto.recogido`, `x`, `y`: permiten distinguir suelo, mano y entrega. Estados válidos: suelo `(false,false)`, mano `(true,false)`, entregado `(false,true)` para `(recogido,entregado)`.
- `Estante.recibidos`: registro de productos aceptados sin duplicarlos.
- `Juego.interactuar()`, `alternarPausa()` y consultas: coordinan las acciones de la pantalla.
- `Juego.Estado`: enum provisional de flujo, no patrón State.

Se conservan las firmas principales del UML: `update()`, `recogerProducto(Producto)`, `entregarProducto(Estante)`, `cargar()`, `estaCompletado()`, `bloqueaPaso()`, `aceptaProducto(Producto)`, `recibirProducto(Producto)`, `marcarEntregado()`, `iniciarPartida()`, `actualizar()` y `terminarPartida()`.

## Actualización y teclado

El modelo usa un paso fijo de 1/60 segundo. `Timer` de Swing solicita fotogramas aproximadamente cada 16 ms; `GamePanel` acumula tiempo transcurrido y ejecuta los pasos necesarios. El retraso aceptado por fotograma se limita a 150 ms para evitar saltos enormes tras bloquearse la ventana. Todas las actualizaciones ocurren en el hilo de eventos de Swing.

`ControlTeclado` utiliza Key Bindings (`WHEN_IN_FOCUSED_WINDOW`), no un bucle bloqueante. Conserva un conjunto de teclas presionadas y otro de acciones nuevas. Por eso mantener E no ejecuta múltiples entregas. WASD y flechas pueden combinarse y la diagonal se normaliza. Al perder foco se limpian ambos conjuntos y se pausa.

La interacción se procesa antes del paso del reloj: una entrega final válida gana el nivel antes de que se descuente ese paso. En pausa y estados finales no se mueven personajes ni se descuenta tiempo. Un reinicio crea objetos nuevos y reinicia la puntuación.

## Representación visual

`GamePanel` dibuja figuras vectoriales con `Graphics2D`: no hay sprites descargados ni dependencias de archivos. La resolución lógica es 1280 × 800, con mapa de 960 × 540. La ventana escala conservando proporciones. El botón del menú es una indicación visual de la tecla Enter, no un botón de ratón; esta versión se controla con teclado.

## Próximo hito

1. Generalizar `Nivel` para admitir mapas 2 y 3 y añadir avance de nivel conservando puntos.
2. Sustituir el enum de flujo por `EstadoJuego` y clases concretas (patrón State).
3. Extraer la puntuación a un modelo observable y un HUD observador (patrón Observer).
4. Actualizar el UML con esas extensiones, probar, añadir animaciones y sonido.

El objetivo es avanzar sobre esta base, no volver a escribir el juego desde cero.
