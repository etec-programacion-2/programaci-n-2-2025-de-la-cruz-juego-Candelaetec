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
class ServidorMain {

    fun iniciar() {
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
}

fun main() {
    val servidor = ServidorMain()
    servidor.iniciar()
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
        // Utiliza polimorfismo: cada comando sabe cómo ejecutarse
        comando.ejecutar(ServicioPartidas)
    }
}


