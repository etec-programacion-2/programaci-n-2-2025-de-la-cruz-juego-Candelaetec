package org.example

/**
 * Data class que representa una celda individual en el tablero
 * 
 * @param fila Posición de la fila (0-indexed)
 * @param columna Posición de la columna (0-indexed)
 * @param contenido Contenido de la celda (puede ser null si está vacía)
 */
data class Celda(
    val fila: Int,
    val columna: Int,
    val contenido: String? = null
) {
    // Validaciones en el init block
    init {
        require(fila >= 0) { "La fila debe ser mayor o igual a 0" }
        require(columna >= 0) { "La columna debe ser mayor o igual a 0" }
    }
    
    /**
     * Propiedad computada que indica si la celda está vacía
     */
    val estaVacia: Boolean
        get() = contenido == null
    
    /**
     * Función para colocar contenido en la celda
     */
    fun colocarContenido(nuevoContenido: String): Celda {
        require(nuevoContenido.isNotBlank()) { "El contenido no puede estar vacío" }
        return this.copy(contenido = nuevoContenido)
    }
    
    /**
     * Función para vaciar la celda
     */
    fun vaciar(): Celda {
        return this.copy(contenido = null)
    }
    
    /**
     * Representación en string más legible para debugging
     */
    override fun toString(): String {
        val estado = if (estaVacia) "vacía" else "con '${contenido}'"
        return "Celda(${fila},${columna}) - $estado"
    }
}