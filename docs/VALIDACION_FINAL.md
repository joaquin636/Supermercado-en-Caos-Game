# Validación de la candidata final

La candidata se prepara en integracion/candidata-final. El código funcional está en c5056be, con las ramas de Rubén y Aldayer como ancestros reales. La documentación y el JAR se incorporan en un commit posterior de entrega. La base main continúa en 22ad77c.

## Autoría

La cuenta conectada es jjjmmamuel, identificador 271656499. Git ya utilizaba ese nombre y el correo privado 271656499+jjjmmamuel@users.noreply.github.com. Se aplicaron ambos valores a la configuración local del repositorio. El correo público de perfil no se utilizó.

El commit local 4d17a8c tenía a Codex como Author y Committer. Tras comprobar que ninguna rama remota publicada lo contenía, se sustituyó por 2f17311552022519a58eb15ef35cac39b1bbbc72. Sus árboles coinciden exactamente en dacac4754207058c7eff594ac8e78f4398f599dd y git diff no presenta diferencias funcionales. El mensaje no contiene coautorías ni firmas añadidas.

Los commits nuevos de la candidata usan Author y Committer jjjmmamuel con su correo privado. Los commits originales de Rubén y Aldayer conservan sus autores. Las fuentes originales de Juan están en la base integrada por la PR #2. La configuración se limita al repositorio y es compartida por sus worktrees.

## Cambios e integración

| Intervención | Rama y commit | Archivos principales | Resultado |
| --- | --- | --- | --- |
| Metadatos y guardas iniciales de State | correccion/ruben-state, 2f17311 | Juego, EstadoResultado, PruebasEstados y manifiesto | Mismo contenido de la corrección anterior y autoría personal |
| Nivel 3 y 4 originales | integracion/candidata-final, c7b2c5b | Catálogo y pruebas de Aldayer | Historia de la rama incorporada con su autoría |
| Integración y correcciones conjuntas | integracion/candidata-final, c5056be | Juego, estados, GamePanel, eventos, APIClient, catálogo, pruebas y manifiestos | State controla acciones reales, Observer conectado y seis niveles operables |
| Preparación de entrega | Commit posterior a c5056be | README, UML, informe, guía, scripts y JAR | Documentación y paquete de fuentes de producción |

Se resolvieron conflictos en Juego.java, sources.txt, tests.txt, PruebasJuego.java y CatalogoNiveles.java. Se mantuvieron los eventos de entregas, errores, choques y finalización. La suscripción del HUD ocurre en addNotify y se retira en removeNotify. El render permanece en GamePanel.

Se retiraron del seguimiento los 16 .class antiguos de build. El JAR de dist es el único binario ejecutable solicitado. El script de empaquetado utiliza un directorio nuevo y compila únicamente sources.txt.

## Compilación y suite

Entorno probado de Windows con JDK 27 y objetivo Java 17. Se compiló con --release 17, -encoding UTF-8 y -Xlint:all sin advertencias del compilador.

~~~powershell
javac --release 17 -encoding UTF-8 -Xlint:all -d build/validacion/clases @sources.txt @tests.txt
java -Djava.awt.headless=true -cp build/validacion/clases com.supermercado.PruebasJuego
~~~

| Grupo | Comprobaciones reportadas | Cobertura |
| --- | --- | --- |
| PruebasJuego | 845 | Movimiento, colisiones, diagonales, productos, estados y render |
| PruebasCampana | 201 | Configuraciones, reloj, fixtures y controles |
| PruebasJuan | 2505 | Strategy, reglas de entrega, Q y recorrido del segundo nivel |
| PruebasEstados | 27 | Permisos, comandos reales del panel, resultados y estado nulo |
| PruebasAldayer | 17 | Observer, altas, bajas, errores y reinicio |
| PruebasIntegracion | 131 | HUD dibujado y campaña de seis niveles |
| PruebasHttp | 31 | Transporte local, métricas por intento y concurrencia |
| Total de contadores publicados | 3757 | Los helpers de recorrido realizan validaciones adicionales |

Todos estos grupos pasaron. Los contadores son los reportados por cada grupo y no representan el número de métodos de prueba. Los recorridos BFS ejecutan movimiento y ticks del Juego y sus comprobaciones compartidas pueden incrementar otros contadores después de haberse impreso.

## Campaña

| Nivel | Objetivo | Tiempo inicial | Puntos acumulados | Tiempo restante del recorrido |
| --- | --- | --- | --- | --- |
| 1 | Seis entregas libres | 180 s | 600 | 165,2 s |
| 2 | Seis entregas libres | 135 s | 1200 | 93,9 s |
| 3 | Seis entregas libres con rutas cruzadas | 165 s | 1800 | 125,7 s |
| 4 | Frutas, lácteos y limpieza dos veces | 180 s | 2400 | 147,8 s |
| 5 | Cruces con cinco clientes y pausas | 155 s | 3000 | 98,1 s |
| 6 | Lácteos, frutas y limpieza dos veces | 100 s | 3600 | 62,5 s |

Las pruebas verifican acceso secuencial desde el primer nivel, puntuación acumulada, pausa en cada etapa, derrota y reintento en los niveles 3 a 6, rechazo de entrega fuera de orden y cierre único de campaña con nueva partida.

El primer recorrido aislado del sexto usó 170 s y dejó 132,5 s. Se ajustó a 100 s para reducir ese margen en la etapa final, conservando el mapa y las mecánicas. Los tiempos 155 y 100 son valores iniciales de balance. Falta una evaluación humana de dificultad.

La prueba de Juan imprime y captura el reloj del segundo nivel antes de avanzar al tercero. En ese recorrido específico quedan 101,9 s. Es una ruta distinta de la campaña de seis etapas, cuyo segundo nivel termina con 93,9 s.

## Red y estadísticas

La suite final utiliza PartidasPrueba para gameplay y un servidor HTTP de loopback para red. La etiqueta de esos lanzadores y capturas muestra Transporte simulado para pruebas. VerificarApi queda fuera de la ejecución automática y usa el endpoint externo original.

Se comprobaron 201, 503, solicitud lenta, timeout, desconexión y excepción asíncrona. El servidor local verificó un POST con Content-Type application/json y valores exactos. El EDT procesó otro turno y el juego reintentó mientras la respuesta estaba retenida.

Las entregas y choques del JSON corresponden al intento finalizado. Los puntos corresponden al acumulado válido de campaña. El registro conserva además puntosIntento, entregasIncorrectas y resultado. El comienzo del intento limpia contadores y una versión impide que respuestas de intentos anteriores reemplacen el mensaje actual. FIN_NIVEL duplicado no vuelve a enviar.

### Verificación externa ejecutada

~~~text
POST de prueba real: nivel=1, puntos=600, entregas=6, choques=0
Resumen del modelo: ResumenIntento[nivel=1, puntos=600, puntosIntento=600, entregas=6, entregasIncorrectas=0, choques=0, resultado=VICTORIA]
[APIClient] HTTP 201: peticion aceptada.
Aceptacion HTTP comprobada. JSONPlaceholder simula la creacion, sin persistencia.
~~~

Este lanzador prepara entregas mediante el modelo y verifica red. No acredita habilidad humana ni una campaña recorrida físicamente. JSONPlaceholder no conserva los recursos creados. Fuente del servicio [Guía oficial](https://jsonplaceholder.typicode.com/guide/).

Durante una primera ejecución conjunta quedaron instancias antiguas de Juego sin inyección de transporte y se emitieron peticiones de prueba a JSONPlaceholder. Se corrigieron esas instancias y se repitió la suite aislada. La demostración externa anterior se ejecutó después, explícitamente y separada de las pruebas automáticas.

## Interfaz e informe

Se abrió la ventana real de la corrección inicial y se verificaron menú, Enter, movimiento y reloj con Timer real. Las pruebas de la candidata activan acciones del panel, comprueban pausa y generan capturas de los seis niveles. La verificación del JAR empaquetado se registra al cierre.

El informe Word contiene exactamente diez secciones y un resumen de seis líneas. Se exportó con Word y se inspeccionaron las ocho páginas, tablas, UML y figura de nivel. El renderizador empaquetado carece de LibreOffice en este equipo, por lo que se utilizó Word nativo y exportación PDF/A con fuentes incrustadas para la revisión. Los PDF y PNG de esa revisión son intermediarios de auditoría.

## Rúbrica actualizada

Valoración técnica orientativa de la candidata local, sujeta a la evaluación del profesor y a la demostración del equipo.

| Criterio | Máximo | Estimación | Evidencia | Pendiente |
| --- | --- | --- | --- | --- |
| Diseño POO | 4 | 3 | Composición real, responsabilidades separadas, guardas y pruebas | Defender las entidades mutables y la coordinación que concentra Juego |
| Patrones | 5 | 5 | Builder, Strategy, Observer y State utilizados y explicados | Dominio individual en exposición |
| API externa | 3 | 2 | Cliente dedicado, POST real 201, async, errores y resumen visible | Justificar su papel de funcionalidad clave y el alcance del servicio de demostración |
| Resultados y Git | 4 | 3 | Seis niveles, JAR, fuentes, commits originales y candidata local revisable | Partida humana, revisión del equipo y publicación/integración autorizadas |
| Presentación oral | 4 | Pendiente | Guía de quince minutos para cuatro integrantes | Ensayar y demostrar dominio real |

Subtotal técnico estimado de 13 sobre 16. Los cuatro puntos de exposición se evalúan durante la defensa. No se garantiza 20 sobre 20 por disponer de estas implementaciones.

## Estado de las PR

Las PR remotas #3 y #4 continúan con sus fuentes publicadas previas. Las correcciones se prepararon en la candidata local y no modifican esas PR automáticamente. La rama de Rubén mantiene su commit original publicado. La corrección de metadatos y las integraciones nuevas son locales.

La recomendación técnica es revisar la candidata conjunta y sus diferencias con main, en lugar de fusionar secuencialmente las versiones originales sin correcciones. main y las ramas remotas conservan sus referencias anteriores. La publicación y fusión requieren autorización expresa.

## Comprobaciones del equipo

- Probar el JAR en el ordenador de exposición con JDK o JRE 17 o superior.
- Completar una campaña humana y valorar tiempos de los niveles 5 y 6.
- Verificar legibilidad a la resolución del proyector y comportamiento al cambiar de ventana.
- Ejecutar VerificarApi con conexión y mostrar la respuesta real como servicio de demostración.
- Revisar el historial con autores y comprender las correcciones asumidas por cada integrante.
- Ensayar la guía oral y responder preguntas sobre patrones, relaciones POO y asincronía.


## Cierre de verificación de entrega

Se clonó localmente el commit de entrega 018d36d49da51ce40e8ad4e6687fc0bc755b37ed en verificacion-clonada. La copia comenzó sin build ni clases precompiladas. La compilación de sources.txt y tests.txt para Java 17 terminó sin advertencias y toda la suite pasó, incluidos los seis niveles y los mismos contadores indicados anteriormente.

Se abrió el JAR de esa copia usando exclusivamente dist/SupermercadoEnCaos.jar como classpath de producción. La comprobación confirmó las seis configuraciones, ventana visible, inicio mediante Enter, desplazamiento, reloj, pausa al perder foco y rechazo de E durante la pausa. La ventana se cerró al terminar.

Las 40 clases de producción del JAR coinciden byte por byte con las obtenidas en la compilación limpia. Todas tienen versión de bytecode 61, correspondiente a Java 17. El JAR excluye las clases de pruebas y los lanzadores de demostración.

SHA256 del JAR
81c45c021ecd7163db10a3717b339328bc5d26b60ab1042864a81e7d4ad7e4a1

Las capturas nuevas de nivel 5 y final de campaña proceden de la copia limpia. La captura del final muestra explícitamente Transporte simulado para pruebas. Es evidencia de la campaña y del panel, mientras que la aceptación HTTP real se acredita con la verificación externa separada de este documento.

Recomendación de cierre. La candidata está preparada para revisión del equipo y autorización de publicación o integración. La revisión técnica conjunta pasó. Permanecen la prueba humana de dificultad, el ensayo y la evaluación oral, además de la aprobación del responsable para cualquier cambio remoto.
