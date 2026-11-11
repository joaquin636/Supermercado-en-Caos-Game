package com.supermercado;

import com.supermercado.entities.Cliente;
import com.supermercado.entities.Jugador;
import com.supermercado.entities.Personaje;
import com.supermercado.game.Juego;
import com.supermercado.game.eventos.BuzonMensajesHud;
import com.supermercado.game.eventos.RegistroEstadisticas;
import com.supermercado.game.eventos.TipoEvento;
import com.supermercado.obstacles.Estante;
import com.supermercado.products.Producto;
import com.supermercado.obstacles.Obstaculo;

/** Pruebas automatizadas de A1 (Patron Observer: eventos, suscripcion, retirada y reinicio). */
public final class PruebasAldayer {
    private static int checks;

    private PruebasAldayer() {}

    public static void ejecutar() {
        checks = 0;
        probarDosObservadoresYRetirada();
        probarChoqueEntregaIncorrectaYReinicio();
        System.out.println("OK Aldayer (A1 Observer): " + checks + " comprobaciones.");
    }

    private static void probarDosObservadoresYRetirada() {
        Juego juego = PartidasPrueba.crear();
        RegistroEstadisticas stats = juego.getEstadisticas();
        BuzonMensajesHud buzon = new BuzonMensajesHud();
        juego.agregarObservador(buzon);
        // Evitar duplicados al suscribir dos veces el mismo observador
        juego.agregarObservador(buzon);

        juego.iniciarPartida();
        check(stats.getEntregasCorrectas() == 0, "Estadisticas inician en 0");
        check(buzon.getHistorial().isEmpty(), "Buzon inicia vacio");

        entregarPrimerProductoCorrecto(juego);

        check(stats.getEntregasCorrectas() == 1, "Observador 1 recibe entrega correcta");
        check(buzon.getHistorial().size() == 1, "Observador 2 recibe exactamente un evento sin duplicarse");
        check(stats.getUltimoEvento() != null && stats.getUltimoEvento().getTipo() == TipoEvento.ENTREGA_CORRECTA,
                "Tipo de evento ENTREGA_CORRECTA");
        check(stats.getUltimoEvento().getDeltaPuntos() == 100, "Delta de puntos reportado es 100");
        check(buzon.getUltimoMensaje().contains("+100"), "Buzon registra detalle de puntos");

        // Retirar el segundo observador y comprobar que deja de recibir eventos
        juego.retirarObservador(buzon);
        entregarPrimerProductoCorrecto(juego);

        check(stats.getEntregasCorrectas() == 2, "Observador 1 sigue recibiendo tras retirar el 2");
        check(buzon.getHistorial().size() == 1, "Observador 2 retirado ya no recibe notificaciones");
    }

    private static void probarChoqueEntregaIncorrectaYReinicio() {
        Juego juego = PartidasPrueba.crear();
        RegistroEstadisticas stats = juego.getEstadisticas();
        BuzonMensajesHud buzon = new BuzonMensajesHud();
        juego.agregarObservador(buzon);
        juego.iniciarPartida();

        // Provocar entrega en estante de categoria incorrecta
        Jugador j = juego.getNivel().getJugador();
        Producto prod = buscarProductoPendiente(juego);
        j.setPosicion(prod.getX(), prod.getY());
        juego.interactuar(); // recoge
        check(j.getProductoEnMano() == prod, "Producto recogido para prueba de error");

        Estante incorrecto = buscarEstanteIncorrecto(juego, prod);
        j.setPosicion(incorrecto.getX(), incorrecto.getY() + incorrecto.getAlto() + 4);
        juego.interactuar(); // falla entrega

        check(stats.getEntregasIncorrectas() == 1, "Registra entrega en estante incorrecto");
        check(j.getProductoEnMano() == prod, "Conserva producto tras error");

        // Provocar choque con cliente
        Cliente cliente = buscarCliente(juego);
        j.setPosicion(cliente.getX(), cliente.getY());
        juego.actualizar();
        check(stats.getChoquesClientes() == 1, "Registra evento de choque con cliente");

        // Provocar derrota y reintento sin duplicar ni arrastrar puntos del intento fallido
        juego.terminarPartida();
        check(stats.getNivelesFinalizados() == 1, "Registra evento FIN_NIVEL por derrota");
        check(juego.reintentarNivel(), "Permite reintentar nivel tras derrota");
        check(juego.getPuntuacion() == 0, "Reintento descarta puntos del intento fallido");

        // Reiniciar partida limpia estadisticas
        juego.iniciarPartida();
        check(stats.getEntregasCorrectas() == 0 && stats.getEntregasIncorrectas() == 0
                && stats.getChoquesClientes() == 0 && stats.getNivelesFinalizados() == 0,
                "iniciarPartida reinicia el registro de estadisticas");
    }

    private static void entregarPrimerProductoCorrecto(Juego juego) {
        Jugador j = juego.getNivel().getJugador();
        Producto p = buscarProductoPendiente(juego);
        Estante e = buscarEstanteCorrecto(juego, p);
        j.setPosicion(p.getX(), p.getY());
        juego.interactuar();
        j.setPosicion(e.getX(), e.getY() + e.getAlto() + 4);
        juego.interactuar();
    }

    private static Producto buscarProductoPendiente(Juego juego) {
        Producto sig = juego.getNivel().getSiguienteProducto();
        if (sig != null) return sig;
        for (Producto p : juego.getNivel().getProductos()) {
            if (!p.isEntregado() && !p.isRecogido()) return p;
        }
        throw new IllegalStateException("No hay productos disponibles");
    }

    private static Estante buscarEstanteCorrecto(Juego juego, Producto p) {
        for (Obstaculo obs : juego.getNivel().getObstaculos()) {
            if (obs instanceof Estante) {
                Estante e = (Estante) obs;
                if (e.aceptaProducto(p)) return e;
            }
        }
        throw new IllegalStateException("No hay estante para " + p.getCategoria());
    }

    private static Estante buscarEstanteIncorrecto(Juego juego, Producto p) {
        for (Obstaculo obs : juego.getNivel().getObstaculos()) {
            if (obs instanceof Estante) {
                Estante e = (Estante) obs;
                if (!e.aceptaProducto(p)) return e;
            }
        }
        throw new IllegalStateException("No hay estante incorrecto");
    }

    private static Cliente buscarCliente(Juego juego) {
        for (Personaje p : juego.getNivel().getPersonajes()) {
            if (p instanceof Cliente) return (Cliente) p;
        }
        throw new IllegalStateException("No hay clientes en el nivel");
    }

    private static void check(boolean condicion, String detalle) {
        if (!condicion) {
            throw new AssertionError("Fallo en PruebasAldayer: " + detalle);
        }
        checks++;
    }
}
