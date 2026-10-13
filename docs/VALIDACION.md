# Validación de esta entrega

- Compilación: Java 17, UTF-8, `--release 17`, `-Xlint:all`; sin errores ni advertencias.
- También se ejecutaron las pruebas contra el JAR que acompaña la entrega, no solo contra los archivos de desarrollo.
- Resultado: **833 comprobaciones correctas**. Incluye verificaciones repetidas durante el recorrido, no 833 casos independientes.
- Recorrido automatizado: **6/6 productos, 600 puntos y 150,8 segundos restantes**, utilizando el mapa y el resolvedor de colisiones del juego.
- Se comprobaron movimiento, diagonal, límites, obstáculos, patrulla, penalización, inmunidad, recogida, categoría equivocada, entrega duplicada, pausa, reinicio y derrota.
- Se ejecutaron acciones de Key Bindings de Swing para presionar, mantener y soltar teclas; se comprobó pausa y limpieza de entrada al perder foco.
- Se renderizaron menú, partida, producto en mano y pausa a PNG. Se revisaron visualmente menú y partida.
- Scripts Bash: sintaxis comprobada. Los scripts de Windows y la apertura de IntelliJ se prepararon, pero no se ejecutaron en Windows en este entorno.
- Limitación: no hubo prueba con ventana nativa ni teclado físico. El render y las acciones Swing se probaron sin pantalla.

El 60% representa el alcance acordado para este hito; no significa que el 60% de todas las posibles pruebas del juego final esté aprobado. Véase la distribución y los pendientes en README.md.
