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
    val puerto = 5050
    val pool = Executors.newCachedThreadPool()
    val json = JsonConfig.default
    val server = ServerSocket(puerto)
    println("[Servidor] Escuchando en puerto $puerto...")

    while (true) {
        val socket = server.accept()
        pool.submit { manejarCliente(socket, json) }
    }
}

private fun manejarCliente(socket: Socket, json: kotlinx.serialization.json.Json) {
    socket.use { s ->
        val inReader = BufferedReader(InputStreamReader(s.getInputStream()))
        val outWriter = PrintWriter(s.getOutputStream(), true)
        println("[Servidor] Cliente conectado: ${s.inetAddress.hostAddress}")

        var linea: String?
        while (inReader.readLine().also { linea = it } != null) {
            val texto = linea!!.trim()
            if (texto.isEmpty()) continue
            try {
                val comando = json.decodeFromString<Comando>(texto)
                val evento = procesarComando(comando)
                val respuesta = json.encodeToString<Evento>(evento)
                outWriter.println(respuesta)
            } catch (e: Exception) {
                val error = Evento.Error(mensaje = e.message ?: "Error desconocido")
                outWriter.println(json.encodeToString<Evento>(error))
            }
        }
        println("[Servidor] Cliente desconectado")
    }
}

private fun procesarComando(comando: Comando): Evento {
    return when (comando) {
        is Comando.CrearPartida -> {
            val juego = ServicioPartidas.crearPartida(comando.jugador)
            Evento.PartidaActualizada(juego)
        }
        is Comando.UnirseAPartida -> {
            val juego = ServicioPartidas.unirseAPartida(comando.idPartida, comando.jugador)
                ?: return Evento.Error("Partida no encontrada")
            Evento.PartidaActualizada(juego)
        }
        is Comando.UnirseAPartidaAuto -> {
            val elegible = ServicioPartidas.listarPartidas()
                .firstOrNull { it.estado == EstadoJuego.ESPERANDO_JUGADORES && it.jugadores.size < it.maxJugadores }
                ?: return Evento.Error("No hay partidas disponibles")
            val juego = ServicioPartidas.unirseAPartida(elegible.id, comando.jugador)
                ?: return Evento.Error("Partida no encontrada")
            Evento.PartidaActualizada(juego)
        }
        is Comando.RealizarMovimiento -> {
            val juego = ServicioPartidas.obtenerPartida(comando.idPartida)
                ?: return Evento.Error("Partida no encontrada")
            val actualizado = try {
                juego.realizarMovimiento(
                    jugadorId = comando.jugadorId,
                    fila = comando.fila,
                    columna = comando.columna,
                    contenido = comando.contenido
                )
            } catch (e: Exception) {
                return Evento.Error("Movimiento inválido: ${e.message}")
            }
            ServicioPartidas.actualizarPartida(actualizado)
            Evento.PartidaActualizada(actualizado)
        }
    }
}


