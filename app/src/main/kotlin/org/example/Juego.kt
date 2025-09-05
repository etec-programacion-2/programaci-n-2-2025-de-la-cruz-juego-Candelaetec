package org.example

/**
 * Data class que representa el estado completo de una partida
 * 
 * @param id Identificador único de la partida
 * @param tablero El tablero donde se desarrolla el juego
 * @param jugadores Lista mutable de jugadores participando en la partida
 * @param estado Estado actual de la partida
 * @param fechaCreacion Timestamp de cuándo se creó la partida
 * @param maxJugadores Número máximo de jugadores permitidos (por defecto 4)
 * @param rondaActual Número de la ronda actual (por defecto 1)
 */
data class Juego(
    val id: String,
    val tablero: Tablero,  // ← NUEVA PROPIEDAD: El tablero del juego
    val jugadores: MutableList<Jugador> = mutableListOf(),
    val estado: EstadoJuego = EstadoJuego.ESPERANDO_JUGADORES,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val maxJugadores: Int = 4,
    val rondaActual: Int = 1
) {
    // Constructor secundario para crear juegos con tablero estándar
    constructor(
        id: String,
        filasTablero: Int = 8,
        columnasTablero: Int = 8,
        maxJugadores: Int = 4
    ) : this(
        id = id,
        tablero = Tablero(filasTablero, columnasTablero),
        maxJugadores = maxJugadores
    )
    
    // Validaciones en el init block
    init {
        require(id.isNotBlank()) { "El ID del juego no puede estar vacío" }
        require(maxJugadores > 0) { "El número máximo de jugadores debe ser positivo" }
        require(rondaActual > 0) { "La ronda actual debe ser positiva" }
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
    
    // ==========================================
    // MÉTODOS PARA INTERACTUAR CON EL TABLERO
    // ==========================================
    
    /**
     * Realizar un movimiento en el tablero
     */
    fun realizarMovimiento(jugadorId: Long, fila: Int, columna: Int, contenido: String): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para realizar movimientos" }
        require(jugadores.any { it.id == jugadorId }) { "El jugador $jugadorId no está en esta partida" }
        require(tablero.estaVacia(fila, columna)) { "La celda ($fila, $columna) ya está ocupada" }
        
        tablero.colocarEnCelda(fila, columna, contenido)
        return this // El tablero se modifica in-place, pero podríamos retornar una copia si fuera immutable
    }
    
    /**
     * Obtener el estado actual del tablero como string
     */
    fun verTablero(): String {
        return tablero.mostrarTablero()
    }
    
    /**
     * Verificar si una posición específica está disponible
     */
    fun posicionDisponible(fila: Int, columna: Int): Boolean {
        return tablero.coordenadasValidas(fila, columna) && tablero.estaVacia(fila, columna)
    }
    
    /**
     * Obtener todas las posiciones vacías disponibles
     */
    fun posicionesDisponibles(): List<Pair<Int, Int>> {
        return tablero.obtenerCeldasVacias().map { Pair(it.fila, it.columna) }
    }
    
    /**
     * Limpiar el tablero (útil para reiniciar partida)
     */
    fun reiniciarTablero(): Juego {
        tablero.limpiarTablero()
        return this.copy(rondaActual = 1)
    }
    
    // ==========================================
    // MÉTODOS EXISTENTES DE GESTIÓN DE JUGADORES
    // ==========================================
    
    /**
     * Función para agregar un jugador al juego
     */
    fun agregarJugador(jugador: Jugador): Juego {
        require(!estaLleno) { "El juego ya está lleno" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { 
            "No se pueden agregar jugadores cuando el juego está en estado: $estado" 
        }
        require(!jugadores.any { it.id == jugador.id }) { 
            "Ya existe un jugador con el ID: ${jugador.id}" 
        }
        
        val nuevosJugadores = jugadores.toMutableList().apply { add(jugador) }
        return this.copy(jugadores = nuevosJugadores)
    }
    
    /**
     * Función para remover un jugador del juego
     */
    fun removerJugador(jugadorId: Long): Juego {
        val nuevosJugadores = jugadores.filter { it.id != jugadorId }.toMutableList()
        return this.copy(jugadores = nuevosJugadores)
    }
    
    /**
     * Función para cambiar el estado del juego
     */
    fun cambiarEstado(nuevoEstado: EstadoJuego): Juego {
        when (estado) {
            EstadoJuego.ESPERANDO_JUGADORES -> {
                require(
                    nuevoEstado in listOf(EstadoJuego.EN_CURSO, EstadoJuego.CANCELADO)
                ) { "Transición inválida de $estado a $nuevoEstado" }
            }
            EstadoJuego.EN_CURSO -> {
                require(
                    nuevoEstado in listOf(EstadoJuego.PAUSADO, EstadoJuego.FINALIZADO, EstadoJuego.CANCELADO)
                ) { "Transición inválida de $estado a $nuevoEstado" }
            }
            EstadoJuego.PAUSADO -> {
                require(
                    nuevoEstado in listOf(EstadoJuego.EN_CURSO, EstadoJuego.CANCELADO)
                ) { "Transición inválida de $estado a $nuevoEstado" }
            }
            EstadoJuego.FINALIZADO, EstadoJuego.CANCELADO -> {
                throw IllegalStateException("No se puede cambiar el estado desde $estado")
            }
        }
        
        return this.copy(estado = nuevoEstado)
    }
    
    /**
     * Función para iniciar el juego
     */
    fun iniciarJuego(): Juego {
        require(jugadores.isNotEmpty()) { "No se puede iniciar un juego sin jugadores" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { 
            "El juego debe estar esperando jugadores para iniciarse" 
        }
        
        return this.copy(estado = EstadoJuego.EN_CURSO)
    }
    
    /**
     * Función para avanzar a la siguiente ronda
     */
    fun siguienteRonda(): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para avanzar de ronda" }
        return this.copy(rondaActual = rondaActual + 1)
    }
}