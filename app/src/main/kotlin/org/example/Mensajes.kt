package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Protocolo de mensajes entre cliente y servidor
 * Implementa polimorfismo con sealed classes para diferentes tipos de comandos y eventos
 */

@JsonClassDiscriminator("tipo")
@Serializable
sealed class Comando {

    /**
     * Ejecuta el comando en el contexto del servicio de partidas
     * Implementa polimorfismo: cada subclase define su propia lógica de ejecución
     */
    abstract fun ejecutar(servicioPartidas: ServicioPartidas): Evento

    @Serializable
    data class CrearPartida(val jugador: Jugador) : Comando() {
        override fun ejecutar(servicioPartidas: ServicioPartidas): Evento {
            val juego = servicioPartidas.crearPartida(jugador)
            return Evento.PartidaActualizada(juego)
        }
    }

    @Serializable
    data class UnirseAPartida(val idPartida: String, val jugador: Jugador) : Comando() {
        override fun ejecutar(servicioPartidas: ServicioPartidas): Evento {
            val juego = servicioPartidas.unirseAPartida(idPartida, jugador)
            return if (juego != null) {
                Evento.PartidaActualizada(juego)
            } else {
                Evento.Error("Partida no encontrada")
            }
        }
    }

    @Serializable
    data class UnirseAPartidaAuto(val jugador: Jugador) : Comando() {
        override fun ejecutar(servicioPartidas: ServicioPartidas): Evento {
            val elegible = servicioPartidas.listarPartidas()
                .firstOrNull { it.estado == EstadoJuego.ESPERANDO_JUGADORES && it.jugadores.size < it.maxJugadores }

            if (elegible == null) {
                return Evento.Error("No hay partidas disponibles")
            }

            val juego = servicioPartidas.unirseAPartida(elegible.id, jugador)
            return if (juego != null) {
                Evento.PartidaActualizada(juego)
            } else {
                Evento.Error("Partida no encontrada")
            }
        }
    }

    @Serializable
    data class RealizarMovimiento(
        val idPartida: String,
        val jugadorId: Long,
        val fila: Int,
        val columna: Int,
        val contenido: String
    ) : Comando() {
        override fun ejecutar(servicioPartidas: ServicioPartidas): Evento {
            val juego = servicioPartidas.obtenerPartida(idPartida)
            if (juego == null) {
                return Evento.Error("Partida no encontrada")
            }

            return try {
                val actualizado = juego.realizarMovimiento(
                    jugadorId = jugadorId,
                    fila = fila,
                    columna = columna,
                    contenido = contenido
                )
                servicioPartidas.actualizarPartida(actualizado)
                Evento.PartidaActualizada(actualizado)
            } catch (e: Exception) {
                Evento.Error("Movimiento inválido: ${e.message}")
            }
        }
    }
}

@JsonClassDiscriminator("tipo")
@Serializable
sealed class Evento {

    /**
     * Procesa el evento en el cliente
     * Implementa polimorfismo: cada subclase define cómo manejar el evento
     */
    abstract fun procesarEnCliente(cliente: ClienteBase)

    @Serializable
    data class PartidaActualizada(val juego: Juego) : Evento() {
        override fun procesarEnCliente(cliente: ClienteBase) {
            cliente.juegoActual = juego
        }
    }

    @Serializable
    data class Error(val mensaje: String, val codigo: String? = null) : Evento() {
        override fun procesarEnCliente(cliente: ClienteBase) {
            cliente.mostrarError(mensaje)
        }
    }
}

/**
 * Clase base abstracta para clientes
 * Implementa herencia para compartir funcionalidad común entre diferentes tipos de clientes
 */
abstract class ClienteBase {

    internal open var juegoActual: Juego? = null

    internal open var jugadorActual: Jugador? = null

    /**
     * Método abstracto que cada cliente implementa de manera diferente
     * Implementa polimorfismo: comportamiento específico por tipo de cliente
     */
    abstract fun mostrarError(mensaje: String)

    /**
     * Método común para actualizar el estado del juego
     * Implementa encapsulamiento: lógica compartida protegida
     */
    protected open fun actualizarEstadoJuego(juego: Juego) {
        this.juegoActual = juego
    }

    /**
     * Método común para obtener el estado actual
     */
    fun obtenerEstadoActual(): Pair<Juego?, Jugador?> {
        return Pair(juegoActual, jugadorActual)
    }
}


