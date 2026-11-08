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
