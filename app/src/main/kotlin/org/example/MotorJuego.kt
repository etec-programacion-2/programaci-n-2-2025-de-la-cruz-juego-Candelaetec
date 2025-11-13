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

    fun determinarJugadorActual(): Jugador? {
        if (juego.estado != EstadoJuego.EN_CURSO) return null
        if (juego.jugadores.isEmpty()) return null
        val jugadoresConectados = juego.jugadoresConectados
        if (jugadoresConectados.isEmpty()) return null
        return if (juego.jugadorActual < juego.jugadores.size) {
            juego.jugadores[juego.jugadorActual]
        } else null
    }

    fun esTurnoDelJugador(jugador: Jugador): Boolean {
        val jugadorActual = determinarJugadorActual()
        return jugadorActual?.id == jugador.id
    }

    fun validarCondicionVictoria(): Juego {
        return when (juego.tipoJuego) {
            TipoJuego.TRES_EN_LINEA -> validarVictoriaTresEnLinea()
            TipoJuego.AJEDREZ -> validarVictoriaAjedrez()
            TipoJuego.DAMAS -> validarVictoriaDamas()
            TipoJuego.GENERICO -> validarVictoriaGenerica()
        }
    }

    private fun validarVictoriaTresEnLinea(): Juego {
        val tablero = juego.tablero
        for (fila in 0 until tablero.filas) {
            val filaCompleta = tablero.obtenerFila(fila)
            if (esLineaGanadora(filaCompleta)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        for (columna in 0 until tablero.columnas) {
            val columnaCompleta = tablero.obtenerColumna(columna)
            if (esLineaGanadora(columnaCompleta)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        if (tablero.filas == tablero.columnas) {
            val diagonalPrincipal = mutableListOf<Celda>()
            for (i in 0 until tablero.filas) {
                diagonalPrincipal.add(tablero.obtenerCelda(i, i))
            }
            if (esLineaGanadora(diagonalPrincipal)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
            val diagonalSecundaria = mutableListOf<Celda>()
            for (i in 0 until tablero.filas) {
                diagonalSecundaria.add(tablero.obtenerCelda(i, tablero.columnas - 1 - i))
            }
            if (esLineaGanadora(diagonalSecundaria)) {
                return juego.cambiarEstado(EstadoJuego.FINALIZADO)
            }
        }
        if (tablero.obtenerCeldasVacias().isEmpty()) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        return juego
    }

    private fun esLineaGanadora(linea: List<Celda>): Boolean {
        if (linea.isEmpty()) return false
        val primerContenido = linea[0].contenido
        if (primerContenido.isNullOrBlank()) return false
        return linea.all { !it.estaVacia && it.contenido == primerContenido }
    }

    private fun validarVictoriaAjedrez(): Juego {
        val tablero = juego.tablero
        val celdasOcupadas = tablero.obtenerCeldasOcupadas()
        val reyesEncontrados = celdasOcupadas.filter { celda ->
            celda.contenido == "♚" || celda.contenido == "♔"
        }
        if (reyesEncontrados.size < 2) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        return juego
    }

    private fun validarVictoriaDamas(): Juego {
        val tablero = juego.tablero
        val celdasOcupadas = tablero.obtenerCeldasOcupadas()
        val piezasBlancas = celdasOcupadas.count {
            it.contenido?.contains("♔") == true || it.contenido?.contains("♕") == true
        }
        val piezasNegras = celdasOcupadas.count {
            it.contenido?.contains("♚") == true || it.contenido?.contains("♛") == true
        }
        if (piezasBlancas == 0 || piezasNegras == 0) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        return juego
    }

    private fun validarVictoriaGenerica(): Juego {
        if (juego.tablero.obtenerCeldasVacias().isEmpty()) {
            return juego.cambiarEstado(EstadoJuego.FINALIZADO)
        }
        return juego
    }

    fun calcularSiguienteJugador(): Int {
        if (juego.jugadores.isEmpty()) return 0
        var siguienteIndice = (juego.jugadorActual + 1) % juego.jugadores.size
        var intentos = 0
        while (intentos < juego.jugadores.size) {
            if (juego.jugadores[siguienteIndice].conectado) {
                return siguienteIndice
            }
            siguienteIndice = (siguienteIndice + 1) % juego.jugadores.size
            intentos++
        }
        return juego.jugadorActual
    }

    fun procesarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        require(juego.estado == EstadoJuego.EN_CURSO) {
            "El juego debe estar en curso para realizar movimientos. Estado actual: ${juego.estado}"
        }
        require(esTurnoDelJugador(jugador)) {
            "No es el turno del jugador '${jugador.nombre}'. Turno actual: ${determinarJugadorActual()?.nombre}"
        }
        val juegoConMovimiento = juego.ejecutarMovimientoInterno(jugador, movimiento)
        val motorActualizado = MotorJuego(juegoConMovimiento)
        val juegoConEstadoValidado = motorActualizado.validarCondicionVictoria()
        return if (juegoConEstadoValidado.estado == EstadoJuego.EN_CURSO) {
            val siguienteJugadorIndice = MotorJuego(juegoConEstadoValidado).calcularSiguienteJugador()
            juegoConEstadoValidado.copy(jugadorActual = siguienteJugadorIndice)
        } else {
            juegoConEstadoValidado
        }
    }

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

    fun puedeJugar(): Boolean {
        return juego.estado == EstadoJuego.EN_CURSO &&
                juego.jugadoresConectados.isNotEmpty()
    }

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