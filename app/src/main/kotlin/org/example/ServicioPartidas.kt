package org.example

/**
 * Servicio centralizado para orquestar el ciclo de vida de múltiples partidas.
 * Implementa el patrón Singleton y principios SOLID.
 *
 * Principios SOLID aplicados:
 * - S (Single Responsibility): Solo gestiona el ciclo de vida de partidas
 * - O (Open-Closed): Extensible para nuevos tipos de juegos sin modificar el código existente
 * - L (Liskov Substitution): Compatible con cualquier implementación de Juego
 * - I (Interface Segregation): Métodos específicos para cada operación
 * - D (Dependency Inversion): No depende de implementaciones concretas
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

    // Registro de partidas activas (encapsulado, thread-safe)
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

    /**
     * Obtiene una partida por ID (encapsulado)
     */
    fun obtenerPartida(idPartida: String): Juego? = partidasActivas[idPartida]

    /**
     * Lista todas las partidas activas (encapsulado)
     */
    fun listarPartidas(): List<Juego> = partidasActivas.values.toList()

    /**
     * Finaliza una partida (encapsulado)
     */
    fun finalizarPartida(idPartida: String) {
        partidasActivas.remove(idPartida)
    }

    /**
     * Actualiza el estado de una partida activa (encapsulado)
     */
    fun actualizarPartida(juego: Juego) {
        partidasActivas[juego.id] = juego
    }

    /**
     * Genera un ID único para una nueva partida (privado, encapsulado)
     */
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


