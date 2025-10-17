package org.example

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Cliente de consola: conecta, envía un Comando (create|join|move), y muestra Eventos recibidos.
 * Argumentos:
 *   --cmd=[create|join|joinAuto|move]
 *   --host=127.0.0.1  --port=5050
 *   --name=Cliente    --playerId=1001
 *   --id=PARTIDA-XXXX (para join/move)
 *   --fila=0 --columna=0 --contenido=X (para move)
 */
fun main(args: Array<String>) {
    val opts = args.toOptions()
    val host = opts["host"] ?: "127.0.0.1"
    val puerto = (opts["port"] ?: "5050").toIntOrNull() ?: 5050
    val cmdName = (opts["cmd"] ?: "create").lowercase()
    val json = JsonConfig.default

    val comando: Comando = when (cmdName) {
        "create" -> {
            val nombre = opts["name"] ?: "Cliente"
            val playerId = (opts["playerId"] ?: "1001").toLongOrNull() ?: 1001L
            Comando.CrearPartida(Jugador(id = playerId, nombre = nombre))
        }
        "join" -> {
            val id = opts["id"] ?: return usage("Falta --id para join")
            val nombre = opts["name"] ?: "Cliente"
            val playerId = (opts["playerId"] ?: "1002").toLongOrNull() ?: 1002L
            Comando.UnirseAPartida(idPartida = id, jugador = Jugador(id = playerId, nombre = nombre))
        }
        "joinauto", "joinauto" -> {
            val nombre = opts["name"] ?: "Cliente"
            val playerId = (opts["playerId"] ?: "1002").toLongOrNull() ?: 1002L
            Comando.UnirseAPartidaAuto(jugador = Jugador(id = playerId, nombre = nombre))
        }
        "move" -> {
            val id = opts["id"] ?: return usage("Falta --id para move")
            val playerId = (opts["playerId"] ?: return usage("Falta --playerId para move")).toLongOrNull()
                ?: return usage("--playerId inválido")
            val fila = (opts["fila"] ?: return usage("Falta --fila para move")).toIntOrNull()
                ?: return usage("--fila inválido")
            val columna = (opts["columna"] ?: return usage("Falta --columna para move")).toIntOrNull()
                ?: return usage("--columna inválido")
            val contenido = opts["contenido"] ?: return usage("Falta --contenido para move")
            Comando.RealizarMovimiento(idPartida = id, jugadorId = playerId, fila = fila, columna = columna, contenido = contenido)
        }
        else -> return usage("--cmd debe ser create|join|joinAuto|move")
    }

    Socket(host, puerto).use { socket ->
        val inReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val outWriter = PrintWriter(socket.getOutputStream(), true)

        outWriter.println(json.encodeToString<Comando>(comando))

        val linea = inReader.readLine()
        if (linea != null) {
            val evento = json.decodeFromString<Evento>(linea)
            println("[Cliente] Evento recibido: $evento")
        }
    }
}

private fun Array<String>.toOptions(): Map<String, String> {
    return mapNotNull { arg ->
        val idx = arg.indexOf('=')
        if (idx > 2 && arg.startsWith("--")) {
            val k = arg.substring(2, idx)
            val v = arg.substring(idx + 1)
            k to v
        } else null
    }.toMap()
}

private fun usage(msg: String): Nothing {
    System.err.println("Error: $msg")
    System.err.println(
        "Uso: --cmd=[create|join|move] [--host=H --port=P] " +
            "[--name=N --playerId=ID] [--id=PARTIDA] [--fila=F --columna=C --contenido=X]"
    )
    throw IllegalArgumentException(msg)
}


