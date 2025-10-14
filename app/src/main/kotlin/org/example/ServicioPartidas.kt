package org.example

/**
 * Servicio centralizado para orquestar el ciclo de vida de múltiples partidas.
 *
 * - Mantiene un registro de partidas activas en memoria
 * - Permite crear una nueva partida con un jugador inicial
 * - Permite que un jugador se una a una partida existente
 *
 * Diseño: se implementa como Singleton usando `object` de Kotlin para simplificar
 * el acceso y la gestión de estado compartido en esta app de consola. En un
 * entorno más grande, se podría preferir inyectar esta dependencia (DI) para
 * facilitar testeo, reemplazos y configuraciones.
 */
object ServicioPartidas {
    // Registro de partidas activas (thread-safe)
    private val partidasActivas = java.util.concurrent.ConcurrentHashMap<String, Juego>()

    /**
     * Crea una nueva partida, agrega al jugador creador y la registra como activa.
     * Por simplicidad, la partida es de Tres en Línea (3x3) y admite hasta 2 jugadores.
     */
    @Synchronized
    fun crearPartida(jugador: Jugador): Juego {
        val idGenerado = generarIdPartida()
        var juego = Juego(
            id = idGenerado,
            tablero = Tablero(3, 3),
            maxJugadores = 2,
            tipoJuego = TipoJuego.TRES_EN_LINEA
        )
        juego = juego.agregarJugador(jugador)
        partidasActivas[idGenerado] = juego
        return juego
    }

    /**
     * Agrega un jugador a la partida indicada si existe.
     * Devuelve el juego actualizado o null si la partida no existe.
     */
    @Synchronized
    fun unirseAPartida(idPartida: String, jugador: Jugador): Juego? {
        val actual = partidasActivas[idPartida] ?: return null
        val actualizado = actual.agregarJugador(jugador)
        partidasActivas[idPartida] = actualizado
        return actualizado
    }

    /** Mantiene un registro de todas las partidas activas. */
    fun obtenerPartida(idPartida: String): Juego? = partidasActivas[idPartida]

    fun listarPartidas(): List<Juego> = partidasActivas.values.toList()

    fun finalizarPartida(idPartida: String) {
        partidasActivas.remove(idPartida)
    }

    private fun generarIdPartida(): String {
        return "PARTIDA-" + java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
    }
}

/**
 * Notas de diseño (para discusión):
 * - Singleton (este archivo):
 *   Ventajas → acceso simple, estado compartido único, cero configuración.
 *   Desventajas → acoplamiento global, más difícil de testear/mokear y reiniciar estado.
 * - Inyección de Dependencias:
 *   Ventajas → testabilidad, reemplazo por dobles, múltiples instancias configurables.
 *   Desventajas → requiere un contenedor o cableado, mayor complejidad inicial.
 */


