# Arquitectura implementada

## Ejecución y presentación

Main abre GameWindow en el Event Dispatch Thread de Swing. GameWindow aloja GamePanel y notifica pérdida de foco. GamePanel mantiene una escena reutilizable de 640 por 400, dibuja los sprites procedurales y escala por vecino más cercano. La paleta, el mobiliario, las baldosas y los marcos conservan la identidad de Mercado La Esquina.

Un Timer de Swing solicita fotogramas. El acumulador del panel ejecuta pasos fijos de Personaje.PASO, 1/60 s. Un fotograma lento conserva el tiempo de simulación pendiente. El dibujo y la interpretación del menú pertenecen al panel. Las reglas de la partida pertenecen al modelo.

ControlTeclado registra teclas mantenidas y pulsaciones únicas. El panel utiliza los ejes para solicitar dirección y remite Enter, R, P/Esc, E y Q a Juego.manejarTeclado. Juego delega en el EstadoJuego activo. El dibujo de cada pantalla permanece en GamePanel.

## Modelo del juego

Juego posee un Nivel activo y un CatalogoNiveles. Coordina puntuación, reloj, avance, reintento y final de campaña. Sus métodos públicos validan el estado necesario antes de modificar el mundo.

Nivel reconstruye jugador, clientes, productos y obstáculos a partir de ConfiguracionNivel. Valida posiciones iniciales, resuelve colisiones por ejes y comprueba proximidad. Esta reconstrucción permite reintentar sin compartir objetos mutables entre intentos.

Personaje define posición, velocidad, desplazamiento solicitado y update abstracto. Jugador calcula movimiento normalizado y protección temporal. Cliente delega el desplazamiento a una estrategia. Nivel.actualizarPersonajes llama personaje.update mediante referencias Personaje y después aplica las colisiones.

Producto controla la recogida, entrega y devolución al suelo. Jugador mantiene productoEnMano y utiliza Estante para entregar. Juego suma puntos después de confirmar una entrega aceptada por la categoría y la regla del nivel.

## State y comandos

EstadoJuego define actualizar y manejarTeclado. El contexto Juego conserva la referencia al estado activo.

| Estado concreto | Actualización y acciones |
| --- | --- |
| EstadoMenu | Simulación detenida. Enter inicia una campaña mediante iniciarPartida |
| EstadoJugando | Actualiza la simulación. P/Esc pausa, E interactúa y Q suelta |
| EstadoPausa | Simulación detenida. P/Esc recupera el estado anterior |
| EstadoResultado | Enter avanza tras victoria o reintenta tras derrota. R inicia campaña |
| EstadoFinalCampana | Enter o R inicia otra campaña |

GamePanel conserva navegación del menú, instrucciones y Salir. Tras cargar un Nivel nuevo limpia pulsaciones, acumulador y buzón. El fotograma que confirma la carga conserva todo el tiempo del nivel nuevo.

EstadoResultado guarda el resultado de victoria como dato final. Juego consulta ese mismo dato para distinguir victoria y derrota. Las guardas de los métodos públicos impiden que una llamada externa entregue durante pausa, avance tras derrota o reintente una victoria.

El enum Juego.Estado es una vista de consulta para la presentación y las pruebas. El comportamiento se delega en las clases de estado. EstadoJuego carece de responsabilidades de dibujo.

## Observer y estadísticas

Juego mantiene ObservadorJuego registrados, evita añadir dos veces la misma referencia y permite retirarlos. Publica EventoJuego con tipo, nivel, delta de puntos, puntuación total y detalle.

Los observadores concretos son RegistroEstadisticas y BuzonMensajesHud. RegistroEstadisticas procesa entregas correctas, incorrectas, choques y final de intento. GamePanel registra su buzón en addNotify y lo retira y limpia en removeNotify. El aviso vigente se dibuja en dorado y cede paso a una instrucción posterior. La petición de repaint es segura desde la recepción del evento.

La finalización del intento tiene una única notificación. Cargar o reintentar inicializa los contadores del intento. Las estadísticas distinguen los datos del intento y el total acumulado del juego. La versión del intento impide que una respuesta de red antigua sobrescriba el mensaje del intento nuevo.

## APIClient

APIClient concentra URI, serialización JSON, cliente HTTP, envío, timeout y evaluación de la respuesta. RegistroEstadisticas toma un resumen antes de lanzar la petición, por lo que el POST conserva los datos del intento terminado aunque el jugador ya haya reintentado.

HttpClient.sendAsync entrega un resultado futuro. El hilo de Swing continúa procesando controles y simulación mientras espera. Los resultados de aceptación, HTTP fallido, timeout y excepción de red producen un mensaje consultable en la pantalla de resultado.

Las pruebas inyectan un cliente aislado para las partidas sintéticas y usan un servidor local para verificar el transporte real. La aplicación utiliza el endpoint configurado de JSONPlaceholder.

JSONPlaceholder simula creación y carece de persistencia permanente para esos POST. El proyecto informa aceptación de una petición de demostración y documenta este límite del proveedor. [Guía oficial](https://jsonplaceholder.typicode.com/guide/)

## Builder, Strategy y creación

ConfiguracionNivel.Builder representa la construcción incremental de una receta de nivel. construir valida número, tiempo, posición y objetivo y produce listas copiadas. Reutilizar o modificar el Builder después no altera la configuración construida. Es el patrón creacional que ya resuelve la construcción de escenarios.

Las funciones Supplier de la configuración permiten crear entidades nuevas durante cada carga y configurar clientes con estrategias distintas. Son fábricas funcionales. Su presencia se explica con ese alcance concreto, sin atribuir Factory Method de GoF a cualquier llamada de creación.

Cliente recibe MovimientoCliente por constructor. PatrullaRebote solicita marcha continua. PatrullaConPausas alterna marcha y descanso. Cada Cliente mantiene su propio contador de actualizaciones. Compartir una estrategia no comparte el reloj individual.

Nivel recibe su regla desde la configuración. EntregasLibres admite categorías en cualquier orden. EntregasEnOrden calcula la siguiente categoría usando las entregas realizadas. La misma regla sirve a los niveles 4 y 6, con secuencias de categorías distintas.

## Relaciones POO reales

| Relación | Evidencia y límite |
| --- | --- |
| Asociación | Jugador conserva productoEnMano y colabora con Estante al entregar |
| Agregación | Estante.recibidos añade productos creados por Nivel, que siguen perteneciendo al mundo |
| Composición | Nivel crea el conjunto de entidades de cada intento y Juego crea y reemplaza su Nivel |
| Herencia | Jugador y Cliente extienden Personaje. Estante extiende Obstaculo |
| Polimorfismo | El bucle llama Personaje.update y el contexto delega mediante las interfaces de Strategy, Observer y State |
| Encapsulamiento | Campos privados, resultado inmutable, copias de configuraciones y métodos de transición |
| Abstracción | Personaje y las interfaces permiten usar operaciones sin conocer cada implementación |

Una lista no demuestra agregación por sí sola. Los productos existen antes de recibirse en el estante y Nivel mantiene sus referencias. La composición describe propiedad conceptual durante una partida. Java conserva cualquier objeto mientras una referencia lo haga alcanzable.

Las listas expuestas impiden modificar su estructura. Las entidades internas siguen siendo mutables y sus getters no ofrecen inmutabilidad profunda. Personaje.setPosicion existe para el modelo y la preparación de pruebas.

## Cohesión y acoplamiento

Nivel concentra geometría y mundo. Juego concentra reglas de campaña y coordinación. GamePanel presenta, solicita comandos y ejecuta el paso fijo. Los estados deciden qué acciones corresponden a cada pantalla. Los observadores procesan consecuencias de los eventos. APIClient concentra transporte.

El modelo no necesita una ventana para probarse. Los niveles cambian por configuración y las estrategias cambian por composición. Observer permite añadir o retirar consumidores sin introducir llamadas de red o dibujo dentro de las entidades.

## Campaña

Las configuraciones 1 a 6 están en CatalogoNiveles.predeterminado. Los primeros tres niveles son libres. El cuarto alterna frutas, lácteos y limpieza. El quinto combina rutas y ciclos de patrullas con entregas libres. El sexto exige lácteos, frutas y limpieza dos veces y finaliza la campaña.

Los valores nuevos de 155 s y 100 s son ajustes iniciales sustentados por recorridos automáticos accesibles. La dificultad percibida se debe contrastar con partidas humanas. La matriz de pruebas y los resultados registrados pertenecen a docs/VALIDACION_FINAL.md.
