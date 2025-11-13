package org.example

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Sistema de logging personalizado para debugging de comunicaciones
 * entre clientes heterogéneos y servidor.
 */
object Logger {

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private var currentLevel = Level.INFO

    fun setLevel(level: Level) {
        currentLevel = level
    }

    fun debug(message: String, vararg args: Any?) {
        log(Level.DEBUG, message, *args)
    }

    fun info(message: String, vararg args: Any?) {
        log(Level.INFO, message, *args)
    }

    fun warn(message: String, vararg args: Any?) {
        log(Level.WARN, message, *args)
    }

    fun error(message: String, vararg args: Any?) {
        log(Level.ERROR, message, *args)
    }

    private fun log(level: Level, message: String, vararg args: Any?) {
        if (level.ordinal < currentLevel.ordinal) return

        val timestamp = LocalDateTime.now().format(formatter)
        val formattedMessage = if (args.isNotEmpty()) message.format(*args) else message
        val levelPrefix = when (level) {
            Level.DEBUG -> "🐛 DEBUG"
            Level.INFO -> "ℹ️  INFO"
            Level.WARN -> "⚠️  WARN"
            Level.ERROR -> "❌ ERROR"
        }

        val threadName = Thread.currentThread().name
        val logLine = "[$timestamp] [$levelPrefix] [$threadName] $formattedMessage"

        // Imprimir en consola
        println(logLine)

        // En un sistema real, aquí se escribiría a un archivo de log
        // appendToLogFile(logLine)
    }

    // Funciones específicas para logging de red
    fun logClientConnection(clientType: String, clientId: String, action: String) {
        info("Cliente $clientType [$clientId]: $action")
    }

    fun logServerEvent(event: String, clientInfo: String = "") {
        info("Servidor: $event ${if (clientInfo.isNotEmpty()) "-> $clientInfo" else ""}")
    }

    fun logMessageSerialization(direction: String, messageType: String, content: String) {
        debug("$direction $messageType: ${content.take(100)}${if (content.length > 100) "..." else ""}")
    }

    fun logGameStateChange(gameId: String, oldState: EstadoJuego, newState: EstadoJuego, trigger: String) {
        info("Juego [$gameId]: Estado cambió de $oldState a $newState (trigger: $trigger)")
    }

    fun logPlayerAction(gameId: String, playerId: Long, playerName: String, action: String) {
        info("Juego [$gameId]: Jugador $playerName (ID:$playerId) realizó: $action")
    }

    fun logError(context: String, error: String, details: String = "") {
        error("$context - $error ${if (details.isNotEmpty()) "($details)" else ""}")
    }

    fun logPerformance(operation: String, durationMs: Long) {
        debug("Performance: $operation tomó ${durationMs}ms")
    }

    // Función para medir tiempo de ejecución
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block()
        } finally {
            val duration = System.currentTimeMillis() - start
            logPerformance(operation, duration)
        }
    }
}