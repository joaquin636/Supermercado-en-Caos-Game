# Supermercado en Caos

**Supermercado en Caos** es un videojuego cenital 2D desarrollado en Java (versión 17+, Swing / Java2D) sin dependencias ni bibliotecas externas. Proyecto desarrollado para el curso de Programación Orientada a Objetos (POO) de la Universidad Peruana Cayetano Heredia.

El juego consta de un nivel completo donde el objetivo es realizar seis entregas de productos en un tiempo máximo de 180 segundos (3 minutos).

![Partida](docs/capturas/02_partida.png)

---

## 🚀 Ejecución y Compilación

1. **Ejecutar JAR:**
   * Extraer todo el ZIP.
   * Con Java 17 o superior instalado, ejecutar:
     ```bash
     java -jar dist/SupermercadoEnCaos.jar
     ```
2. **Compilar desde código fuente:**
   * **Windows:** Ejecutar `ejecutar.bat` (requiere JDK 17+).
   * **Linux / macOS:** Ejecutar `bash ejecutar.sh` (requiere JDK 17+).
   * Si el comando `java` del sistema falla, asegúrate de configurar la variable `JAVA_HOME` apuntando a tu JDK.
   * **Entorno IDE:** En IntelliJ (o similares), abrir esta carpeta raíz y ejecutar la clase principal `com.supermercado.Main`.

---

## 🎮 Controles y Dinámica de Juego

* **Navegación en Menús:** Teclas `Arriba` / `Abajo` o `W` / `S`. Presionar `Enter` para seleccionar *Jugar/Iniciar*, *Instrucciones* o *Salir*.
* **Movimiento del Personaje:** Teclas `WASD` o `Flechas direccionales`.
* **Interacción (`E`):** Presionar la tecla `E` estando cerca de un objeto para recogerlo del suelo o entregarlo en su estante.
* **Pausa (`P` / `ESC`):** Pausa o reanuda la partida. Cambiar el foco de la ventana pausa el juego automáticamente y limpia las teclas presionadas.
* **Reiniciar (`Enter` / `R`):** Permite volver a jugar inmediatamente después de una pantalla de victoria o derrota.

---

## 🎯 Objetivo y Reglas de Juego

* **Transporte:** El repositor solo puede llevar un producto a la vez.
* **Puntuación:** Cada entrega realizada en la categoría correcta otorga **+100 puntos**.
* **Obstáculos:** Las cajas y los estantes actúan como objetos sólidos que bloquean el paso.
* **Clientes:** Los clientes patrullan por la tienda. Al chocar con ellos, se aplican **-5 segundos** de penalización al reloj, otorgando **1 segundo de inmunidad temporal** tras el contacto.
* **Condición de Victoria:** Se gana al entregar correctamente los 6 productos en sus estantes antes de que expire el tiempo.
* **Condición de Derrota:** Se pierde automáticamente si el temporizador llega a 00:00.

---

## 📦 Productos y Categorías

El juego cuenta con 6 productos repartidos en 3 categorías bien definidas:

| Categoría | Productos Incluidos |
| :--- | :--- |
| **Frutas** | Manzana, Plátano |
| **Lácteos** | Leche, Yogur |
| **Limpieza** | Detergente, Jabón |

---

## 🚧 Obstáculos y Personajes

* **El Repositor (Jugador):** Personaje principal controlado por el jugador. Solo puede cargar un producto a la vez.
* **Clientes:** Personajes secundarios que patrullan la tienda de forma autónoma. Al colisionar con ellos, penalizan el tiempo restante.
* **Cajas y Estantes:** Obstáculos sólidos que bloquean el paso y delimitan los pasillos.
* *(En desarrollo para la versión final: Supervisores, derrames en el piso y carritos descontrolados)*.

---

## 🎨 Arte y Alcance Visual

* **Gráficos Originales:** Estilo pixel art original dibujado mediante Java2D (baldosas, mobiliario, productos y personajes) con una paleta limitada y coherente.
* **Efectos Visuales:** Animación de pasos, orientación vertical, flotación de productos e indicadores de parpadeo para la inmunidad.
* **Atlas de Texturas:** El archivo `atlas PNG` ubicado en la carpeta `assets` documenta las formas originales. Sin embargo, el juego drena y dibuja directamente desde código, por lo que no requiere archivos externos durante la ejecución.
* **Alcance:** No incluye audio ni niveles adicionales.

---

## 🏛️ Arquitectura y Cumplimiento de Rúbrica

* **Estructura:** El proyecto cuenta con **8 clases de dominio** y **4 clases auxiliares reales**.
* **Documentación:** Ver detalles técnicos en `docs/ARQUITECTURA.md`, `docs/UML.svg` y `docs/UML.drawio`.
* **Detalles Técnicos:** El `enum` de manejo de pantallas se utiliza exclusivamente para la selección de vistas y no se presenta como un patrón de diseño State.
* **Evidencias y Defensa:** Consultar `docs/GIT.md` y `docs/EXPOSICION.md`.

---

## 🧪 Verificación y Pruebas Automatizadas

* **Ejecutar Pruebas:** Ejecuta `probar.bat` (Windows) o `bash probar.sh` (Linux/macOS) para compilar y correr la suite de pruebas sin librerías externas.
* **Cobertura de Pruebas:** Las pruebas automáticas evalúan el movimiento, colisiones, transporte, entregas en categorías incorrectas, victoria, derrota, pausa, inmunidad, respuesta de controles y navegación hacia las instrucciones. Además, generan capturas directas del panel Swing.
* **Nota sobre la ruta automatizada:** El recorrido automático utiliza el resolvedor real de colisiones del modelo y pasos de actualización entre segmentos; no simula todas las pulsaciones físicas de un jugador humano.