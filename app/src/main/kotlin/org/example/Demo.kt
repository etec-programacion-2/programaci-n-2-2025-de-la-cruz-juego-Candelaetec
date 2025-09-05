package org.example

fun main() {
    println("=== DEMO DEL TABLERO ===\n")
    
    // 1. Crear un tablero 3x3 para ejemplo simple
    val tablero = Tablero(filas = 3, columnas = 3)
    println("Tablero creado: $tablero")
    println("Dimensiones: ${tablero.filas}x${tablero.columnas}\n")
    
    // 2. Mostrar tablero vacío
    println("Tablero inicial (vacío):")
    println(tablero.mostrarTablero())
    
    // 3. Colocar algunas piezas
    println("Colocando piezas en el tablero...")
    tablero.colocarEnCelda(0, 0, "X")
    tablero.colocarEnCelda(1, 1, "O")
    tablero.colocarEnCelda(2, 2, "X")
    tablero.colocarEnCelda(0, 2, "O")
    
    println("Tablero con piezas:")
    println(tablero.mostrarTablero())
    
    // 4. Demostrar obtención de celdas específicas
    println("=== ACCESO A CELDAS ESPECÍFICAS ===")
    val celdaEspecifica = tablero.obtenerCelda(1, 1)
    println("Celda en posición (1,1): $celdaEspecifica")
    
    // 5. Verificar si una celda está vacía
    println("¿La celda (0,1) está vacía? ${tablero.estaVacia(0, 1)}")
    println("¿La celda (1,1) está vacía? ${tablero.estaVacia(1, 1)}")
    
    // 6. Obtener listas de celdas
    println("\n=== ANÁLISIS DEL TABLERO ===")
    println("Celdas vacías: ${tablero.obtenerCeldasVacias().size}")
    println("Celdas ocupadas: ${tablero.obtenerCeldasOcupadas().size}")
    
    // 7. Obtener fila y columna completas
    println("\nPrimera fila: ${tablero.obtenerFila(0)}")
    println("Segunda columna: ${tablero.obtenerColumna(1)}")
    
    // 8. Demostrar validación de coordenadas
    println("\n=== VALIDACIÓN DE COORDENADAS ===")
    println("¿Son válidas las coordenadas (1,1)? ${tablero.coordenadasValidas(1, 1)}")
    println("¿Son válidas las coordenadas (5,5)? ${tablero.coordenadasValidas(5, 5)}")
    
    // 9. Ejemplo de manejo de errores
    println("\n=== MANEJO DE ERRORES ===")
    try {
        tablero.obtenerCelda(10, 10) // Esto debería fallar
    } catch (e: IndexOutOfBoundsException) {
        println("Error capturado: ${e.message}")
    }
    
    // 10. Ejemplo con tablero más grande (como ajedrez)
    println("\n=== TABLERO DE AJEDREZ (8x8) ===")
    val tableroAjedrez = Tablero(8, 8)
    
    // Colocar algunas piezas de ajedrez
    tableroAjedrez.colocarEnCelda(0, 0, "♜") // Torre negra
    tableroAjedrez.colocarEnCelda(0, 7, "♜") // Torre negra
    tableroAjedrez.colocarEnCelda(7, 0, "♖") // Torre blanca
    tableroAjedrez.colocarEnCelda(7, 7, "♖") // Torre blanca
    tableroAjedrez.colocarEnCelda(3, 4, "♕") // Reina blanca
    
    println("Tablero de ajedrez: $tableroAjedrez")
    println(tableroAjedrez.mostrarTablero())
    
    // 11. Demostrar operaciones en lote
    println("=== ESTADÍSTICAS ===")
    println("Total de celdas: ${tableroAjedrez.filas * tableroAjedrez.columnas}")
    println("Celdas ocupadas: ${tableroAjedrez.contarCeldasOcupadas()}")
    println("Celdas vacías: ${tableroAjedrez.obtenerCeldasVacias().size}")
}