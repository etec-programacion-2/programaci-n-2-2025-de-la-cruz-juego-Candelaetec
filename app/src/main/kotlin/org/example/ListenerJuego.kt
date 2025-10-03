package org.example

/**
 * Interfaz que define los eventos que pueden ocurrir durante un juego.
 * Implementa el patrón Observer para desacoplar la lógica del juego de la interfaz de usuario.
 * 
 * Los componentes que quieran reaccionar a eventos del juego deben implementar esta interfaz
 * y registrarse como listeners en la instancia del juego.
 */
interface ListenerJuego {
    
    /**
     * Se ejecuta cuando un jugador realiza un movimiento válido
     * 
     * @param juego Estado actual del juego después del movimiento
     * @param jugador El jugador que realizó el movimiento
     * @param movimiento El movimiento que se realizó
     */
    fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) = Unit
    
    /**
     * Se ejecuta cuando el turno cambia a otro jugador
     * 
     * @param juego Estado actual del juego
     * @param jugadorAnterior El jugador cuyo turno acaba de terminar (puede ser null)
     * @param jugadorActual El jugador cuyo turno comienza ahora (puede ser null)
     */
    fun onTurnoCambiado(juego: Juego, jugadorAnterior: Jugador?, jugadorActual: Jugador?) = Unit
    
    /**
     * Se ejecuta cuando el juego termina (victoria, empate, etc.)
     * 
     * @param juego Estado final del juego
     * @param ganador El jugador ganador, o null si fue empate
     * @param razonFinalizacion Descripción de por qué terminó el juego
     */
    fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) = Unit
    
    /**
     * Se ejecuta cuando un jugador se conecta o desconecta
     * 
     * @param juego Estado actual del juego
     * @param jugador El jugador que cambió su estado de conexión
     * @param conectado true si se conectó, false si se desconectó
     */
    fun onEstadoConexionCambiado(juego: Juego, jugador: Jugador, conectado: Boolean) = Unit
    
    /**
     * Se ejecuta cuando se agrega un nuevo jugador al juego
     * 
     * @param juego Estado actual del juego
     * @param jugador El jugador que se agregó
     */
    fun onJugadorAgregado(juego: Juego, jugador: Jugador) = Unit
    
    /**
     * Se ejecuta cuando se remueve un jugador del juego
     * 
     * @param juego Estado actual del juego
     * @param jugador El jugador que se removió
     */
    fun onJugadorRemovido(juego: Juego, jugador: Jugador) = Unit
    
    /**
     * Se ejecuta cuando el estado del juego cambia (ej: ESPERANDO_JUGADORES -> EN_CURSO)
     * 
     * @param juego Estado actual del juego
     * @param estadoAnterior El estado anterior del juego
     * @param estadoNuevo El nuevo estado del juego
     */
    fun onEstadoJuegoCambiado(juego: Juego, estadoAnterior: EstadoJuego, estadoNuevo: EstadoJuego) = Unit
    
    /**
     * Se ejecuta cuando ocurre un error durante el procesamiento de un movimiento
     * 
     * @param juego Estado actual del juego
     * @param jugador El jugador que intentó realizar el movimiento
     * @param movimiento El movimiento que falló
     * @param error La excepción que se produjo
     */
    fun onErrorMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento, error: Exception) = Unit
    
    /**
     * Se ejecuta cuando se inicia una nueva ronda
     * 
     * @param juego Estado actual del juego
     * @param numeroRonda El número de la nueva ronda
     */
    fun onNuevaRonda(juego: Juego, numeroRonda: Int) = Unit
    
    /**
     * Se ejecuta cuando el juego se pausa o reanuda
     * 
     * @param juego Estado actual del juego
     * @param pausado true si se pausó, false si se reanudó
     */
    fun onJuegoPausado(juego: Juego, pausado: Boolean) = Unit
}

/**
 * Implementación de conveniencia que permite extender solo los métodos necesarios
 * sin tener que implementar todos los métodos de la interfaz
 */
abstract class ListenerJuegoBase : ListenerJuego {
    // Todos los métodos ya tienen implementación por defecto en la interfaz
}

/**
 * Clase de datos que encapsula información sobre un evento del juego
 */
data class EventoJuego(
    val tipo: TipoEvento,
    val juego: Juego,
    val timestamp: Long = System.currentTimeMillis(),
    val jugador: Jugador? = null,
    val movimiento: Movimiento? = null,
    val datos: Map<String, Any> = emptyMap()
) {
    enum class TipoEvento {
        MOVIMIENTO_REALIZADO,
        TURNO_CAMBIADO,
        JUEGO_TERMINADO,
        CONEXION_CAMBIADA,
        JUGADOR_AGREGADO,
        JUGADOR_REMOVIDO,
        ESTADO_CAMBIADO,
        ERROR_MOVIMIENTO,
        NUEVA_RONDA,
        JUEGO_PAUSADO
    }
}

/**
 * Listener especializado para logging/debugging
 */
class LoggerListener : ListenerJuegoBase() {
    
    override fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) {
        println("🎮 [${obtenerTimestamp()}] Movimiento: ${jugador.nombre} realizó $movimiento")
    }
    
    override fun onTurnoCambiado(juego: Juego, jugadorAnterior: Jugador?, jugadorActual: Jugador?) {
        val anterior = jugadorAnterior?.nombre ?: "Nadie"
        val actual = jugadorActual?.nombre ?: "Nadie"
        println("🔄 [${obtenerTimestamp()}] Turno: $anterior → $actual")
    }
    
    override fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) {
        val resultado = ganador?.let { "Ganó ${it.nombre}" } ?: "Empate"
        println("🏁 [${obtenerTimestamp()}] org.example.Juego terminado: $resultado ($razonFinalizacion)")
    }
    
    override fun onEstadoConexionCambiado(juego: Juego, jugador: Jugador, conectado: Boolean) {
        val estado = if (conectado) "conectó" else "desconectó"
        println("🔌 [${obtenerTimestamp()}] ${jugador.nombre} se $estado")
    }
    
    override fun onJugadorAgregado(juego: Juego, jugador: Jugador) {
        println("➕ [${obtenerTimestamp()}] Jugador agregado: ${jugador.nombre}")
    }
    
    override fun onJugadorRemovido(juego: Juego, jugador: Jugador) {
        println("➖ [${obtenerTimestamp()}] Jugador removido: ${jugador.nombre}")
    }
    
    override fun onEstadoJuegoCambiado(juego: Juego, estadoAnterior: EstadoJuego, estadoNuevo: EstadoJuego) {
        println("🎯 [${obtenerTimestamp()}] Estado: $estadoAnterior → $estadoNuevo")
    }
    
    override fun onErrorMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento, error: Exception) {
        println("❌ [${obtenerTimestamp()}] Error: ${jugador.nombre} falló $movimiento - ${error.message}")
    }
    
    override fun onNuevaRonda(juego: Juego, numeroRonda: Int) {
        println("🆕 [${obtenerTimestamp()}] Nueva ronda: #$numeroRonda")
    }
    
    override fun onJuegoPausado(juego: Juego, pausado: Boolean) {
        val accion = if (pausado) "pausado" else "reanudado"
        println("⏸️ [${obtenerTimestamp()}] org.example.Juego $accion")
    }
    
    private fun obtenerTimestamp(): String {
        return java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())
    }
}

/**
 * Listener que mantiene estadísticas del juego
 */
class EstadisticasListener : ListenerJuegoBase() {
    
    private val estadisticas = mutableMapOf<String, Int>()
    private val movimientosPorJugador = mutableMapOf<Long, Int>()
    
    override fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) {
        estadisticas["movimientos_totales"] = estadisticas.getOrDefault("movimientos_totales", 0) + 1
        movimientosPorJugador[jugador.id] = movimientosPorJugador.getOrDefault(jugador.id, 0) + 1
        
        if (movimiento.esColocacion) {
            estadisticas["colocaciones"] = estadisticas.getOrDefault("colocaciones", 0) + 1
        } else {
            estadisticas["movimientos_complejos"] = estadisticas.getOrDefault("movimientos_complejos", 0) + 1
        }
    }
    
    override fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) {
        estadisticas["juegos_completados"] = estadisticas.getOrDefault("juegos_completados", 0) + 1
        
        if (ganador != null) {
            estadisticas["victorias"] = estadisticas.getOrDefault("victorias", 0) + 1
        } else {
            estadisticas["empates"] = estadisticas.getOrDefault("empates", 0) + 1
        }
    }
    
    override fun onErrorMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento, error: Exception) {
        estadisticas["movimientos_invalidos"] = estadisticas.getOrDefault("movimientos_invalidos", 0) + 1
    }
    
    fun obtenerEstadisticas(): Map<String, Int> = estadisticas.toMap()
    
    fun obtenerMovimientosPorJugador(): Map<Long, Int> = movimientosPorJugador.toMap()
    
    fun reiniciarEstadisticas() {
        estadisticas.clear()
        movimientosPorJugador.clear()
    }
}

/**
 * Listener que simula una interfaz de usuario simple
 */
class InterfazUsuarioListener : ListenerJuegoBase() {
    
    override fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) {
        println("\n📱 [UI] Actualizando tablero después del movimiento de ${jugador.nombre}")
        mostrarTableroUI(juego)
    }
    
    override fun onTurnoCambiado(juego: Juego, jugadorAnterior: Jugador?, jugadorActual: Jugador?) {
        if (jugadorActual != null) {
            println("\n📱 [UI] Es el turno de: ${jugadorActual.nombre}")
            println("📱 [UI] Esperando movimiento...")
        }
    }
    
    override fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) {
        println("\n📱 [UI] ============ JUEGO TERMINADO ============")
        if (ganador != null) {
            println("📱 [UI] 🏆 ¡Felicitaciones ${ganador.nombre}! Has ganado.")
        } else {
            println("📱 [UI] 🤝 ¡Empate! Buen juego para todos.")
        }
        println("📱 [UI] Razón: $razonFinalizacion")
        println("📱 [UI] ======================================")
    }
    
    override fun onEstadoConexionCambiado(juego: Juego, jugador: Jugador, conectado: Boolean) {
        if (conectado) {
            println("📱 [UI] 💚 ${jugador.nombre} se reconectó")
        } else {
            println("📱 [UI] 💔 ${jugador.nombre} se desconectó")
        }
    }
    
    override fun onErrorMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento, error: Exception) {
        println("📱 [UI] ⚠️ Error: ${error.message}")
        println("📱 [UI] Por favor, ${jugador.nombre}, intenta otro movimiento.")
    }
    
    private fun mostrarTableroUI(juego: Juego) {
        val lineas = juego.verTablero().split("\n")
        lineas.forEach { linea ->
            println("📱 [UI] $linea")
        }
    }
}

/**
 * Listener que guarda el historial del juego
 */
class HistorialListener : ListenerJuegoBase() {
    
    private val historial = mutableListOf<EventoJuego>()
    
    override fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) {
        historial.add(
            EventoJuego(
                tipo = EventoJuego.TipoEvento.MOVIMIENTO_REALIZADO,
                juego = juego,
                jugador = jugador,
                movimiento = movimiento,
                datos = mapOf(
                    "ronda" to juego.rondaActual,
                    "turno" to juego.jugadorActual
                )
            )
        )
    }
    
    override fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) {
        historial.add(
            EventoJuego(
                tipo = EventoJuego.TipoEvento.JUEGO_TERMINADO,
                juego = juego,
                jugador = ganador,
                datos = mapOf(
                    "razon" to razonFinalizacion,
                    "duracion_movimientos" to historial.count { it.tipo == EventoJuego.TipoEvento.MOVIMIENTO_REALIZADO }
                )
            )
        )
    }
    
    fun obtenerHistorial(): List<EventoJuego> = historial.toList()
    
    fun limpiarHistorial() = historial.clear()
    
    fun obtenerMovimientosDelJugador(jugadorId: Long): List<EventoJuego> {
        return historial.filter { 
            it.tipo == EventoJuego.TipoEvento.MOVIMIENTO_REALIZADO && it.jugador?.id == jugadorId 
        }
    }
    
    fun obtenerResumenPartida(): String {
        val movimientos = historial.count { it.tipo == EventoJuego.TipoEvento.MOVIMIENTO_REALIZADO }
        val juegoFinalizado = historial.any { it.tipo == EventoJuego.TipoEvento.JUEGO_TERMINADO }
        
        return """
            📊 RESUMEN DE LA PARTIDA:
            • Movimientos totales: $movimientos
            • Estado: ${if (juegoFinalizado) "Finalizada" else "En curso"}
            • Duración: ${historial.size} eventos registrados
        """.trimIndent()
    }
}