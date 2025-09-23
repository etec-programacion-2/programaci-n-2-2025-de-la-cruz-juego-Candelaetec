package org.example

/**
 * Data class que representa el estado completo de una partida con gestión avanzada de movimientos
 * 
 * @param id Identificador único de la partida
 * @param tablero El tablero donde se desarrolla el juego
 * @param jugadores Lista de jugadores participando en la partida
 * @param estado Estado actual de la partida
 * @param jugadorActual Índice del jugador cuyo turno es actual
 * @param fechaCreacion Timestamp de cuándo se creó la partida
 * @param maxJugadores Número máximo de jugadores permitidos (por defecto 4)
 * @param rondaActual Número de la ronda actual (por defecto 1)
 * @param tipoJuego Tipo de juego para aplicar reglas específicas
 */
data class Juego(
    val id: String,
    val tablero: Tablero,
    val jugadores: List<Jugador> = listOf(),
    val estado: EstadoJuego = EstadoJuego.ESPERANDO_JUGADORES,
    val jugadorActual: Int = 0, // Índice del jugador actual
    val fechaCreacion: Long = System.currentTimeMillis(),
    val maxJugadores: Int = 4,
    val rondaActual: Int = 1,
    val tipoJuego: TipoJuego = TipoJuego.GENERICO
) {
    // Constructor secundario para crear juegos con tablero estándar
    constructor(
        id: String,
        filasTablero: Int = 8,
        columnasTablero: Int = 8,
        maxJugadores: Int = 4,
        tipoJuego: TipoJuego = TipoJuego.GENERICO
    ) : this(
        id = id,
        tablero = Tablero(filasTablero, columnasTablero),
        maxJugadores = maxJugadores,
        tipoJuego = tipoJuego
    )
    
    // Validaciones en el init block
    init {
        require(id.isNotBlank()) { "El ID del juego no puede estar vacío" }
        require(maxJugadores > 0) { "El número máximo de jugadores debe ser positivo" }
        require(rondaActual > 0) { "La ronda actual debe ser positiva" }
        require(jugadorActual >= 0) { "El índice del jugador actual debe ser mayor o igual a 0" }
    }
    
    /**
     * Propiedad computada que indica si el juego está lleno
     */
    val estaLleno: Boolean
        get() = jugadores.size >= maxJugadores
    
    /**
     * Propiedad computada que retorna los jugadores conectados
     */
    val jugadoresConectados: List<Jugador>
        get() = jugadores.filter { it.conectado }
    
    /**
     * Propiedad computada que retorna el jugador actual
     */
    val jugadorEnTurno: Jugador?
        get() = if (jugadores.isNotEmpty() && jugadorActual < jugadores.size) {
            jugadores[jugadorActual]
        } else null
    
    // ==========================================
    // MÉTODOS PRINCIPALES (delegan en MotorJuego)
    // ==========================================
    
    /**
     * Realiza un movimiento utilizando el MotorJuego
     */
    fun realizarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        val motor = MotorJuego(this)
        return motor.procesarMovimiento(jugador, movimiento)
    }

    /**
     * Método sobrecargado para compatibilidad (colocaciones simples como tres en línea)
     */
    fun realizarMovimiento(jugadorId: Long, fila: Int, columna: Int, contenido: String): Juego {
        val jugador = jugadores.find { it.id == jugadorId }
            ?: throw IllegalArgumentException("Jugador con ID $jugadorId no encontrado")
        
        val movimiento = Movimiento.colocacion(fila, columna, contenido)
        return realizarMovimiento(jugador, movimiento)
    }

    /**
     * Método interno para que el MotorJuego ejecute el movimiento
     * sin encargarse de turnos ni validación de victoria.
     */
    internal fun ejecutarMovimientoInterno(jugador: Jugador, movimiento: Movimiento): Juego {
        require(estado == EstadoJuego.EN_CURSO) {
            "El juego debe estar en curso para realizar movimientos. Estado actual: $estado"
        }
        require(jugadores.any { it.id == jugador.id }) {
            "El jugador '${jugador.nombre}' (ID: ${jugador.id}) no está en esta partida"
        }

        // Validar movimiento según reglas
        validarMovimiento(jugador, movimiento)

        // Ejecutar movimiento en el tablero
        val nuevoTablero = ejecutarMovimiento(movimiento)

        return this.copy(tablero = nuevoTablero)
    }
    
    // ==========================================
    // VALIDACIONES DE MOVIMIENTOS
    // ==========================================
    
    private fun validarMovimiento(jugador: Jugador, movimiento: Movimiento) {
        if (!movimiento.esColocacion) {
            require(tablero.coordenadasValidas(movimiento.filaOrigen, movimiento.columnaOrigen)) {
                "Coordenadas de origen (${movimiento.filaOrigen}, ${movimiento.columnaOrigen}) fuera del tablero"
            }
        }
        require(tablero.coordenadasValidas(movimiento.filaDestino, movimiento.columnaDestino)) {
            "Coordenadas de destino (${movimiento.filaDestino}, ${movimiento.columnaDestino}) fuera del tablero"
        }
        
        when (tipoJuego) {
            TipoJuego.TRES_EN_LINEA -> validarMovimientoTresEnLinea(movimiento)
            TipoJuego.AJEDREZ -> validarMovimientoAjedrez(jugador, movimiento)
            TipoJuego.DAMAS -> validarMovimientoDamas(jugador, movimiento)
            TipoJuego.GENERICO -> validarMovimientoGenerico(movimiento)
        }
    }
    
    private fun validarMovimientoTresEnLinea(movimiento: Movimiento) {
        require(movimiento.esColocacion) { "En tres en línea solo se pueden hacer colocaciones" }
        require(tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
            "La celda (${movimiento.filaDestino}, ${movimiento.columnaDestino}) ya está ocupada"
        }
        require(!movimiento.contenido.isNullOrBlank()) { "Debe especificar el contenido (X u O)" }
    }
    
    private fun validarMovimientoAjedrez(jugador: Jugador, movimiento: Movimiento) {
        require(!movimiento.esColocacion) { "En ajedrez se deben mover piezas existentes" }
        require(!tablero.estaVacia(movimiento.filaOrigen, movimiento.columnaOrigen)) {
            "No hay ninguna pieza en la posición de origen"
        }
        // Aquí iría lógica más compleja de validación de ajedrez
    }
    
    private fun validarMovimientoDamas(jugador: Jugador, movimiento: Movimiento) {
        require(!movimiento.esColocacion) { "En damas se deben mover piezas existentes" }
        require(!tablero.estaVacia(movimiento.filaOrigen, movimiento.columnaOrigen)) {
            "No hay ninguna pieza en la posición de origen"
        }
        require(movimiento.esDiagonal()) { "En damas solo se permiten movimientos diagonales" }
        require(tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
            "El destino debe estar vacío (excepto para capturas)"
        }
    }
    
    private fun validarMovimientoGenerico(movimiento: Movimiento) {
        if (movimiento.esColocacion) {
            require(tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
                "La celda destino ya está ocupada"
            }
        } else {
            require(!tablero.estaVacia(movimiento.filaOrigen, movimiento.columnaOrigen)) {
                "No hay ninguna pieza en la posición de origen"
            }
        }
    }
    
    // ==========================================
    // EJECUCIÓN DE MOVIMIENTOS
    // ==========================================
    
    private fun ejecutarMovimiento(movimiento: Movimiento): Tablero {
        val nuevoTablero = Tablero(tablero.filas, tablero.columnas)
        for (fila in 0 until tablero.filas) {
            for (columna in 0 until tablero.columnas) {
                val celda = tablero.obtenerCelda(fila, columna)
                if (!celda.estaVacia) {
                    nuevoTablero.colocarEnCelda(fila, columna, celda.contenido!!)
                }
            }
        }
        if (movimiento.esColocacion) {
            nuevoTablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, movimiento.contenido!!)
        } else {
            val contenidoAMover = tablero.obtenerCelda(movimiento.filaOrigen, movimiento.columnaOrigen).contenido ?: ""
            nuevoTablero.vaciarCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
            nuevoTablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, contenidoAMover)
        }
        return nuevoTablero
    }
    
    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    
    fun esturnoDelJugador(jugador: Jugador): Boolean = jugadorEnTurno?.id == jugador.id
    
    fun verTablero(): String = tablero.mostrarTablero()
    
    fun posicionDisponible(fila: Int, columna: Int): Boolean =
        tablero.coordenadasValidas(fila, columna) && tablero.estaVacia(fila, columna)
    
    fun posicionesDisponibles(): List<Pair<Int, Int>> =
        tablero.obtenerCeldasVacias().map { it.fila to it.columna }
    
    fun reiniciarTablero(): Juego =
        this.copy(tablero = Tablero(tablero.filas, tablero.columnas), rondaActual = 1, jugadorActual = 0)
    
    fun agregarJugador(jugador: Jugador): Juego {
        require(!estaLleno) { "El juego ya está lleno" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { "No se pueden agregar jugadores en estado $estado" }
        require(!jugadores.any { it.id == jugador.id }) { "Ya existe un jugador con ID ${jugador.id}" }
        return this.copy(jugadores = jugadores + jugador)
    }
    
    fun removerJugador(jugadorId: Long): Juego {
        val nuevosJugadores = jugadores.filter { it.id != jugadorId }
        val nuevoIndice = if (jugadorActual >= nuevosJugadores.size) 0 else jugadorActual
        return this.copy(jugadores = nuevosJugadores, jugadorActual = nuevoIndice)
    }
    
    fun cambiarEstado(nuevoEstado: EstadoJuego): Juego {
        when (estado) {
            EstadoJuego.ESPERANDO_JUGADORES -> require(nuevoEstado in listOf(EstadoJuego.EN_CURSO, EstadoJuego.CANCELADO))
            EstadoJuego.EN_CURSO -> require(nuevoEstado in listOf(EstadoJuego.PAUSADO, EstadoJuego.FINALIZADO, EstadoJuego.CANCELADO))
            EstadoJuego.PAUSADO -> require(nuevoEstado in listOf(EstadoJuego.EN_CURSO, EstadoJuego.CANCELADO))
            EstadoJuego.FINALIZADO, EstadoJuego.CANCELADO -> throw IllegalStateException("No se puede cambiar el estado desde $estado")
        }
        return this.copy(estado = nuevoEstado)
    }
    
    fun iniciarJuego(): Juego {
        require(jugadores.isNotEmpty()) { "No se puede iniciar un juego sin jugadores" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { "El juego debe estar esperando jugadores" }
        return this.copy(estado = EstadoJuego.EN_CURSO)
    }
    
    fun siguienteRonda(): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para avanzar ronda" }
        return this.copy(rondaActual = rondaActual + 1, jugadorActual = 0)
    }
}

/**
 * Enum para diferentes tipos de juego que requieren validaciones específicas
 */
enum class TipoJuego {
    GENERICO, TRES_EN_LINEA, AJEDREZ, DAMAS
}
