package org.example

data class Juego(
    val id: String,
    val tablero: Tablero,
    val jugadores: List<Jugador> = listOf(),
    val estado: EstadoJuego = EstadoJuego.ESPERANDO_JUGADORES,
    val jugadorActual: Int = 0,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val maxJugadores: Int = 4,
    val rondaActual: Int = 1,
    val tipoJuego: TipoJuego = TipoJuego.GENERICO,
    private val listeners: List<ListenerJuego> = listOf()
) {

    val jugadoresConectados: List<Jugador>
        get() = jugadores.filter { it.conectado }

    val jugadorEnTurno: Jugador?
        get() = if (jugadores.isNotEmpty() && jugadorActual in jugadores.indices) jugadores[jugadorActual] else null

    fun agregarJugador(jugador: Jugador): Juego {
        require(jugadores.size < maxJugadores) { "El juego ya está lleno" }
        require(jugadores.none { it.id == jugador.id }) { "El jugador ya está en la partida" }
        val nuevosJugadores = jugadores + jugador
        return copy(jugadores = nuevosJugadores)
    }

    fun removerJugador(jugador: Jugador): Juego {
        val nuevosJugadores = jugadores.filter { it.id != jugador.id }
        return copy(jugadores = nuevosJugadores)
    }

    fun iniciarJuego(): Juego {
        require(jugadores.size >= 2) { "Se requieren al menos 2 jugadores para iniciar" }
        return copy(estado = EstadoJuego.EN_CURSO, jugadorActual = 0, rondaActual = 1)
    }

    fun cambiarEstado(nuevoEstado: EstadoJuego): Juego {
        return copy(estado = nuevoEstado)
    }

    fun siguienteRonda(): Juego {
        return copy(rondaActual = rondaActual + 1)
    }

    fun realizarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        val motor = MotorJuego(this)
        return motor.procesarMovimiento(jugador, movimiento)
    }

    fun realizarMovimiento(jugadorId: Long, fila: Int, columna: Int, contenido: String): Juego {
        val jugador = jugadores.find { it.id == jugadorId }
            ?: throw IllegalArgumentException("Jugador no encontrado")
        val movimiento = Movimiento.colocacion(fila, columna, contenido)
        return realizarMovimiento(jugador, movimiento)
    }

    fun verTablero(): String {
        val sb = StringBuilder()
        for (fila in 0 until tablero.filas) {
            for (col in 0 until tablero.columnas) {
                val celda = tablero.obtenerCelda(fila, col)
                sb.append(celda.contenido ?: ".")
                if (col < tablero.columnas - 1) sb.append(" ")
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun posicionesDisponibles(): List<Pair<Int, Int>> {
        val libres = mutableListOf<Pair<Int, Int>>()
        for (fila in 0 until tablero.filas) {
            for (col in 0 until tablero.columnas) {
                if (tablero.obtenerCelda(fila, col).estaVacia) {
                    libres.add(fila to col)
                }
            }
        }
        return libres
    }

    fun posicionDisponible(fila: Int, columna: Int): Boolean {
        return tablero.obtenerCelda(fila, columna).estaVacia
    }

    fun ejecutarMovimientoInterno(jugador: Jugador, movimiento: Movimiento): Juego {
        val nuevoTablero = when {
            movimiento.esColocacion -> {
                val celda = tablero.obtenerCelda(movimiento.filaDestino, movimiento.columnaDestino)
                require(celda.estaVacia) { "La celda ya está ocupada" }
                val nuevoTablero = Tablero(tablero.filas, tablero.columnas)
                // Copiar el estado actual
                for (f in 0 until tablero.filas) {
                    for (c in 0 until tablero.columnas) {
                        val actual = tablero.obtenerCelda(f, c)
                        if (!actual.estaVacia) {
                            nuevoTablero.colocarEnCelda(f, c, actual.contenido!!)
                        }
                    }
                }
                nuevoTablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, movimiento.contenido ?: "")
                nuevoTablero
            }
            else -> {
                // Movimiento normal (de origen a destino)
                val celdaOrigen = tablero.obtenerCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
                val celdaDestino = tablero.obtenerCelda(movimiento.filaDestino, movimiento.columnaDestino)
                require(!celdaOrigen.estaVacia) { "No hay pieza en la celda de origen" }
                require(celdaDestino.estaVacia) { "La celda de destino ya está ocupada" }
                val nuevoTablero = Tablero(tablero.filas, tablero.columnas)
                for (f in 0 until tablero.filas) {
                    for (c in 0 until tablero.columnas) {
                        val actual = tablero.obtenerCelda(f, c)
                        if (!actual.estaVacia && !(f == movimiento.filaOrigen && c == movimiento.columnaOrigen)) {
                            nuevoTablero.colocarEnCelda(f, c, actual.contenido!!)
                        }
                    }
                }
                nuevoTablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, celdaOrigen.contenido!!)
                nuevoTablero
            }
        }
        return copy(tablero = nuevoTablero)
    }

    fun agregarListener(listener: ListenerJuego): Juego {
        return copy(listeners = listeners + listener)
    }

    fun notificarEvento(evento: EventoJuego) {
        // listeners.forEach { it.onEvento(evento) }
    }
}