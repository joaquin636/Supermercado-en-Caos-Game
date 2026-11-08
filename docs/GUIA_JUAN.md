# Cambios y explicacion de Juan

Base de trabajo: commit 3da543e. Las tareas U1, U2 y U3 amplian el proyecto existente.
No representan la implementacion de State u Observer ni de los niveles 3 a 6.

## U1 Movimiento de clientes con Strategy

Cliente es el contexto; MovimientoCliente es la interfaz de estrategia.
PatrullaRebote calcula desplazamientos continuos en las cuatro direcciones.
PatrullaConPausas alterna, por defecto, dos segundos de marcha con uno de descanso.
Ambas estrategias devuelven un Desplazamiento, un record de dos valores (dx, dy).
No cambian coordenadas ni conocen Swing. Nivel.mover sigue comprobando limites y obstaculos.

El constructor original de Cliente sigue funcionando y elige PatrullaRebote.
El constructor de cuatro argumentos permite elegir otra estrategia. Ejemplo real de uso:

```java
.cliente(() -> new Cliente(520, 285, 2, new PatrullaConPausas(2, 1)))
```

La expresion con flecha es un Supplier: la configuracion conserva una receta que produce un
cliente nuevo en cada carga. Las posiciones, direccion y contador de actualizaciones pertenecen
al cliente. Las estrategias son inmutables: compartir una no comparte el progreso de los clientes.
El tiempo activo se calcula como actualizaciones por PASO. En pausa Juego no actualiza personajes,
por lo que tampoco avanza este reloj. Reintentar construye clientes con contador cero.

Flujo: Timer de Swing -> Juego.actualizar -> Nivel.actualizarPersonajes -> Cliente.update ->
estrategia.calcular -> solicitarMovimiento -> Nivel.mover -> setPosicion si la zona esta libre.
Si mover devuelve bloqueo, Nivel invoca cambiarDireccion sobre el cliente y la siguiente solicitud
usa la direccion invertida. No se introducen mas comprobaciones instanceof para elegir estrategias.

Pruebas: cuatro direcciones, independencia entre clientes, ciclo de marcha/descanso,
colisiones y reconstruccion de entidades. Ejecutar probar.bat o bash probar.sh.

La prueba recorre pasos del modelo; no sustituye el control manual con teclado.

## U2 Entregas libres y secuencia por categorias

La base de Joaquin ya contenia entregas en orden por identidad de producto y bloqueaba la
recogida fuera de turno. Se conserva el Builder y su bandera entregasEnOrden, pero se completa
el comportamiento acordado: se puede recoger cualquiera; la entrega fuera de orden conserva el
producto y no suma ni avanza. Nivel delega en ReglaEntregas: EntregasLibres o EntregasEnOrden.

En modo ordenado las categorias de la lista inicial forman la secuencia. Ejemplo: Manzana,
Leche, Platano significa FRUTAS -> LACTEOS -> FRUTAS. Se puede entregar el Platano primero
porque pertenece a la primera categoria. La regla cuenta entregados, consulta la categoria de
esa posicion en la lista y busca un pendiente de esa categoria. No mantiene otro contador.
getSiguienteProducto permite a la HUD mostrar SIGUE FRUTAS, LACTEOS, etc. No obliga a un nombre
concreto dentro de la misma categoria. Cada entrega sigue pasando por Jugador y Estante.

Q deja el producto en la posicion libre del jugador y vacia su mano. Es la salida necesaria
para continuar tras recoger una categoria fuera de turno. Producto.soltar solo funciona si esta
recogido y no entregado; sus coordenadas pasan a ser modificables mediante esa operacion.
La configuracion original no cambia: reintentar crea productos nuevos en sus posiciones iniciales.
ControlTeclado registra Q; GamePanel la consume una sola vez por pulsacion; Juego la ignora
si no esta jugando. Si E y Q coinciden en un fotograma se prioriza Q.

Ejemplo de prueba manual con un nivel configurado con entregasEnOrden(true): recoger Leche
cuando SIGUE indica FRUTAS, ir a LACTEOS y pulsar E. Se rechaza conservando el producto. Pulsar
Q, buscar una fruta, entregarla y seguir la categoria indicada. La partida no queda bloqueada.
Las pruebas automaticas cubren ese rechazo, Q real en los Key Bindings, categorias repetidas,
intercambio de dos frutas, puntos duplicados, modo libre, pausa y reintento.

## U3 Pasillo concurrido

CatalogoNiveles mantiene el nivel 1 y sustituye la demo de dos productos por seis entregas
libres en el nivel 2. Tiempo inicial: 165 segundos. Hay dos clientes continuos y uno que
camina dos segundos y descansa uno. Estantes y cajas cambian de posicion para exigir otras rutas.
La fabrica del cliente con pausas usa el constructor nuevo; los otros usan el original.
La HUD muestra el nombre del nivel y una z sobre un cliente detenido por su estrategia.

Se accede completando el nivel 1 y pulsando Enter. Ganar suma otros 600 puntos; perder y
reintentar devuelve a 600, conservando lo ganado en el nivel anterior. Nivel 3 sigue pendiente:
no se anuncia una campaña terminada cuando solo existen dos niveles del producto.

PruebasJuan.caminar usa busqueda en anchura (BFS) para planear caminos por una rejilla de
7 unidades. Ejecuta cada tramo con setDireccion y dos llamadas reales a Juego.actualizar:
210/60 = 3.5 unidades por tick. No asigna posiciones durante ese recorrido del nivel 2.
Los clientes se actualizan al mismo tiempo y sus impactos pueden penalizar el reloj.
Esto verifica accesibilidad y que la ruta cabe en el tiempo, no el balance para jugadores humanos.
Las pruebas unitarias de entrega si colocan al jugador junto a objetos para aislar las reglas.

## Ejecutar y demostrar

1. Abrir la carpeta del repositorio en IntelliJ con JDK 17 o superior.
2. Ejecutar com.supermercado.Main o ejecutar.bat para jugar desde los fuentes.
3. Ejecutar probar.bat para compilar y ejecutar PruebasJuego, que llama a PruebasCampana y
   PruebasJuan. Las capturas quedan en build/capturas-pruebas, no en docs.
4. Para demostrar U2 sin alterar el catalogo, ejecutar PruebasJuan con --demo-orden desde
   IntelliJ, o tras probar.bat: java -cp build/classes com.supermercado.PruebasJuan --demo-orden.
   Usa la misma GameWindow mediante su constructor que recibe un Juego. Son tres productos
   de prueba: FRUTAS -> LACTEOS -> FRUTAS, no otro nivel del producto.
5. El JAR dist/SupermercadoEnCaos.jar se reconstruye para incluir las clases nuevas de esta rama.

## Mapa de archivos para estudiar el codigo

| Archivo | Que debes entender |
|---|---|
| entities/Personaje.java | Posicion y velocidad privadas; update abstracto; solicitarMovimiento solo guarda una solicitud |
| entities/Cliente.java | Constructor original delega al nuevo; referencia MovimientoCliente; update calcula, solicita y cuenta ticks |
| entities/movimiento/MovimientoCliente.java | Contrato comun y record inmutable de dos desplazamientos |
| entities/movimiento/PatrullaRebote.java | switch transforma 0,1,2,3 en derecha, abajo, izquierda, arriba |
| entities/movimiento/PatrullaConPausas.java | Constructor valida duraciones; resto del tiempo ubica la fase del ciclo; devuelve cero durante descanso |
| levels/ConfiguracionNivel.java | Builder define datos y Supplier fabrica entidades nuevas al recargar |
| levels/CatalogoNiveles.java | Seleccion de mapas, estrategias y parametros por numero de nivel |
| levels/Nivel.java | Crea objetos, valida reglas, busca objetivos y aplica colisiones por ejes |
| game/objetivos/ReglaEntregas.java | Contrato para validar entrega y consultar un pendiente esperado |
| game/objetivos/EntregasLibres.java | Acepta cualquier producto pendiente del mismo nivel |
| game/objetivos/EntregasEnOrden.java | Deriva progreso de los estados entregados y exige la categoria siguiente |
| products/Producto.java | Recogido y entregado; soltar cambia coordenadas y vuelve a disponible |
| entities/Jugador.java | Una referencia al objeto en mano; vaciarla al entregar o dejar producto |
| game/Juego.java | Valida estado, coordina interaccion y suma solo tras exito; gestiona reloj y progreso de Joaquin |
| input/ControlTeclado.java | Registra Q; consumir evita repetir la accion por mantener la tecla |
| ui/GamePanel.java | Lee Q y E; muestra siguiente categoria y rotulos; dibuja desde el modelo |
| ui/GameWindow.java | Constructor inyectable para reutilizar ventana en demostracion ordenada |
| test/com/supermercado/PruebasJuan.java | Pruebas U1-U3, ruta a velocidad real, capturas y demo de orden |

Las rutas de produccion de la tabla parten de src/com/supermercado.
extends establece herencia; implements cumple una interfaz; @Override comprueba sobrescritura.
Un record agrupa valores inmutables. Una lambda Supplier es una receta, no un objeto ya creado.
null en getSiguienteProducto significa que no hay una categoria concreta que exigir (modo libre
o nivel completado). En modo ordenado la HUD consulta la categoria del objeto retornado.

## Bibliotecas y limites

Todo el software usa Java 17: javax.swing (ventana, panel, Timer, acciones), java.awt y
java.awt.geom (dibujo y rectangulos), java.util y java.util.function (colecciones y Supplier).
Las pruebas usan javax.imageio, java.awt.image y java.nio.file para guardar capturas.
No se instala un motor ni una biblioteca externa. El movimiento y las colisiones son codigo propio.
La modificacion no acredita por si sola la rubrica final completa: faltan los niveles y patrones
de los otros integrantes y la exposicion individual. No hay prueba con teclado fisico automatizado.

## Resultado de verificacion

Compilacion con JDK 27, --release 17, UTF-8 y -Xlint:all: correcta y sin advertencias.
La suite completa pasa 845 comprobaciones generales, 201 de campaña y 2430 de Juan (3476 en total).
El recorrido del nivel 2 completa seis entregas con 137.7 segundos restantes; incluye espera inicial
para comprobar el descanso de un cliente. Es una ruta automatizada, no una partida manual de un alumno.
Se verifico tambien con el JAR como primera entrada del classpath y pruebas fuera del JAR.
El JAR y la demo ordenada iniciaron como procesos de escritorio sin errores de arranque.
Las capturas son renderizados del panel Swing, revisados visualmente; no capturas de teclado fisico.
El balance humano, las contribuciones de los compañeros y la defensa de los conceptos siguen siendo
trabajo real del equipo. Los tres commits de esta rama contienen asistencia de IA; estudiar y revisar
los cambios es necesario para explicarlos y atribuir el trabajo con honestidad.
