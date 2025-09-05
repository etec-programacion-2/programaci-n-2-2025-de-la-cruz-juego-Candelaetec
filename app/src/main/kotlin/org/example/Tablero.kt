package org.example

/**
 * Clase que representa el tablero del juego
 * 
 * Encapsula la gestión de una matriz de celdas y proporciona métodos
 * para interactuar con el tablero de forma segura.
 * 
 * @param filas Número de filas del tablero
 * @param columnas Número de columnas del tablero
 */
class Tablero(
    val filas: Int,
    val columnas: Int
) {
    // Estructura de datos: Lista de listas para representar la matriz
    private val celdas: List<MutableList<Celda>>
    
    // Inicialización del tablero
    init {
        require(filas > 0) { "El número de filas debe ser positivo" }
        require(columnas > 0) { "El número de columnas debe ser positivo" }
        
        // Crear la matriz de celdas
        celdas = List(filas) { fila ->
            MutableList(columnas) { columna ->
                Celda(fila = fila, columna = columna)
            }
        }
    }
    
    /**
     * Obtiene una celda específica del tablero
     * 
     * @param fila Posición de la fila (0-indexed)
     * @param columna Posición de la columna (0-indexed)
     * @return La celda en la posición especificada
     * @throws IndexOutOfBoundsException si las coordenadas están fuera del tablero
     */
    fun obtenerCelda(fila: Int, columna: Int): Celda {
        validarCoordenadas(fila, columna)
        return celdas[fila][columna]
    }
    
    /**
     * Coloca contenido en una celda específica
     * 
     * @param fila Posición de la fila
     * @param columna Posición de la columna
     * @param contenido Contenido a colocar en la celda
     * @throws IndexOutOfBoundsException si las coordenadas están fuera del tablero
     */
    fun colocarEnCelda(fila: Int, columna: Int, contenido: String) {
        validarCoordenadas(fila, columna)
        require(contenido.isNotBlank()) { "El contenido no puede estar vacío" }
        
        celdas[fila][columna] = celdas[fila][columna].colocarContenido(contenido)
    }
    
    /**
     * Vacía una celda específica
     * 
     * @param fila Posición de la fila
     * @param columna Posición de la columna
     */
    fun vaciarCelda(fila: Int, columna: Int) {
        validarCoordenadas(fila, columna)
        celdas[fila][columna] = celdas[fila][columna].vaciar()
    }
    
    /**
     * Verifica si una celda está vacía
     */
    fun estaVacia(fila: Int, columna: Int): Boolean {
        validarCoordenadas(fila, columna)
        return celdas[fila][columna].estaVacia
    }
    
    /**
     * Obtiene todas las celdas vacías del tablero
     */
    fun obtenerCeldasVacias(): List<Celda> {
        return celdas.flatten().filter { it.estaVacia }
    }
    
    /**
     * Obtiene todas las celdas ocupadas del tablero
     */
    fun obtenerCeldasOcupadas(): List<Celda> {
        return celdas.flatten().filter { !it.estaVacia }
    }
    
    /**
     * Obtiene una fila completa del tablero
     */
    fun obtenerFila(numeroFila: Int): List<Celda> {
        require(numeroFila in 0 until filas) { "Fila $numeroFila fuera de rango" }
        return celdas[numeroFila].toList()
    }
    
    /**
     * Obtiene una columna completa del tablero
     */
    fun obtenerColumna(numeroColumna: Int): List<Celda> {
        require(numeroColumna in 0 until columnas) { "Columna $numeroColumna fuera de rango" }
        return celdas.map { it[numeroColumna] }
    }
    
    /**
     * Limpia todo el tablero (vacía todas las celdas)
     */
    fun limpiarTablero() {
        for (fila in 0 until filas) {
            for (columna in 0 until columnas) {
                celdas[fila][columna] = celdas[fila][columna].vaciar()
            }
        }
    }
    
    /**
     * Cuenta el número de celdas ocupadas
     */
    fun contarCeldasOcupadas(): Int {
        return celdas.flatten().count { !it.estaVacia }
    }
    
    /**
     * Verifica si las coordenadas están dentro del tablero
     */
    fun coordenadasValidas(fila: Int, columna: Int): Boolean {
        return fila in 0 until filas && columna in 0 until columnas
    }
    
    /**
     * Función privada para validar coordenadas y lanzar excepción si son inválidas
     */
    private fun validarCoordenadas(fila: Int, columna: Int) {
        if (!coordenadasValidas(fila, columna)) {
            throw IndexOutOfBoundsException(
                "Coordenadas ($fila, $columna) fuera del tablero ${filas}x${columnas}"
            )
        }
    }
    
    /**
     * Representación visual simple del tablero para debugging
     */
    fun mostrarTablero(): String {
        val sb = StringBuilder()
        sb.append("Tablero ${filas}x${columnas}:\n")
        sb.append("   ")
        
        // Números de columna
        for (col in 0 until columnas) {
            sb.append("%2d ".format(col))
        }
        sb.append("\n")
        
        // Contenido del tablero
        for (fila in 0 until filas) {
            sb.append("%2d ".format(fila))
            for (columna in 0 until columnas) {
                val contenido = celdas[fila][columna].contenido ?: "."
                sb.append("%2s ".format(contenido.take(1)))
            }
            sb.append("\n")
        }
        
        return sb.toString()
    }
    
    override fun toString(): String {
        return "Tablero(${filas}x${columnas}, ocupadas: ${contarCeldasOcupadas()}/${filas * columnas})"
    }
}