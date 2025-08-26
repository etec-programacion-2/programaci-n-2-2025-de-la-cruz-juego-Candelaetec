package org.example

/**
 * Data class que representa a un jugador en el sistema
 * 
 * @param id Identificador único del jugador (Long para soportar muchos usuarios)
 * @param nombre Nombre del jugador elegido por el usuario
 * @param puntuacion Puntuación actual del jugador (por defecto 0)
 * @param conectado Estado de conexión del jugador (por defecto true)
 */
data class Jugador(
    val id: Long,
    val nombre: String,
    val puntuacion: Int = 0,
    val conectado: Boolean = true
) {
    // Validación en el init block
    init {
        require(nombre.isNotBlank()) { "El nombre del jugador no puede estar vacío" }
        require(puntuacion >= 0) { "La puntuación no puede ser negativa" }
    }
    
    /**
     * Función para crear una copia del jugador con nueva puntuación
     */
    fun actualizarPuntuacion(nuevaPuntuacion: Int): Jugador {
        require(nuevaPuntuacion >= 0) { "La puntuación no puede ser negativa" }
        return this.copy(puntuacion = nuevaPuntuacion)
    }
    
    /**
     * Función para cambiar el estado de conexión
     */
    fun cambiarEstadoConexion(conectado: Boolean): Jugador {
        return this.copy(conectado = conectado)
    }
}