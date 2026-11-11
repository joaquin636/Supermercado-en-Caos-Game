# Supermercado en Caos

Juego cenital de reposición de productos desarrollado en Java 17+, Swing y Java2D. El repositor recoge un producto, lo lleva a su sección y evita clientes mientras completa una campaña de seis niveles. Mercado La Esquina conserva su pixel art original, su paleta cálida y sus marcos dorados.

## Ejecutar

Se necesita un JDK 17 o superior para compilar desde fuentes. Si Java no está disponible en PATH, configura JAVA_HOME con la carpeta de tu JDK.

En Windows abre esta carpeta y ejecuta:

~~~powershell
.\ejecutar.bat
~~~

En Linux o macOS:

~~~bash
bash ejecutar.sh
~~~

La clase principal del IDE es com.supermercado.Main. Los scripts compilan las clases incluidas en sources.txt con --release 17 y codificación UTF-8.

El paquete ejecutable final se inicia con:

~~~bash
java -jar dist/SupermercadoEnCaos.jar
~~~

Consulta docs/VALIDACION_FINAL.md para identificar el commit, el entorno y las verificaciones correspondientes al JAR de esta candidata.

## Controles

| Acción | Tecla |
| --- | --- |
| Elegir una opción de menú | Arriba / abajo o W / S |
| Aceptar opción, avanzar tras victoria o reintentar tras derrota | Enter |
| Mover al repositor | WASD o flechas |
| Recoger o entregar cerca del objeto | E |
| Dejar el producto transportado en el suelo | Q |
| Pausar o reanudar | P o Esc |
| Iniciar otra campaña desde un resultado | R |

El menú incluye Jugar/Iniciar, Instrucciones y Salir. Al perder el foco, la ventana pausa el juego y limpia las teclas mantenidas. En pausa se conservan reloj, posiciones y producto transportado.

## Objetivo y campaña

Cada entrega aceptada suma 100 puntos. Un choque con un cliente descuenta cinco segundos y concede un segundo de protección. Las cajas y estantes bloquean el paso. El repositor transporta un producto a la vez.

Una entrega fuera de categoría o de turno conserva el producto y la puntuación. Q permite dejarlo para buscar el que corresponde. Los productos de una misma categoría son intercambiables cuando la regla exige esa categoría.

| Nivel | Nombre | Tiempo inicial | Regla y desafío |
| --- | --- | ---: | --- |
| 1 | Turno de reposición | 180 s | Seis entregas libres y patrullas horizontales |
| 2 | Pasillo concurrido | 135 s | Estantes repartidos, corredores y cuatro clientes con marcha continua o pausada |
| 3 | Rutas cruzadas | 165 s | Recorridos entre cajas y patrullas en ambos ejes |
| 4 | Reposición por secciones | 180 s | Frutas, lácteos y limpieza, dos veces en ese orden |
| 5 | Cruce de reposición | 155 s | Entregas libres entre dos corredores con cinco patrullas y ciclos de pausa distintos |
| 6 | Último turno | 100 s | Lácteos, frutas y limpieza, dos veces, con seis clientes y pasillos más estrechos |

Los tiempos de los niveles nuevos son valores iniciales de balance. Se deben contrastar con partidas humanas del equipo. El recorrido automático permite comprobar accesibilidad y reloj.

Enter conserva los puntos al avanzar. Si se pierde, el reintento reconstruye ese nivel y restaura la puntuación de entrada. Ganar el sexto finaliza la campaña. Enter o R desde ese final inicia otra campaña con cero puntos.

## Arquitectura y patrones

Juego coordina el modelo y delega actualización y comandos en EstadoJuego. GamePanel conserva presentación, menú y selección de opciones. ControlTeclado procesa pulsaciones y teclas mantenidas. Nivel crea las entidades y resuelve movimiento, colisiones y proximidad.

- Builder construye configuraciones validadas e independientes mediante ConfiguracionNivel.Builder.
- Strategy permite intercambiar MovimientoCliente y ReglaEntregas sin duplicar colisiones o puntuación.
- Observer conecta los eventos del modelo con el HUD y RegistroEstadisticas.
- State determina las acciones admitidas en menú, partida, pausa y resultados.

Los ejemplos, las relaciones UML y la defensa técnica se desarrollan en docs/ARQUITECTURA.md y docs/GUIA_EXPOSICION.md.

## Estadísticas y API

Al finalizar un intento, RegistroEstadisticas toma sus entregas, errores, choques, puntos y resultado. APIClient realiza el POST asíncrono mediante java.net.http.HttpClient. El juego permanece operativo durante la espera. El resultado del envío aparece en la pantalla de resultado y se describen errores HTTP, desconexión y timeout.

El proveedor configurado es JSONPlaceholder. Su POST simula la creación del recurso y no ofrece almacenamiento permanente de las estadísticas. El mensaje de éxito indica que la API aceptó la petición de demostración. La demostración académica debe identificar el servicio y su alcance. [Guía oficial de JSONPlaceholder](https://jsonplaceholder.typicode.com/guide/)

## Pruebas

En Windows ejecuta:

~~~powershell
.\probar.bat
~~~

En Linux o macOS:

~~~bash
bash probar.sh
~~~

La suite compila sources.txt y tests.txt y ejecuta com.supermercado.PruebasJuego. Comprueba contratos del modelo, controles Swing, pausa, resultados, entrega libre y ordenada, seis niveles y conexión entre los patrones. La red se comprueba con un servidor HTTP local controlado, con casos de aceptación, error, lentitud y desconexión. Su ejecución requiere permiso para utilizar sockets de la propia computadora.

Las capturas se generan bajo build/capturas-pruebas. Las fixtures de campaña permiten probar configuraciones pequeñas de forma aislada. El recorrido de la campaña real utiliza la dirección del jugador y actualizaciones del juego, con colisiones y reloj activos.

La prueba automatizada de interfaz ejecuta los mapas de acciones Swing. El ensayo humano del equipo debe comprobar la comodidad del control, la dificultad y la legibilidad en la pantalla de exposición.

## Recursos y documentos

La presentación dibuja el pixel art mediante Java2D en una escena de 640 por 400 y usa escalado por vecino más cercano. El atlas de assets documenta el arte original. La ejecución no necesita descargar sprites. Esta versión no incorpora audio.

- docs/ARQUITECTURA.md describe componentes, patrones y relaciones.
- docs/UML.svg contiene el diagrama de clases.
- docs/GUIA_EXPOSICION.md organiza la defensa para cuatro integrantes.
- docs/VALIDACION_FINAL.md identifica las pruebas ejecutadas, sus resultados y sus límites.
- El informe final desarrolla las diez secciones de la rúbrica.

Las contribuciones originales se conservan en el historial de ramas y sus commits. El estado de esta candidata local y su eventual integración a main debe consultarse en la documentación de validación.


## Empaquetado y verificación de red

empaquetar.bat compila las fuentes de producción en un directorio nuevo bajo build y actualiza dist/SupermercadoEnCaos.jar. En Linux o macOS se puede usar bash empaquetar.sh.

Después de compilar las pruebas, el siguiente comando verifica una petición real de demostración. Necesita conexión a Internet y se ejecuta explícitamente.

~~~text
java -cp build/classes com.supermercado.VerificarApi
~~~

Las pruebas normales usan transporte aislado y un servidor HTTP local. Las ventanas lanzadas desde PruebasJuan también usan transporte simulado y lo indican en el resultado. El juego normal ejecutado mediante Main o el JAR utiliza la API real.

El informe final vigente está en docs/Informe_Final_SupermercadoEnCaos.docx. El informe parcial se conserva como antecedente de la entrega anterior.
