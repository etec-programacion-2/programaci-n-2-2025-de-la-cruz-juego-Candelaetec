/**
     * Función para agregar un jugador al juego con notificaciones
     */
    fun agregarJugador(jugador: Jugador): Juego {
        require(!estaLleno) { "El juego ya está lleno" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { 
            "No se pueden agregar jugadores cuando el juego está en estado: $estado" 
        }
        require(!jugadores.any { it.id == jugador.id }) { 
            "Ya existe un jugador con el ID: ${jugador.id}" 
        }
        
        val nuevosJugadores = jugadores + jugador
        val juegoActualizado = this.copy(jugadores = nuevosJugadores)
        
        // Notificar a los listeners
        juegoActualizado.notificarJugadorAgregado(jugador)
        
        return juegoActualizado
    }
    
    /**
     * Función para remover un jugador del juego con notificaciones
     */
    fun removerJugador(jugadorId: Long): Juego {
        val jugadorARemover = jugadores.find { it.id == jugadorId }
            ?: return this // Si no existe el jugador, no hay nada que hacer
        
        val nuevosJugadores = jugadores.filter { it.id != jugadorId }
        val nuevoJugadorActual = if (jugadorActual >= nuevosJugadores.size) 0 else jugadorActual
        val juegoActualizado = this.copy(jugadores = nuevosJugadores, jugadorActual = nuevoJugadorActual)
        
        // Notificar a los listeners
        juegoActualizado.notificarJugadorRemovido(jugadorARemover)
        
        return juegoActualizado
    }
    
    /**
     * Función para cambiar el estado del juego con notificaciones
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
        
        val estadoAnterior = this.estado
        val juegoActualizado = this.copy(estado = nuevoEstado)
        
        // Notificar cambio de estado
        juegoActualizado.notificarCambioEstado(estadoAnterior, nuevoEstado)
        
        // Notificar pausa/reanudación si es relevante
        if (nuevoEstado == EstadoJuego.PAUSADO) {
            juegoActualizado.notificarJuegoPausado(true)
        } else if (estadoAnterior == EstadoJuego.PAUSADO && nuevoEstado == EstadoJuego.EN_CURSO) {
            juegoActualizado.notificarJuegoPausado(false)
        }
        
        return juegoActualizado
    }
    
    /**
     * Función para avanzar a la siguiente ronda con notificaciones
     */
    fun siguienteRonda(): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para avanzar de ronda" }
        
        val nuevaRonda = rondaActual + 1
        val juegoActualizado = this.copy(rondaActual = nuevaRonda, jugadorActual = 0)
        
        // Notificar nueva ronda
        juegoActualizado.notificarNuevaRonda(nuevaRonda)
        
        return juegoActualizado
    }
    
    /**
     * Actualiza el estado de conexión de un jugador
     */
    fun actualizarConexionJugador(jugadorId: Long, conectado: Boolean): Juego {
        val jugadorIndex = jugadores.indexOfFirst { it.id == jugadorId }
        if (jugadorIndex == -1) return this
        
        val jugadorActual = jugadores[jugadorIndex]
        if (jugadorActual.conectado == conectado) return this // Sin cambios
        
        val jugadorActualizado = jugadorActual.cambiarEstadoConexion(conectado)
        val nuevosJugadores = jugadores.toMutableList().apply { this[jugadorIndex] = jugadorActualizado }
        val juegoActualizado = this.copy(jugadores = nuevosJugadores)
        
        // Notificar cambio de conexión
        juegoActualizado.notificarCambioConexion(jugadorActualizado, conectado)
        
        return juegoActualizado
    }
    
    // ==========================================
    // MÉTODOS AUXILIARES PARA DETERMINACIÓN DE GANADORES
    // ==========================================
    
    /**
     * Determina el ganador del juego basado en el estado del tablero
     */
    private fun determinarGanador(juego: Juego): Jugador? {
        when (juego.tipoJuego) {
            TipoJuego.TRES_EN_LINEA -> return determinarGanadorTresEnLinea(juego)
            TipoJuego.AJEDREZ -> return determinarGanadorAjedrez(juego)
            TipoJuego.DAMAS -> return determinarGanadorDamas(juego)
            TipoJuego.GENERICO -> return null // Juegos genéricos no tienen ganador específico
        }
    }
    
    /**
     * Determina la razón de finalización del juego
     */
    private fun determinarRazonFinalizacion(juego: Juego, ganador: Jugador?): String {
        return when {
            ganador != null -> "Victoria por cumplir condición de victoria"
            juego.tablero.obtenerCeldasVacias().isEmpty() -> "Empate - Tablero lleno"
            juego.jugadoresConectados.isEmpty() -> "Juego terminado - No hay jugadores conectados"
            else -> "Juego terminado por condición especial"
        }
    }
    
    /**
     * Lógica específica para determinar ganador en tres en línea
     */
    private fun determinarGanadorTresEnLinea(juego: Juego): Jugador? {
        val motor = MotorJuego(juego)
        
        // Verificar filas, columnas y diagonales
        for (fila in 0 until juego.tablero.filas) {
            val filaCompleta = juego.tablero.obtenerFila(fila)
            val contenidoLinea = obtenerContenidoLinea(filaCompleta)
            if (contenidoLinea != null) {
                return encontrarJugadorPorContenido(contenidoLinea)
            }
        }
        
        for (columna in 0 until juego.tablero.columnas) {
            val columnaCompleta = juego.tablero.obtenerColumna(columna)
            val contenidoLinea = obtenerContenidoLinea(columnaCompleta)
            if (contenidoLinea != null) {
                return encontrarJugadorPorContenido(contenidoLinea)
            }
        }
        
        // Verificar diagonales (solo para tableros cuadrados)
        if (juego.tablero.filas == juego.tablero.columnas) {
            // Diagonal principal
            val diagonalPrincipal = mutableListOf<Celda>()
            for (i in 0 until juego.tablero.filas) {
                diagonalPrincipal.add(juego.tablero.obtenerCelda(i, i))
            }
            val contenidoDiagonal1 = obtenerContenidoLinea(diagonalPrincipal)
            if (contenidoDiagonal1 != null) {
                return encontrarJugadorPorContenido(contenidoDiagonal1)
            }
            
            // Diagonal secundaria
            val diagonalSecundaria = mutableListOf<Celda>()
            for (i in 0 until juego.tablero.filas) {
                diagonalSecundaria.add(juego.tablero.obtenerCelda(i, juego.tablero.columnas - 1 - i))
            }
            val contenidoDiagonal2 = obtenerContenidoLinea(diagonalSecundaria)
            if (contenidoDiagonal2 != null) {
                return encontrarJugadorPorContenido(contenidoDiagonal2)
            }
        }
        
        return null
    }
    
    /**
     * Obtiene el contenido de una línea ganadora si existe
     */
    private fun obtenerContenidoLinea(linea: List<Celda>): String? {
        if (linea.isEmpty()) return null
        
        val primerContenido = linea[0].contenido
        if (primerContenido.isNullOrBlank()) return null
        
        return if (linea.all { !it.estaVacia && it.contenido == primerContenido }) {
            primerContenido
        } else null
    }
    
    /**
     * Encuentra un jugador basado en el contenido de sus piezas
     */
    private fun encontrarJugadorPorContenido(contenido: String): Jugador? {
        // En una implementación real, aquí habría lógica para mapear
        // contenido de piezas con jugadores específicos
        // Por simplicidad, retornamos el primer jugador para contenido "X"
        // y el segundo para contenido "O"
        return when (contenido) {
            "X" -> jugadores.getOrNull(0)
            "O" -> jugadores.getOrNull(1)
            else -> null
        }
    }
    
    /**
     * Determina ganador para ajedrez (implementación simplificada)
     */
    private fun determinarGanadorAjedrez(juego: Juego): Jugador? {
        val celdasOcupadas = juego.tablero.obtenerCeldasOcupadas()
        val reyesEncontrados = celdasOcupadas.filter { celda ->
            celda.contenido == "♚" || celda.contenido == "♔"
        }
        
        // Si solo queda un rey, el propietario de ese rey gana
        if (reyesEncontrados.size == 1) {
            val reyGanador = reyesEncontrados[0].contenido
            return when (reyGanador) {
                "♚" -> jugadores.getOrNull(0) // Rey negro - jugador 1
                "♔" -> jugadores.getOrNull(1) // Rey blanco - jugador 2
                else -> null
            }
        }
        
        return null
    }
    
    /**
     * Determina ganador para damas (implementación simplificada)
     */
    private fun determinarGanadorDamas(juego: Juego): Jugador? {
        val celdasOcupadas = juego.tablero.obtenerCeldasOcupadas()
        
        val piezasBlancas = celdasOcupadas.count { 
            it.contenido?.contains("♔") == true || it.contenido?.contains("♕") == true 
        }
        val piezasNegras = celdasOcupadas.count { 
            it.contenido?.contains("♚") == true || it.contenido?.contains("♛") == true 
        }
        
        return when {
            piezasBlancas == 0 -> jugadores.getOrNull(0) // Ganan las negras
            piezasNegras == 0 -> jugadores.getOrNull(1) // Ganan las blancas
            else -> null
        }
    }
     package org.example

/**
 * Data class que representa el estado completo de una partida con gestión avanzada de movimientos
 * y sistema de eventos integrado
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
 * @param listeners Lista de listeners suscritos a eventos del juego
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
    val tipoJuego: TipoJuego = TipoJuego.GENERICO,
    private val listeners: List<ListenerJuego> = listOf()
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
    // GESTIÓN DE LISTENERS (PATRÓN OBSERVER)
    // ==========================================
    
    /**
     * Agrega un listener para recibir notificaciones de eventos del juego
     * 
     * @param listener El listener a agregar
     * @return Nueva instancia del juego con el listener agregado
     */
    fun agregarListener(listener: ListenerJuego): Juego {
        return this.copy(listeners = listeners + listener)
    }
    
    /**
     * Remueve un listener de las notificaciones
     * 
     * @param listener El listener a remover
     * @return Nueva instancia del juego sin el listener
     */
    fun removerListener(listener: ListenerJuego): Juego {
        return this.copy(listeners = listeners - listener)
    }
    
    /**
     * Remueve todos los listeners
     * 
     * @return Nueva instancia del juego sin listeners
     */
    fun limpiarListeners(): Juego {
        return this.copy(listeners = emptyList())
    }
    
    /**
     * Obtiene la lista actual de listeners
     */
    fun obtenerListeners(): List<ListenerJuego> = listeners.toList()
    
    /**
     * Notifica a todos los listeners sobre un movimiento realizado
     */
    private fun notificarMovimiento(jugador: Jugador, movimiento: Movimiento) {
        listeners.forEach { listener ->
            try {
                listener.onMovimiento(this, jugador, movimiento)
            } catch (e: Exception) {
                // Log del error sin interrumpir el flujo del juego
                System.err.println("Error en listener durante onMovimiento: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre un cambio de turno
     */
    private fun notificarCambioTurno(jugadorAnterior: Jugador?, jugadorActual: Jugador?) {
        listeners.forEach { listener ->
            try {
                listener.onTurnoCambiado(this, jugadorAnterior, jugadorActual)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onTurnoCambiado: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre la finalización del juego
     */
    private fun notificarJuegoTerminado(ganador: Jugador?, razon: String) {
        listeners.forEach { listener ->
            try {
                listener.onJuegoTerminado(this, ganador, razon)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onJuegoTerminado: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre un cambio de estado
     */
    private fun notificarCambioEstado(estadoAnterior: EstadoJuego, estadoNuevo: EstadoJuego) {
        listeners.forEach { listener ->
            try {
                listener.onEstadoJuegoCambiado(this, estadoAnterior, estadoNuevo)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onEstadoJuegoCambiado: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre un error en movimiento
     */
    private fun notificarErrorMovimiento(jugador: Jugador, movimiento: Movimiento, error: Exception) {
        listeners.forEach { listener ->
            try {
                listener.onErrorMovimiento(this, jugador, movimiento, error)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onErrorMovimiento: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre cambios en conexión de jugadores
     */
    private fun notificarCambioConexion(jugador: Jugador, conectado: Boolean) {
        listeners.forEach { listener ->
            try {
                listener.onEstadoConexionCambiado(this, jugador, conectado)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onEstadoConexionCambiado: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre un jugador agregado
     */
    private fun notificarJugadorAgregado(jugador: Jugador) {
        listeners.forEach { listener ->
            try {
                listener.onJugadorAgregado(this, jugador)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onJugadorAgregado: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre un jugador removido
     */
    private fun notificarJugadorRemovido(jugador: Jugador) {
        listeners.forEach { listener ->
            try {
                listener.onJugadorRemovido(this, jugador)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onJugadorRemovido: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre una nueva ronda
     */
    private fun notificarNuevaRonda(numeroRonda: Int) {
        listeners.forEach { listener ->
            try {
                listener.onNuevaRonda(this, numeroRonda)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onNuevaRonda: ${e.message}")
            }
        }
    }
    
    /**
     * Notifica a todos los listeners sobre pausa/reanudación
     */
    private fun notificarJuegoPausado(pausado: Boolean) {
        listeners.forEach { listener ->
            try {
                listener.onJuegoPausado(this, pausado)
            } catch (e: Exception) {
                System.err.println("Error en listener durante onJuegoPausado: ${e.message}")
            }
        }
    }
    
    // ==========================================
    // MÉTODOS MEJORADOS CON NOTIFICACIONES
    // ==========================================
    
    /**
     * Realiza un movimiento usando el MotorJuego para validaciones y control de flujo
     * Incluye notificaciones a los listeners
     * 
     * @param jugador El jugador que realiza el movimiento
     * @param movimiento El movimiento a realizar
     * @return Nueva instancia del juego con el movimiento aplicado y estado validado
     */
    fun realizarMovimiento(jugador: Jugador, movimiento: Movimiento): Juego {
        try {
            val motor = MotorJuego(this)
            val jugadorAnterior = motor.determinarJugadorActual()
            
            // Procesar movimiento usando el motor
            val juegoActualizado = motor.procesarMovimiento(jugador, movimiento)
            
            // Copiar listeners al nuevo juego
            val juegoConListeners = juegoActualizado.copy(listeners = this.listeners)
            
            // Notificar movimiento realizado
            juegoConListeners.notificarMovimiento(jugador, movimiento)
            
            // Notificar cambio de turno si es necesario
            val motorActualizado = MotorJuego(juegoConListeners)
            val jugadorActual = motorActualizado.determinarJugadorActual()
            if (jugadorAnterior?.id != jugadorActual?.id) {
                juegoConListeners.notificarCambioTurno(jugadorAnterior, jugadorActual)
            }
            
            // Notificar si el juego terminó
            if (juegoConListeners.estado == EstadoJuego.FINALIZADO) {
                val ganador = determinarGanador(juegoConListeners)
                val razon = determinarRazonFinalizacion(juegoConListeners, ganador)
                juegoConListeners.notificarJuegoTerminado(ganador, razon)
            }
            
            return juegoConListeners
            
        } catch (e: Exception) {
            // Notificar error a los listeners
            notificarErrorMovimiento(jugador, movimiento, e)
            throw e // Re-lanzar la excepción
        }
    }
    
    /**
     * Método interno para ejecutar movimientos (usado por MotorJuego)
     * Mantiene las validaciones específicas del tipo de juego
     * 
     * @param jugador El jugador que realiza el movimiento
     * @param movimiento El movimiento a realizar
     * @return Nueva instancia del juego con solo el movimiento aplicado (sin validar turnos ni victoria)
     */
    internal fun ejecutarMovimientoInterno(jugador: Jugador, movimiento: Movimiento): Juego {
        // Validaciones básicas
        require(estado == EstadoJuego.EN_CURSO) { 
            "El juego debe estar en curso para realizar movimientos. Estado actual: $estado" 
        }
        
        require(jugadores.any { it.id == jugador.id }) { 
            "El jugador '${jugador.nombre}' (ID: ${jugador.id}) no está en esta partida" 
        }
        
        // Validar el movimiento según las reglas del juego
        validarMovimiento(jugador, movimiento)
        
        // Crear nuevo tablero con el movimiento ejecutado
        val nuevoTablero = ejecutarMovimiento(movimiento)
        
        // Retornar nueva instancia solo con el tablero actualizado
        return this.copy(tablero = nuevoTablero)
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
     * Obtiene el motor de juego para este juego
     * Útil para acceder a funcionalidades avanzadas de control
     */
    fun obtenerMotor(): MotorJuego {
        return MotorJuego(this)
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
     * Ejecuta el movimiento en el tablero y devuelve un nuevo tablero
     */
    private fun ejecutarMovimiento(movimiento: Movimiento): Tablero {
        // Crear nuevo tablero copiando el estado actual
        val nuevoTablero = Tablero(tablero.filas, tablero.columnas)
        
        // Copiar todas las celdas del tablero actual
        for (fila in 0 until tablero.filas) {
            for (columna in 0 until tablero.columnas) {
                val celdaActual = tablero.obtenerCelda(fila, columna)
                if (!celdaActual.estaVacia) {
                    nuevoTablero.colocarEnCelda(fila, columna, celdaActual.contenido!!)
                }
            }
        }
        
        // Ejecutar el movimiento específico
        if (movimiento.esColocacion) {
            // Es una colocación simple
            nuevoTablero.colocarEnCelda(
                movimiento.filaDestino, 
                movimiento.columnaDestino, 
                movimiento.contenido!!
            )
        } else {
            // Es un movimiento con origen y destino
            val celdaOrigen = tablero.obtenerCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
            val contenidoAMover = celdaOrigen.contenido ?: ""
            
            // Vaciar la posición de origen
            nuevoTablero.vaciarCelda(movimiento.filaOrigen, movimiento.columnaOrigen)
            
            // Colocar en el destino
            nuevoTablero.colocarEnCelda(movimiento.filaDestino, movimiento.columnaDestino, contenidoAMover)
        }
        
        return nuevoTablero
    }
    
    /**
     * Verifica si es el turno del jugador especificado (DEPRECATED - usar MotorJuego)
     * @deprecated Usar MotorJuego.esTurnoDelJugador() para mejor separación de responsabilidades
     */
    @Deprecated("Usar MotorJuego.esTurnoDelJugador()", ReplaceWith("obtenerMotor().esTurnoDelJugador(jugador)"))
    fun esturnoDelJugador(jugador: Jugador): Boolean {
        return obtenerMotor().esTurnoDelJugador(jugador)
    }
    
    /**
     * Calcula el índice del siguiente jugador (DEPRECATED - usar MotorJuego)
     * @deprecated Usar MotorJuego.calcularSiguienteJugador() para mejor separación de responsabilidades
     */
    @Deprecated("Usar MotorJuego.calcularSiguienteJugador()", ReplaceWith("obtenerMotor().calcularSiguienteJugador()"))
    private fun calcularSiguienteJugador(): Int {
        return obtenerMotor().calcularSiguienteJugador()
    }
    
    /**
     * Avanza al siguiente turno manualmente (DEPRECATED - usar MotorJuego)
     * @deprecated La gestión de turnos ahora es responsabilidad del MotorJuego
     */
    @Deprecated("La gestión de turnos es responsabilidad del MotorJuego")
    fun siguienteTurno(): Juego {
        require(estado == EstadoJuego.EN_CURSO) { "El juego debe estar en curso para avanzar turnos" }
        return this.copy(jugadorActual = obtenerMotor().calcularSiguienteJugador())
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
        val nuevoTablero = Tablero(tablero.filas, tablero.columnas)
        return this.copy(tablero = nuevoTablero, rondaActual = 1, jugadorActual = 0)
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
        
        val nuevosJugadores = jugadores + jugador
        return this.copy(jugadores = nuevosJugadores)
    }
    
    /**
     * Función para remover un jugador del juego
     */
    fun removerJugador(jugadorId: Long): Juego {
        val nuevosJugadores = jugadores.filter { it.id != jugadorId }
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
    
    // ==========================================
    // MÉTODOS EXISTENTES CONSERVADOS CON MEJORAS
    // ==========================================
    
    /**
     * Función para iniciar el juego con notificaciones
     */
    fun iniciarJuego(): Juego {
        require(jugadores.isNotEmpty()) { "No se puede iniciar un juego sin jugadores" }
        require(estado == EstadoJuego.ESPERANDO_JUGADORES) { 
            "El juego debe estar esperando jugadores para iniciarse" 
        }
        
        val estadoAnterior = this.estado
        val juegoActualizado = this.copy(estado = EstadoJuego.EN_CURSO)
        
        // Notificar cambio de estado
        juegoActualizado.notificarCambioEstado(estadoAnterior, EstadoJuego.EN_CURSO)
        
        // Notificar cambio de turno al primer jugador
        val motor = MotorJuego(juegoActualizado)
        val primerJugador = motor.determinarJugadorActual()
        juegoActualizado.notificarCambioTurno(null, primerJugador)
        
        return juegoActualizado
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