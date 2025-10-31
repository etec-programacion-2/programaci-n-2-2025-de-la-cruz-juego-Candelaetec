package org.example

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Servidor concurrente simple que maneja Comando/Evento sobre sockets, 1 mensaje por línea (JSON).
 */
fun main() {
    Logger.setLevel(Logger.Level.DEBUG) // Cambiar a INFO en producción
    Logger.info("Iniciando servidor de juego multijugador")

    val puerto = 5050
    val pool = Executors.newCachedThreadPool()
    val json = JsonConfig.default
    val server = ServerSocket(puerto)

    Logger.logServerEvent("Servidor escuchando en puerto $puerto")

    while (true) {
        val socket = server.accept()
        val clientAddress = socket.inetAddress.hostAddress
        Logger.logServerEvent("Nueva conexión aceptada", clientAddress)

        pool.submit {
            try {
                manejarCliente(socket, json)
            } catch (e: Exception) {
                Logger.logError("Error manejando cliente $clientAddress", e.message ?: "Error desconocido")
            }
        }
    }
}

private fun manejarCliente(socket: Socket, json: kotlinx.serialization.json.Json) {
    socket.use { s ->
        val clientAddress = s.inetAddress.hostAddress
        Logger.logServerEvent("Cliente conectado", clientAddress)

        val inReader = BufferedReader(InputStreamReader(s.getInputStream()))
        val outWriter = PrintWriter(s.getOutputStream(), true)

        var linea: String?
        var messageCount = 0

        try {
            while (inReader.readLine().also { linea = it } != null) {
                val texto = linea!!.trim()
                if (texto.isEmpty()) continue

                messageCount++
                Logger.logMessageSerialization("RECIBIDO", "mensaje #$messageCount", texto)

                val startTime = System.currentTimeMillis()
                try {
                    val comando = json.decodeFromString<Comando>(texto)
                    Logger.debug("Comando procesado: ${comando::class.simpleName}")

                    val evento = procesarComando(comando)
                    val respuesta = json.encodeToString<Evento>(evento)

                    Logger.logMessageSerialization("ENVIADO", "respuesta #$messageCount", respuesta)
                    outWriter.println(respuesta)

                    val processingTime = System.currentTimeMillis() - startTime
                    Logger.debug("Mensaje #$messageCount procesado en ${processingTime}ms")

                } catch (e: Exception) {
                    Logger.logError("Error procesando mensaje #$messageCount", e.message ?: "Error desconocido", clientAddress)
                    val error = Evento.Error(mensaje = e.message ?: "Error desconocido")
                    val errorResponse = json.encodeToString<Evento>(error)
                    outWriter.println(errorResponse)
                }
            }
        } catch (e: Exception) {
            Logger.logError("Error en conexión con cliente", e.message ?: "Error de conexión", clientAddress)
        }

        Logger.logServerEvent("Cliente desconectado", "$clientAddress (mensajes procesados: $messageCount)")
    }
}

private fun procesarComando(comando: Comando): Evento {
    return Logger.measureTime("procesarComando(${comando::class.simpleName})") {
        when (comando) {
            is Comando.CrearPartida -> {
                Logger.logPlayerAction("NUEVA", comando.jugador.id, comando.jugador.nombre, "creó nueva partida")
                val juego = ServicioPartidas.crearPartida(comando.jugador)
                Logger.logServerEvent("Partida creada", juego.id)
                Evento.PartidaActualizada(juego)
            }
            is Comando.UnirseAPartida -> {
                Logger.logPlayerAction(comando.idPartida, comando.jugador.id, comando.jugador.nombre, "intentó unirse a partida")
                val juego = ServicioPartidas.unirseAPartida(comando.idPartida, comando.jugador)
                if (juego == null) {
                    Logger.logError("UnirseAPartida", "Partida no encontrada", comando.idPartida)
                    return Evento.Error("Partida no encontrada")
                }
                Logger.logPlayerAction(comando.idPartida, comando.jugador.id, comando.jugador.nombre, "se unió exitosamente")
                Logger.logServerEvent("Jugador unido a partida", "${comando.jugador.nombre} -> ${juego.id}")
                Evento.PartidaActualizada(juego)
            }
            is Comando.UnirseAPartidaAuto -> {
                Logger.logPlayerAction("AUTO", comando.jugador.id, comando.jugador.nombre, "busca partida automáticamente")
                val elegible = ServicioPartidas.listarPartidas()
                    .firstOrNull { it.estado == EstadoJuego.ESPERANDO_JUGADORES && it.jugadores.size < it.maxJugadores }

                if (elegible == null) {
                    Logger.logError("UnirseAPartidaAuto", "No hay partidas disponibles", comando.jugador.nombre)
                    return Evento.Error("No hay partidas disponibles")
                }

                val juego = ServicioPartidas.unirseAPartida(elegible.id, comando.jugador)
                if (juego == null) {
                    Logger.logError("UnirseAPartidaAuto", "Error al unirse automáticamente", "${comando.jugador.nombre} -> ${elegible.id}")
                    return Evento.Error("Partida no encontrada")
                }

                Logger.logPlayerAction(elegible.id, comando.jugador.id, comando.jugador.nombre, "se unió automáticamente")
                Logger.logServerEvent("Unión automática exitosa", "${comando.jugador.nombre} -> ${juego.id}")
                Evento.PartidaActualizada(juego)
            }
            is Comando.RealizarMovimiento -> {
                Logger.logPlayerAction(comando.idPartida, comando.jugadorId, "Jugador${comando.jugadorId}",
                    "movimiento en (${comando.fila},${comando.columna}) = '${comando.contenido}'")

                val juego = ServicioPartidas.obtenerPartida(comando.idPartida)
                if (juego == null) {
                    Logger.logError("RealizarMovimiento", "Partida no encontrada", comando.idPartida)
                    return Evento.Error("Partida no encontrada")
                }

                val oldState = juego.estado
                val actualizado = try {
                    juego.realizarMovimiento(
                        jugadorId = comando.jugadorId,
                        fila = comando.fila,
                        columna = comando.columna,
                        contenido = comando.contenido
                    )
                } catch (e: Exception) {
                    Logger.logError("RealizarMovimiento", "Movimiento inválido: ${e.message}",
                        "${comando.fila},${comando.columna} en ${comando.idPartida}")
                    return Evento.Error("Movimiento inválido: ${e.message}")
                }

                ServicioPartidas.actualizarPartida(actualizado)

                if (oldState != actualizado.estado) {
                    Logger.logGameStateChange(comando.idPartida, oldState, actualizado.estado, "movimiento")
                }

                Logger.logServerEvent("Movimiento realizado", "${comando.idPartida}: (${comando.fila},${comando.columna}) = '${comando.contenido}'")
                Evento.PartidaActualizada(actualizado)
            }
        }
    }
}


