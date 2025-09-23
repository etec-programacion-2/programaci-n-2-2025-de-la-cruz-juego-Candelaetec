package org.example

/**
 * Clase responsable de la lógica del juego: gestión de turnos, validación de condiciones
 * de victoria/empate y control del flujo del juego.
 * 
 * Esta clase implementa el patrón de Separación de Responsabilidades (SoC):
 * - La clase Juego se encarga del estado y datos
 * - MotorJuego se encarga de la lógica y reglas del juego
 * 
 * @param juego La instancia del juego que será controlada por este motor
 */
class MotorJuego(private val juego: Juego) {
    
    /**
     * Determina cuál es el jugador que debe jugar en el turno actual
     * 
     * @return El jugador cuyo turno es actual, o null si no hay jugadores o el juego no está en curso
     */
    fun determinarJugadorActual(): Jugador? {
        if (juego.estado != EstadoJuego.EN_CURSO) return null
        if (juego.jugadores.isEmpty()) return null
        
        val jugadoresConectados = juego.jugadoresConectados
        if (jugadoresConectados.isEmpty()) return null
        
        // Calcular el índice del jugador actual entre los conectados
        val indiceEnConectados = juego.jugadorActual % jugadoresConectados.size
        return jugadoresConectados[indiceEnConectados]
    }
    
    /**
     * Valida si es el turno del jugador especificado
     * 
     * @param jugador El jugador que intenta realizar un movimiento
     * @return true si es el turno del jugador, false en caso contrario
     */
    fun esTurnoDelJugador(jugador: Jugador): Boolean {
        val jugadorActual = determinarJugadorActual()
        return jugadorActual?.id == jugador.id
    }
    
    /**
     * Valida las condiciones de victoria y actualiza el estado del juego si es necesario
     * 
     * @return Nueva instancia del juego con el estado actualizado si se detecta una condición de fin
     */
    fun validarCondicionVictoria(): Juego {
        when (juego.tipoJuego) {
            TipoJuego.TRES_EN_LINEA -> return validarVictoriaTresEnLinea()
            TipoJuego.AJEDREZ -> return validarVictoriaAjedrez()
            TipoJuego.DAMAS -> return validarVictoriaDamas()
            TipoJuego.GENERICO -> return validarVictoriaGenerica()
        }
    }
    
    /**
     * Valida condiciones de victoria específicas para tres en línea
     */
    private fun validarVictoriaTresEnLinea(): Juego {
        val tablero = juego.tablero
        
        // Verificar filas
        for (fila in 0 until tablero.filas) {
            val filaCompleta = tablero.obtenerFila(fila)
            if (esLineaGanadora(filaCompleta)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        
        // Verificar columnas
        for (columna in 0 until tablero.columnas) {
            val columnaCompleta = tablero.obtenerColumna(columna)
            if (esLineaGanadora(columnaCompleta)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        
        // Verificar diagonales (solo para tableros cuadrados)
        if (tablero.filas == tablero.columnas) {
            // Diagonal principal
            val diagonalPrincipal = mutableListOf<Celda>()
            for (i in 0 until tablero.filas) {
                diagonalPrincipal.add(tablero.obtenerCelda(i, i))
            }
            if (esLineaGanadora(diagonalPrincipal)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
            
            // Diagonal secundaria
            val diagonalSecundaria = mutableListOf<Celda>()
            for (i in 0 until tablero.filas) {
                diagonalSecundaria.add(tablero.obtenerCelda(i, tablero.columnas - 1 - i))
            }
            if (esLineaGanadora(diagonalSecundaria)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        
        // Verificar empate (tablero lleno sin ganador)
        if (tablero.obtenerCeldasVacias().isEmpty()) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        
        return juego
    }
    
    /**
     * Verifica si una línea de celdas constituye una victoria
     * (todas las celdas tienen el mismo contenido no vacío)
     */
    private fun esLineaGanadora(linea: List<Celda>): Boolean {
        if (linea.isEmpty()) return false
        
        val primerContenido = linea[0].contenido
        if (primerContenido.isNullOrBlank()) return false
        
        return linea.all { !it.estaVacia && it.contenido == primerContenido }
    }
    
    /**
     * Valida condiciones de victoria para ajedrez (simplificada)
     */
    private fun validarVictoriaAjedrez(): Juego {
        val tablero = juego.tablero
        val celdasOcupadas = tablero.obtenerCeldasOcupadas()
        
        // Buscar reyes en el tablero
        val reyesEncontrados = celdasOcupadas.filter { celda ->
            celda.contenido == "♚" || celda.contenido == "♔"
        }
        
        // Si hay menos de 2 reyes, el juego ha terminado
        if (reyesEncontrados.size < 2) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        
        // Verificar jaque mate (implementación simplificada)
        // En una implementación real, aquí se verificaría si el rey está en jaque
        // y no puede moverse a una posición segura
        
        return juego
    }
    
    /**
     * Valida condiciones de victoria para damas
     */
    private fun validarVictoriaDamas(): Juego {
        val tablero = juego.tablero
        val celdasOcupadas = tablero.obtenerCeldasOcupadas()
        
        // Contar piezas por jugador (implementación simplificada)
        val piezasBlancas = celdasOcupadas.count { 
            it.contenido?.contains("♔") == true || it.contenido?.contains("♕") == true 
        }
        val piezasNegras = celdasOcupadas.count { 
            it.contenido?.contains("♚") == true || it.contenido?.contains("♛") == true 
        }
        
        // Si un jugador se queda sin piezas, el otro gana
        if (piezasBlancas == 0 || piezasNegras == 0) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        
        return juego
    }
    
    /**
     * Valida condiciones de victoria genéricas
     */
    private fun validarVictoriaGenerica(): Juego {
        // Para juegos genéricos, solo verificamos empate por tablero lleno
        if (juego.tablero.obtenerCeldasVacias().isEmpty()) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        
        return juego
    }
    
    /**
     * Calcula el índice del siguiente jugador que debe jugar
     * Salta automáticamente jugadores desconectados
     * 
     * @return El índice del siguiente jugador válido
     */
    fun calcularSiguienteJugador(): Int {
        if (juego.jugadores.isEmpty()) return 0
        
        val jugadoresConectados = juego.jugadoresConectados
        if (jugadoresConectados.isEmpty()) return juego.jugadorActual
        
        // Encontrar el índice actual en la lista de conectados
        val jugadorActualConectado = determinarJugadorActual()
        val indiceActualEnConectados = jugadoresConectados.indexOfFirst { 
            it.id == jugadorActualConectado?.id 
        }
        
        // Calcular siguiente índice en la lista de conectados
        val siguienteIndiceConectados = (indiceActualEnConectados + 1) % jugadoresConectados.size
        val siguienteJugadorConectado = jugadoresConectados[siguienteIndiceConectados]
        
        // Encontrar el índice de este jugador en la lista completa
        return juego.jugadores.indexOfFirst { it.id == siguienteJugadorConectado.id }
    }
    
    /**
     * Procesa un movimiento completo: valida turno, ejecuta movimiento, 
     * verifica condiciones de victoria y avanza turno
     * 
     * @param jugador El jugador que realiza el movimiento
     * @param movimiento El movimiento a realizar
     * @return Nueva instancia del juego con todos los cambios aplicados
     * @throws IllegalStateException Si no es el turno del jugador o el juego no está en curso
     */
    fun procesarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        // Validar que el juego esté en curso
        require(juego.estado == EstadoJuego.EN_CURSO) {
            "El juego debe estar en curso para realizar movimientos. Estado actual: ${juego.estado}"
        }
        
        // Validar turno
        require(esTurnoDelJugador(jugador)) {
            "No es el turno del jugador '${jugador.nombre}'. Turno actual: ${determinarJugadorActual()?.nombre}"
        }
        
        // Ejecutar el movimiento (delegamos a la clase Juego)
        val juegoConMovimiento = juego.ejecutarMovimientoInterno(jugador, movimiento)
        
        // Crear un nuevo motor con el juego actualizado para validar condiciones
        val motorActualizado = MotorJuego(juegoConMovimiento)
        val juegoConEstadoValidado = motorActualizado.validarCondicionVictoria()
        
        // Si el juego no ha terminado, avanzar turno
        return if (juegoConEstadoValidado.estado == EstadoJuego.EN_CURSO) {
            val siguienteJugadorIndice = MotorJuego(juegoConEstadoValidado).calcularSiguienteJugador()
            juegoConEstadoValidado.copy(jugadorActual = siguienteJugadorIndice)
        } else {
            juegoConEstadoValidado
        }
    }
    
    /**
     * Obtiene información del estado actual del juego para debugging
     */
    fun obtenerEstadoJuego(): String {
        val sb = StringBuilder()
        sb.appendLine("=== ESTADO DEL JUEGO ===")
        sb.appendLine("ID: ${juego.id}")
        sb.appendLine("Tipo: ${juego.tipoJuego}")
        sb.appendLine("Estado: ${juego.estado}")
        sb.appendLine("Ronda: ${juego.rondaActual}")
        sb.appendLine("Jugadores: ${juego.jugadores.size}/${juego.maxJugadores}")
        sb.appendLine("Jugador actual: ${determinarJugadorActual()?.nombre ?: "N/A"}")
        sb.appendLine("Jugadores conectados: ${juego.jugadoresConectados.map { it.nombre }}")
        sb.appendLine("Celdas ocupadas: ${juego.tablero.contarCeldasOcupadas()}/${juego.tablero.filas * juego.tablero.columnas}")
        
        return sb.toString()
    }
    
    /**
     * Verifica si el juego puede continuar
     */
    fun puedeJugar(): Boolean {
        return juego.estado == EstadoJuego.EN_CURSO && 
               juego.jugadoresConectados.isNotEmpty()
    }
    
    /**
     * Obtiene estadísticas del juego actual
     */
    fun obtenerEstadisticas(): Map<String, Any> {
        return mapOf(
            "jugadores_totales" to juego.jugadores.size,
            "jugadores_conectados" to juego.jugadoresConectados.size,
            "celdas_ocupadas" to juego.tablero.contarCeldasOcupadas(),
            "celdas_disponibles" to juego.tablero.obtenerCeldasVacias().size,
            "puede_continuar" to puedeJugar(),
            "turno_actual" to (determinarJugadorActual()?.nombre ?: "Ninguno"),
            "ronda" to juego.rondaActual
        )
    }
}