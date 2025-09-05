package org.example

/**
 * Data class que representa el estado completo de una partida con gestión avanzada de movimientos
 * 
 * @param id Identificador único de la partida
 * @param tablero El tablero donde se desarrolla el juego
 * @param jugadores Lista mutable de jugadores participando en la partida
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
    val jugadores: MutableList<Jugador> = mutableListOf(),
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
    // MÉTODOS MEJORADOS PARA GESTIÓN DE MOVIMIENTOS
    // ==========================================
    
    /**
     * Realiza un movimiento complejo en el tablero (con origen y destino)
     * 
     * @param jugador El jugador que realiza el movimiento
     * @param movimiento El movimiento a realizar
     * @return Nueva instancia del juego con el movimiento aplicado
     * @throws IllegalStateException Si el juego no está en curso
     * @throws IllegalArgumentException Si el movimiento no es válido
     */
    fun realizarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        // Validaciones básicas
        require(estado == EstadoJuego.EN_CURSO) { 
            "El juego debe estar en curso para realizar movimientos. Estado actual: $estado" 
        }
        
        require(jugadores.any { it.id == jugador.id }) { 
            "El jugador '${jugador.nombre}' (ID: ${jugador.id}) no está en esta partida" 
        }
        
        require(esturnoDelJugador(jugador)) { 
            "No es el turno del jugador '${jugador.nombre}'. Turno actual: ${jugadorEnTurno?.nombre}" 
        }
        
        // Validar el movimiento según las reglas del juego
        validarMovimiento(jugador, movimiento)
        
        // Ejecutar el movimiento
        ejecutarMovimiento(movimiento)
        
        // Retornar nueva instancia con el turno avanzado
        return this.copy(jugadorActual = calcularSiguienteJugador())
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con la implementación anterior
     * (para colocaciones simples como tres en línea)
     */
    fun realizarMovimiento(jugadorId: Long, fila: Int, columna: Int, contenido: String): Juego {
        val jugador = jugadores.find { it.id == jugadorId }
            ?: throw IllegalArgumentException("Jugador con ID $jugadorId no encontrado")
        
        val movimiento = Movimiento.colocacion(fila, columna, contenido)
        return realizarMovimiento(jugador, movimiento)
    }
    
    /**
     * Valida si un movimiento es legal según las reglas del juego
     */
    private fun validarMovimiento(jugador: Jugador, movimiento: Movimiento) {
        // Validar que las coordenadas estén dentro del tablero
        if (!movimiento.esColocacion) {
            require(tablero.coordenadasValidas(movimiento.filaOrigen, movimiento.columnaOrigen)) {
                "Coordenadas de origen (${movimiento.filaOrigen}, ${movimiento.columnaOrigen}) fuera del tablero"
            }
        }
        
        require(tablero.coordenadasValidas(movimiento.filaDestino, movimiento.columnaDestino)) {
            "Coordenadas de destino (${movimiento.filaDestino}, ${movimiento.columnaDestino}) fuera del tablero"
        }
        
        // Aplicar validaciones específicas según el tipo de juego
        when (tipoJuego) {
            TipoJuego.TRES_EN_LINEA -> validarMovimientoTresEnLinea(movimiento)
            TipoJuego.AJEDREZ -> validarMovimientoAjedrez(jugador, movimiento)
            TipoJuego.DAMAS -> validarMovimientoDamas(jugador, movimiento)
            TipoJuego.GENERICO -> validarMovimientoGenerico(movimiento)
        }
    }
    
    /**
     * Validaciones específicas para tres en línea
     */
    private fun validarMovimientoTresEnLinea(movimiento: Movimiento) {
        require(movimiento.esColocacion) { 
            "En tres en línea solo se pueden hacer colocaciones, no movimientos" 
        }
        
        require(tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
            "La celda (${movimiento.filaDestino}, ${movimiento.columnaDestino}) ya está ocupada"
        }
        
        require(movimiento.contenido != null && movimiento.contenido.isNotBlank()) {
            "Debe especificar el contenido a colocar (X u O)"
        }
    }
    
    /**
     * Validaciones específicas para ajedrez (simplificadas)
     */
    private fun validarMovimientoAjedrez(jugador: Jugador, movimiento: Movimiento) {
        require(!movimiento.esColocacion) { 
            "En ajedrez se deben mover piezas existentes" 
        }
        
        // Verificar que hay una pieza en el origen
        require(!tablero.estaVacia(movimiento.filaOrigen, movimiento.columnaOrigen)) {
            "No hay ninguna pieza en la posición de origen (${movimiento.filaOrigen}, ${movimiento.columnaOrigen})"
        }
        
        // Verificar que la pieza le pertenece al jugador (simplificado)
        val celdaOrigen = tablero.obtenerCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
        val piezaOrigen = celdaOrigen.contenido
        
        // En una implementación real, aquí validaríamos que la pieza pertenece al jugador
        // y que el movimiento es válido para ese tipo de pieza
        
        // Si hay una pieza en el destino, verificar que no sea del mismo jugador
        if (!tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
            // Aquí iría la lógica para verificar que se puede capturar la pieza
        }
    }
    
    /**
     * Validaciones específicas para damas
     */
    private fun validarMovimientoDamas(jugador: Jugador, movimiento: Movimiento) {
        require(!movimiento.esColocacion) { 
            "En damas se deben mover piezas existentes" 
        }
        
        require(!tablero.estaVacia(movimiento.filaOrigen, movimiento.columnaOrigen)) {
            "No hay ninguna pieza en la posición de origen"
        }
        
        require(movimiento.esDiagonal()) {
            "En damas solo se permiten movimientos diagonales"
        }
        
        require(tablero.estaVacia(movimiento.filaDestino, movimiento.columnaDestino)) {
            "El destino debe estar vacío (excepto para capturas)"
        }
    }
    
    /**
     * Validaciones genéricas
     */
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
    
    /**
     * Ejecuta el movimiento en el tablero
     */
    private fun ejecutarMovimiento(movimiento: Movimiento) {
        if (movimiento.esColocacion) {
            // Es una colocación simple
            tablero.colocarEnCelda(
                movimiento.filaDestino, 
                movimiento.columnaDestino, 
                movimiento.contenido!!
            )
        } else {
            // Es un movimiento con origen y destino
            val celdaOrigen = tablero.obtenerCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
            val contenidoAMover = celdaOrigen.contenido ?: ""
            
            // Vaciar la posición de origen
            tablero.vaciarCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
            
            // Colocar en el destino
            tablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, contenidoAMover)
        }
    }
    
    /**
     * Verifica si es el turno del jugador especificado
     */
    fun esturnoDelJugador(jugador: Jugador): Boolean {
        return jugadorEnTurno?.id == jugador.id
    }
    
    /**
     * Calcula el índice del siguiente jugador
     */
    private fun calcularSiguienteJugador(): Int {
        if (jugadores.isEmpty()) return 0
        
        var siguienteIndice = (jugadorActual + 1) % jugadores.size
        
        // Saltar jugadores desconectados
        var intentos = 0
        while (!jugadores[siguienteIndice].conectado && intentos < jugadores.size) {
            siguienteIndice = (siguienteIndice + 1) % jugadores.size
            intentos++
        }
        
        return siguienteIndice
    }
    
    /**
     * Avanza al siguiente turno manualmente
     */
    fun siguienteTurno(): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para avanzar turnos" }
        return this.copy(jugadorActual = calcularSiguienteJugador())
    }
    
    // ==========================================
    // MÉTODOS EXISTENTES CONSERVADOS
    // ==========================================
    
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
        return this.copy(rondaActual = 1, jugadorActual = 0)
    }
    
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
        val nuevoJugadorActual = if (jugadorActual >= nuevosJugadores.size) 0 else jugadorActual
        return this.copy(jugadores = nuevosJugadores, jugadorActual = nuevoJugadorActual)
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
        return this.copy(rondaActual = rondaActual + 1, jugadorActual = 0)
    }
}

/**
 * Enum para diferentes tipos de juego que requieren validaciones específicas
 */
enum class TipoJuego {
    GENERICO,      // Juego genérico con validaciones mínimas
    TRES_EN_LINEA, // Tres en línea (solo colocaciones)
    AJEDREZ,       // Ajedrez (movimientos complejos)
    DAMAS          // Damas (movimientos diagonales)
}