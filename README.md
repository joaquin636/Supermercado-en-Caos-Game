# Supermercado en Caos

**Supermercado en Caos** es un videojuego cenital 2D desarrollado en Java (versión 17+, Swing / Java2D) sin dependencias ni bibliotecas externas[cite: 2]. Proyecto desarrollado para el curso de Programación Orientada a Objetos (POO) de la Universidad Peruana Cayetano Heredia[cite: 2].

El juego consta de un nivel completo donde el objetivo es realizar seis entregas de productos en un tiempo máximo de 180 segundos (3 minutos)[cite: 2].

![Partida](docs/capturas/02_partida.png)

---

## 🚀 Ejecución y Compilación

1. **Ejecutar JAR:**
   * Extraer todo el ZIP[cite: 2].
   * Con Java 17 o superior instalado, ejecutar:
     ```bash
     java -jar dist/SupermercadoEnCaos.jar
     ```
2. **Compilar desde código fuente:**
   * **Windows:** Ejecutar `ejecutar.bat` (requiere JDK 17+)[cite: 2].
   * **Linux / macOS:** Ejecutar `bash ejecutar.sh` (requiere JDK 17+)[cite: 2].
   * Si el comando `java` del sistema falla, asegúrate de configurar la variable `JAVA_HOME` apuntando a tu JDK[cite: 2].
   * **Entorno IDE:** En IntelliJ (o similares), abrir esta carpeta raíz y ejecutar la clase principal `com.supermercado.Main`[cite: 2].

---

## 🎮 Controles y Dinámica de Juego

* **Navegación en Menús:** Teclas `Arriba` / `Abajo` o `W` / `S`. Presionar `Enter` para seleccionar *Jugar/Iniciar*, *Instrucciones* o *Salir*[cite: 2].
* **Movimiento del Personaje:** Teclas `WASD` o `Flechas direccionales`[cite: 2].
* **Interacción (`E`):** Presionar la tecla `E` estando cerca de un objeto para recogerlo del suelo o entregarlo en su estante[cite: 2].
* **Pausa (`P` / `ESC`):** Pausa o reanuda la partida[cite: 2]. Cambiar el foco de la ventana pausa el juego automáticamente y limpia las teclas presionadas[cite: 2].
* **Reiniciar (`Enter` / `R`):** Permite volver a jugar inmediatamente después de una pantalla de victoria o derrota[cite: 2].

---

## 🎯 Objetivo y Reglas de Juego

* **Transporte:** El repositor solo puede llevar un producto a la vez[cite: 2].
* **Puntuación:** Cada entrega realizada en la categoría correcta otorga **+100 puntos**[cite: 2].
* **Obstáculos:** Las cajas y los estantes actúan como objetos sólidos que bloquean el paso[cite: 2].
* **Clientes:** Los clientes patrullan por la tienda[cite: 2]. Al chocar con ellos, se aplican **-5 segundos** de penalización al reloj, otorgando **1 segundo de inmunidad temporal** tras el contacto[cite: 2].
* **Condición de Victoria:** Se gana al entregar correctamente los 6 productos en sus estantes antes de que expire el tiempo[cite: 2].
* **Condición de Derrota:** Se pierde automáticamente si el temporizador llega a 00:00[cite: 2].

---

## 📦 Productos y Categorías

El juego cuenta con 6 productos repartidos en 3 categorías bien definidas[cite: 2]:

| Categoría | Productos Incluidos |
| :--- | :--- |
| **Frutas** | Manzana, Plátano |
| **Lácteos** | Leche, Yogur |
| **Limpieza** | Detergente, Jabón |

---

## 🚧 Obstáculos y Personajes

* **El Repositor (Jugador):** Personaje principal controlado por el jugador. Solo puede cargar un producto a la vez[cite: 2].
* **Clientes:** Personajes secundarios que patrullan la tienda de forma autónoma. Al colisionar con ellos, penalizan el tiempo restante[cite: 2].
* **Cajas y Estantes:** Obstáculos sólidos que bloquean el paso y delimitan los pasillos[cite: 2].
* *(En desarrollo para la versión final: Supervisores, derrames en el piso y carritos descontrolados)*[cite: 2].

---

## 🎨 Arte y Alcance Visual

* **Gráficos Originales:** Estilo pixel art original dibujado mediante Java2D (baldosas, mobiliario, productos y personajes) con una paleta limitada y coherente[cite: 1, 2].
* **Efectos Visuales:** Animación de pasos, orientación vertical, flotación de productos e indicadores de parpadeo para la inmunidad[cite: 1, 2].
* **Atlas de Texturas:** El archivo `atlas PNG` ubicado en la carpeta `assets` documenta las formas originales[cite: 2]. Sin embargo, el juego drena y dibuja directamente desde código, por lo que no requiere archivos externos durante la ejecución[cite: 2].
* **Alcance:** No incluye audio ni niveles adicionales[cite: 1, 2].

---

## 🏛️ Arquitectura y Cumplimiento de Rúbrica

* **Estructura:** El proyecto cuenta con **8 clases de dominio** y **4 clases auxiliares reales**[cite: 1, 2].
* **Documentación:** Ver detalles técnicos en `docs/ARQUITECTURA.md`, `docs/UML.svg` y `docs/UML.drawio`[cite: 2].
* **Detalles Técnicos:** El `enum` de manejo de pantallas se utiliza exclusivamente para la selección de vistas y no se presenta como un patrón de diseño State[cite: 1, 2].
* **Evidencias y Defensa:** Consultar `docs/GIT.md` y `docs/EXPOSICION.md`[cite: 2].

---

## 🧪 Verificación y Pruebas Automatizadas

* **Ejecutar Pruebas:** Ejecuta `probar.bat` (Windows) o `bash probar.sh` (Linux/macOS) para compilar y correr la suite de pruebas sin librerías externas[cite: 2].
* **Cobertura de Pruebas:** Las pruebas automáticas evalúan el movimiento, colisiones, transporte, entregas en categorías incorrectas, victoria, derrota, pausa, inmunidad, respuesta de controles y navegación hacia las instrucciones[cite: 2]. Además, generan capturas directas del panel Swing[cite: 1, 2].
* **Nota sobre la ruta automatizada:** El recorrido automático utiliza el resolvedor real de colisiones del modelo y pasos de actualización entre segmentos; no simula todas las pulsaciones físicas de un jugador humano[cite: 1, 2].