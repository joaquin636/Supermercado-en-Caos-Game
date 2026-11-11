# Guía técnica de exposición

La exposición utiliza la candidata identificada en VALIDACION_FINAL.md. El grupo debe ensayar con el mismo equipo, JDK y JAR que llevará a clase. Los tiempos siguientes distribuyen aproximadamente quince minutos entre los cuatro integrantes.

## Recorrido de quince minutos

| Tiempo | Responsable | Demostración y explicación |
| --- | --- | --- |
| 0:00 a 3:30 | Joaquín | Objetivo del juego, campaña, catálogo y Builder. Mostrar configuración de un escenario y las entidades independientes que crea Nivel |
| 3:30 a 7:00 | Juan | Movimiento, clientes, colisiones y Strategy. Mostrar patrulla continua y pausada, recogida, entrega incorrecta y recuperación con Q |
| 7:00 a 10:30 | Rubén | State. Pausar con un producto junto al estante, pulsar E y comprobar que conserva mano y puntos. Reanudar y entregar. Explicar victoria, derrota y reintento |
| 10:30 a 14:00 | Aldayer | Observer y estadísticas. Mostrar aviso de entrega o choque y el resumen del resultado. Explicar el POST asíncrono, timeout, error y alcance real de JSONPlaceholder |
| 14:00 a 15:00 | Equipo | Presentar la evidencia de los seis niveles, el cierre de campaña y los commits reales. Indicar lo comprobado automáticamente y lo ensayado por el grupo |

La partida completa puede exceder esos quince minutos. La exposición puede apoyar el recorrido con capturas y resultados de pruebas identificados. Las fixtures o lanzadores de demostración deben rotularse como tales. La evidencia de una campaña real debe proceder del catálogo de producto y sus resultados.

## Guion de Joaquín

«Supermercado en Caos es un juego de reposición. El jugador transporta un producto a la vez y completa objetivos mientras evita clientes y administra el tiempo. La campaña contiene seis escenarios. Conservamos Java, Swing y Java2D para mantener una implementación comprensible y reproducible con el JDK.»

«CatalogoNiveles agrupa las recetas. ConfiguracionNivel.Builder permite expresar tiempo, posición, estantes, productos, clientes y regla antes de construir una configuración validada. Nivel crea objetos nuevos a partir de esa receta. Por eso un reintento repone los productos y los personajes sin afectar otros intentos.»

Mostrar CatalogoNiveles.predeterminado, ConfiguracionNivel.Builder.construir y Nivel.cargar. Relacionar estas tres operaciones con el UML y demostrar que los niveles nuevos amplían las mecánicas existentes.

## Guion de Juan

«Personaje define update. Jugador y Cliente calculan su solicitud de movimiento de forma distinta. Nivel los recorre mediante referencias Personaje y llama update sin seleccionar manualmente el subtipo. Después aplica las colisiones.»

«Cliente recibe MovimientoCliente. PatrullaRebote camina continuamente y PatrullaConPausas alterna marcha y descanso. El contador temporal pertenece a cada cliente. Nivel sigue resolviendo las paredes para ambas estrategias.»

«Las reglas de entrega también se intercambian. En un nivel libre puedo elegir la sección. En uno ordenado necesito la categoría que indica SIGUE. Si recojo otro producto puedo dejarlo con Q. La entrega rechazada conserva el producto y los puntos.»

Mostrar Cliente.update, MovimientoCliente, ambas estrategias y ReglaEntregas. Demostrar que una colisión descuenta cinco segundos y que la protección evita descontar en cada fotograma.

## Guion de Rubén

«Juego conserva un EstadoJuego activo y delega en él actualización y comandos. La partida ejecuta la simulación. El menú, la pausa y los resultados mantienen esa simulación detenida.»

«El panel remite las acciones al estado. Mientras estoy en pausa, E y Q no alteran el mundo. EstadoResultado distingue victoria y derrota con un único dato. Enter avanza después de ganar y reconstruye el nivel después de perder. El modelo también valida estos permisos para proteger llamadas externas.»

«El enum que lee la interfaz es una consulta. La implementación de State está en las clases que determinan el comportamiento. El dibujo permanece en GamePanel para conservar el pixel art y separar las reglas de la presentación.»

Mostrar EstadoJuego, EstadoJugando, EstadoPausa y EstadoResultado. Ejecutar la prueba de E en pausa desde el panel y explicar la regresión que previene.

## Guion de Aldayer

«Juego publica un EventoJuego al entregar, equivocarse, chocar o finalizar el intento. Los observadores registrados reciben esos datos. El buzón del HUD muestra el aviso y RegistroEstadisticas actualiza las métricas.»

«GamePanel registra su observador al incorporarse a la interfaz y lo retira al salir. RegistroEstadisticas toma una instantánea del intento antes de enviarla. APIClient concentra la comunicación HTTP y devuelve un resultado asíncrono, por lo que el juego sigue respondiendo mientras espera.»

«El proveedor es JSONPlaceholder. Una respuesta de aceptación confirma el transporte de demostración, y ese proveedor no guarda los datos de forma permanente. En el resultado se informa el estado real del envío. Las pruebas controlan aceptación, error HTTP, espera agotada y desconexión mediante un servidor local.»

Mostrar Juego.agregarObservador, Juego.notificarEvento, BuzonMensajesHud, RegistroEstadisticas y APIClient. Para demostrar la API externa debe haber una petición y una respuesta verificables del servicio configurado. Los resultados del servidor local deben presentarse como pruebas de integración controladas.

## Preguntas que el equipo debe poder responder

| Pregunta | Puntos que debe contener la respuesta |
| --- | --- |
| ¿Por qué esto es State? | El contexto delega comportamientos y acciones en objetos de estado concretos. El enum solo expone una consulta |
| ¿Quién decide si se puede entregar durante pausa? | El estado activo no admite la interacción y el contrato público del modelo también protege la operación |
| ¿Qué resuelve Strategy? | Reutiliza el mismo Cliente y la misma colisión con diferentes solicitudes de movimiento. Las reglas de entrega también cambian por interfaz |
| ¿Qué comparte una estrategia entre clientes? | Su algoritmo y parámetros. La dirección y el tiempo individual permanecen en cada cliente |
| ¿Qué aporta Builder? | Construcción incremental legible, validación y configuración independiente del objeto que construye escenarios |
| ¿Toda lista es agregación? | La relación depende de propiedad y ciclo de vida. El estante recibe referencias a productos creados por Nivel |
| ¿Dónde se usa polimorfismo en ejecución? | Personaje.update, EstadoJuego.actualizar y manejarTeclado, cálculo de MovimientoCliente y notificación de ObservadorJuego |
| ¿Qué pasa si Internet tarda? | La petición continúa de forma asíncrona, con límite de espera. La interfaz sigue activa y muestra el resultado correspondiente |
| ¿Cómo se evita enviar dos veces un final? | El modelo conserva un resultado terminal y el registro protege la finalización del intento |
| ¿Qué pasa con una respuesta del intento anterior? | La identidad o versión del intento evita que sobrescriba el estado visible del actual |
| ¿El POST guarda un récord permanente? | JSONPlaceholder simula creación. La demostración debe explicar esa limitación y verificar su respuesta real |
| ¿Cómo se conserva la puntuación al perder? | Juego restaura puntuacionInicioNivel y reconstruye la receta del nivel actual |
| ¿Se probó en Java 17 instalado? | Responder con el entorno exacto de VALIDACION_FINAL.md. --release 17 acredita compatibilidad de compilación, no una ejecución en todos los JDK |
| ¿Qué hizo cada integrante? | Mostrar commits y PR reales, distinguir cambios originales y correcciones posteriores bajo la responsabilidad correspondiente |

## Evidencias de Git

Abrir el historial del repositorio y las PR. Mostrar una contribución concreta de cada integrante y su prueba. Los commits de corrección deben conservar su autor y committer reales. La autoría de una integración no reemplaza la autoría de los commits que incorpora.

Explicar la diferencia entre guardar un commit, subir una rama e integrar una PR. El estado local de candidata y su eventual aprobación deben identificarse de forma explícita durante la preparación.

## Ensayo previo

- Ejecutar la compilación y la suite desde una copia limpia.
- Abrir el JAR de la candidata y comprobar ventana, controles, foco y Salir.
- Jugar los seis niveles y revisar si los tiempos nuevos ofrecen un reto razonable.
- Ensayar la demostración de pausa y entrega incorrecta sin buscar improvisadamente el caso.
- Probar la conexión externa y conservar evidencia del resultado del servicio.
- Abrir UML y señalar relaciones y participantes de cada patrón.
- Practicar las preguntas con una persona que no haya escrito esa parte del código.
