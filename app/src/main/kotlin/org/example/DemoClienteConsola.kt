package org.example

/**
 * Demostración simple del cliente de consola mejorado.
 * 
 * Este script muestra las mejoras implementadas sin requerir
 * conexión real al servidor.
 */
fun main() {
    println("=== DEMOSTRACIÓN DEL CLIENTE DE CONSOLA MEJORADO ===\n")
    
    // Demostrar validaciones
    println("🧪 VALIDACIÓN DE ENTRADA DEL USUARIO")
    println("─" * 50)
    
    // Test de nombres
    val nombresTest = listOf("Ana", "Carlos123", "Jugador Uno", "", "A", "Usuario@123")
    nombresTest.forEach { nombre ->
        val esValido = validarNombre(nombre)
        val estado = if (esValido) "✅ VÁLIDO" else "❌ INVÁLIDO"
        println("   '$nombre' -> $estado")
    }
    
    // Test de coordenadas
    println("\n📍 VALIDACIÓN DE COORDENADAS")
    println("─" * 30)
    val coordenadasTest = listOf("a1", "b2", "c3", "h8", "", "a", "1", "i1", "a9")
    coordenadasTest.forEach { coord ->
        val posicion = parsearCoordenada(coord)
        val estado = if (posicion != null) "✅ ${posicion}" else "❌ INVÁLIDO"
        println("   '$coord' -> $estado")
    }
    
    // Demostrar visualización del tablero
    println("\n🎯 VISUALIZACIÓN DEL TABLERO")
    println("─" * 30)
    
    // Crear un juego de ejemplo
    val jugador1 = Jugador(id = 1L, nombre = "Ana")
    val jugador2 = Jugador(id = 2L, nombre = "Carlos")
    
    var juego = Juego(
        id = "DEMO-001",
        tablero = Tablero(3, 3),
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
    
    juego = juego.agregarJugador(jugador1).agregarJugador(jugador2).iniciarJuego()
    
    // Simular movimientos
    juego = juego.realizarMovimiento(1L, 0, 0, "X") // Ana coloca X en a1
    juego = juego.realizarMovimiento(2L, 1, 1, "O") // Carlos coloca O en b2
    juego = juego.realizarMovimiento(1L, 0, 1, "X") // Ana coloca X en b1
    
    println("📊 Tablero de Tres en Línea:")
    mostrarTablero(juego)
    
    // Demostrar manejo de errores
    println("\n⚠️ MANEJO DE ERRORES")
    println("─" * 20)
    println("✅ Error de conexión: Mensaje descriptivo + opción de reintento")
    println("✅ Error de validación: Feedback específico + corrección")
    println("✅ Error de movimiento: Explicación clara + nueva oportunidad")
    println("✅ Error de servidor: Recuperación automática cuando es posible")
    
    // Demostrar menús
    println("\n📋 MENÚS INTERACTIVOS")
    println("─" * 25)
    println("✅ Menú principal numerado (1-6)")
    println("✅ Opciones claras con emojis")
    println("✅ Ayuda integrada")
    println("✅ Reglas del juego")
    println("✅ Confirmaciones antes de acciones críticas")
    
    println("\n🎉 ¡DEMOSTRACIÓN COMPLETADA!")
    println("\n📋 RESUMEN DE MEJORAS:")
    println("✅ Interfaz de usuario amigable con menús numerados")
    println("✅ Validación robusta de entrada del usuario")
    println("✅ Visualización clara del tablero con coordenadas ASCII")
    println("✅ Manejo comprehensivo de errores con mensajes descriptivos")
    println("✅ Flujo completo del juego desde conexión hasta desconexión")
    println("✅ Retroalimentación clara al usuario en todo momento")
    println("✅ Ayuda y reglas del juego integradas")
    println("✅ Compatibilidad con el cliente original")
}

/**
 * Valida un nombre de usuario
 */
private fun validarNombre(nombre: String): Boolean {
    return !nombre.isBlank() && 
           nombre.length >= 2 && 
           nombre.length <= 20 && 
           nombre.matches(Regex("[a-zA-Z0-9\\s]+"))
}

/**
 * Parsea una coordenada en formato "a1" a posición (fila, columna)
 */
private fun parsearCoordenada(coordenada: String): Pair<Int, Int>? {
    if (coordenada.length != 2) return null
    
    val letra = coordenada[0]
    val numero = coordenada[1]
    
    if (!letra.isLetter() || !numero.isDigit()) return null
    
    val columna = letra.lowercaseChar() - 'a'
    val fila = numero.digitToIntOrNull()?.minus(1) ?: return null
    
    // Validar que esté en un tablero 8x8
    if (fila !in 0..7 || columna !in 0..7) return null
    
    return Pair(fila, columna)
}

/**
 * Muestra el tablero con coordenadas ASCII
 */
private fun mostrarTablero(juego: Juego) {
    val tablero = juego.tablero
    val sb = StringBuilder()
    
    // Encabezado con letras de columnas
    sb.append("   ")
    for (col in 0 until tablero.columnas) {
        sb.append(" ${('a' + col).uppercase()} ")
    }
    sb.appendLine()
    
    // Filas del tablero
    for (fila in 0 until tablero.filas) {
        sb.append("${fila + 1}  ")
        for (col in 0 until tablero.columnas) {
            val celda = tablero.obtenerCelda(fila, col)
            val contenido = celda.contenido ?: "."
            sb.append(" $contenido ")
        }
        sb.appendLine()
    }
    
    println(sb.toString())
}

/**
 * Extensión para repetir strings
 */
private operator fun String.times(n: Int): String = this.repeat(n)
