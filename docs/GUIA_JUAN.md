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
