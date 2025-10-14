package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Protocolo de mensajes entre cliente y servidor
 */

@JsonClassDiscriminator("tipo")
@Serializable
sealed class Comando {
    @Serializable
    data class CrearPartida(val jugador: Jugador) : Comando()

    @Serializable
    data class UnirseAPartida(val idPartida: String, val jugador: Jugador) : Comando()

    @Serializable
    data class RealizarMovimiento(
        val idPartida: String,
        val jugadorId: Long,
        val fila: Int,
        val columna: Int,
        val contenido: String
    ) : Comando()
}

@JsonClassDiscriminator("tipo")
@Serializable
sealed class Evento {
    @Serializable
    data class PartidaActualizada(val juego: Juego) : Evento()

    @Serializable
    data class Error(val mensaje: String, val codigo: String? = null) : Evento()
}


