package org.example

/**
 * Data class que representa un movimiento en el tablero
 *
 * Encapsula las coordenadas de origen y destino de un movimiento,
 * proporcionando validaciones y métodos útiles para el manejo de movimientos.
 *
 * @param filaOrigen Fila de la posición de origen (0-indexed, o -1 si es colocación)
 * @param columnaOrigen Columna de la posición de origen (0-indexed, o -1 si es colocación)
 * @param filaDestino Fila de la posición de destino (0-indexed)
 * @param columnaDestino Columna de la posición de destino (0-indexed)
 * @param contenido Contenido/pieza que se está moviendo (opcional para algunos tipos de juego)
 */
data class Movimiento(
    val filaOrigen: Int,
    val columnaOrigen: Int,
    val filaDestino: Int,
    val columnaDestino: Int,
    val contenido: String? = null
) {
    // Validaciones en el init block
    init {
        require(filaDestino >= 0) { "La fila de destino debe ser mayor o igual a 0" }
        require(columnaDestino >= 0) { "La columna de destino debe ser mayor o igual a 0" }
        // origen puede ser -1 si es colocación
        require(filaOrigen >= -1) { "La fila de origen debe ser >= -1" }
        require(columnaOrigen >= -1) { "La columna de origen debe ser >= -1" }
    }

    /**
     * Propiedad que representa la posición de origen como un Pair
     */
    val posicionOrigen: Pair<Int, Int>
        get() = Pair(filaOrigen, columnaOrigen)

    /**
     * Propiedad que representa la posición de destino como un Pair
     */
    val posicionDestino: Pair<Int, Int>
        get() = Pair(filaDestino, columnaDestino)

    /**
     * Verifica si el movimiento es una colocación (sin origen válido)
     * Útil para juegos como tres en línea donde solo se colocan piezas
     */
    val esColocacion: Boolean
        get() = filaOrigen == -1 && columnaOrigen == -1

    /**
     * Calcula la distancia Manhattan entre origen y destino
     */
    fun distanciaManhattan(): Int {
        return if (esColocacion) 0
        else kotlin.math.abs(filaDestino - filaOrigen) + kotlin.math.abs(columnaDestino - columnaOrigen)
    }

    /**
     * Calcula la distancia Euclidiana entre origen y destino
     */
    fun distanciaEuclidiana(): Double {
        return if (esColocacion) 0.0
        else {
            val deltaFila = (filaDestino - filaOrigen).toDouble()
            val deltaColumna = (columnaDestino - columnaOrigen).toDouble()
            kotlin.math.sqrt(deltaFila * deltaFila + deltaColumna * deltaColumna)
        }
    }

    /**
     * Verifica si el movimiento es en diagonal
     */
    fun esDiagonal(): Boolean {
        return if (esColocacion) false
        else kotlin.math.abs(filaDestino - filaOrigen) == kotlin.math.abs(columnaDestino - columnaOrigen)
    }

    /**
     * Verifica si el movimiento es horizontal
     */
    fun esHorizontal(): Boolean {
        return if (esColocacion) false
        else filaOrigen == filaDestino && columnaOrigen != columnaDestino
    }

    /**
     * Verifica si el movimiento es vertical
     */
    fun esVertical(): Boolean {
        return if (esColocacion) false
        else columnaOrigen == columnaDestino && filaOrigen != filaDestino
    }

    /**
     * Verifica si origen y destino son la misma posición
     */
    fun esMovimientoNulo(): Boolean {
        return if (esColocacion) false
        else filaOrigen == filaDestino && columnaOrigen == columnaDestino
    }

    /**
     * Representación en string legible del movimiento
     */
    override fun toString(): String {
        return if (esColocacion) {
            "Colocación en (${filaDestino},${columnaDestino})" +
                    if (contenido != null) " con '$contenido'" else ""
        } else {
            "Movimiento de (${filaOrigen},${columnaOrigen}) a (${filaDestino},${columnaDestino})" +
                    if (contenido != null) " con '$contenido'" else ""
        }
    }

    companion object {
        /**
         * Factory method para crear un movimiento de solo colocación
         * (útil para juegos como tres en línea)
         */
        fun colocacion(fila: Int, columna: Int, contenido: String): Movimiento {
            return Movimiento(
                filaOrigen = -1,
                columnaOrigen = -1,
                filaDestino = fila,
                columnaDestino = columna,
                contenido = contenido
            )
        }

        /**
         * Factory method para crear un movimiento completo
         */
        fun mover(
            filaOrigen: Int, columnaOrigen: Int,
            filaDestino: Int, columnaDestino: Int,
            contenido: String? = null
        ): Movimiento {
            return Movimiento(filaOrigen, columnaOrigen, filaDestino, columnaDestino, contenido)
        }
    }
}
