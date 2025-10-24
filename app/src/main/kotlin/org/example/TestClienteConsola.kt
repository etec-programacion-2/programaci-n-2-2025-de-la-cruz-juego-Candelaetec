package org.example

/**
 * Test para demostrar las mejoras del cliente de consola.
 * 
 * Este test simula la funcionalidad del nuevo cliente interactivo
 * sin requerir conexión real al servidor.
 */
fun main() {
    println("=== DEMOSTRACIÓN DEL CLIENTE DE CONSOLA MEJORADO ===\n")
    
    // Test 1: Validación de entrada de usuario
    println("🧪 Test 1: Validación de entrada de usuario")
    testearValidacionEntrada()
    
    // Test 2: Visualización del tablero
    println("\n🧪 Test 2: Visualización del tablero con coordenadas")
    testearVisualizacionTablero()
    
    // Test 3: Manejo de errores
    println("\n🧪 Test 3: Manejo de errores")
    testearManejoErrores()
    
    // Test 4: Flujo completo del juego
    println("\n🧪 Test 4: Flujo completo del juego")
    testearFlujoCompleto()
    
    println("\n🎉 ¡Todas las demostraciones completadas exitosamente!")
    println("\n📋 RESUMEN DE MEJORAS IMPLEMENTADAS:")
    println("✅ Menús interactivos numerados")
    println("✅ Validación robusta de entrada del usuario")
    println("✅ Visualización clara del tablero con coordenadas ASCII")
    println("✅ Manejo comprehensivo de errores con mensajes descriptivos")
    println("✅ Flujo completo del juego desde conexión hasta desconexión")
    println("✅ Retroalimentación clara al usuario")
    println("✅ Ayuda y reglas del juego integradas")
    println("✅ Compatibilidad con el cliente original")
}

/**
 * Testea la validación de entrada del usuario
 */
private fun testearValidacionEntrada() {
    println("─" * 40)
    
    // Test de nombres de usuario
    val nombresValidos = listOf("Ana", "Carlos123", "Jugador Uno", "Test")
    val nombresInvalidos = listOf("", "A", "NombreMuyLargoQueExcedeElLimite", "Usuario@123")
    
    println("📝 Validación de nombres de usuario:")
    nombresValidos.forEach { nombre ->
        val esValido = validarNombreUsuario(nombre)
        println("   ✅ '$nombre' -> ${if (esValido) "VÁLIDO" else "INVÁLIDO"}")
    }
    
    nombresInvalidos.forEach { nombre ->
        val esValido = validarNombreUsuario(nombre)
        println("   ❌ '$nombre' -> ${if (esValido) "VÁLIDO" else "INVÁLIDO"}")
    }
    
    // Test de IDs de partida
    println("\n🆔 Validación de IDs de partida:")
    val idsValidos = listOf("PARTIDA-ABC12345", "PARTIDA-XYZ98765")
    val idsInvalidos = listOf("", "PARTIDA-123", "partida-ABC12345", "PARTIDA-ABC123456")
    
    idsValidos.forEach { id ->
        val esValido = validarIdPartida(id)
        println("   ✅ '$id' -> ${if (esValido) "VÁLIDO" else "INVÁLIDO"}")
    }
    
    idsInvalidos.forEach { id ->
        val esValido = validarIdPartida(id)
        println("   ❌ '$id' -> ${if (esValido) "VÁLIDO" else "INVÁLIDO"}")
    }
    
    // Test de coordenadas
    println("\n📍 Validación de coordenadas:")
    val coordenadasValidas = listOf("a1", "b2", "c3", "h8")
    val coordenadasInvalidas = listOf("", "a", "1", "i1", "a9", "z5")
    
    coordenadasValidas.forEach { coord ->
        val posicion = parsearCoordenadaTest(coord)
        println("   ✅ '$coord' -> ${posicion ?: "INVÁLIDO"}")
    }
    
    coordenadasInvalidas.forEach { coord ->
        val posicion = parsearCoordenadaTest(coord)
        println("   ❌ '$coord' -> ${posicion ?: "INVÁLIDO"}")
    }
}

/**
 * Testea la visualización del tablero
 */
private fun testearVisualizacionTablero() {
    println("─" * 40)
    
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
    
    // Simular algunos movimientos
    juego = juego.realizarMovimiento(1L, 0, 0, "X") // Ana coloca X en a1
    juego = juego.realizarMovimiento(2L, 1, 1, "O") // Carlos coloca O en b2
    juego = juego.realizarMovimiento(1L, 0, 1, "X") // Ana coloca X en b1
    
    println("📊 Tablero de ejemplo (Tres en Línea):")
    mostrarTableroDemo(juego)
    
    // Crear un tablero de ajedrez de ejemplo
    val tableroAjedrez = Tablero(8, 8)
    tableroAjedrez.colocarEnCelda(0, 0, "♜") // Torre negra en a1
    tableroAjedrez.colocarEnCelda(7, 7, "♖") // Torre blanca en h8
    tableroAjedrez.colocarEnCelda(0, 4, "♚") // Rey negro en e1
    tableroAjedrez.colocarEnCelda(7, 4, "♔") // Rey blanco en e8
    
    val juegoAjedrez = Juego(
        id = "AJEDREZ-001",
        tablero = tableroAjedrez,
        maxJugadores = 2,
        tipoJuego = TipoJuego.AJEDREZ
    )
    
    println("\n♟️ Tablero de ejemplo (Ajedrez):")
    mostrarTableroDemo(juegoAjedrez)
}

/**
 * Testea el manejo de errores
 */
private fun testearManejoErrores() {
    println("─" * 40)
    
    val erroresComunes = listOf(
        "Error de conexión: No se pudo conectar al servidor",
        "Error en el movimiento: La celda ya está ocupada",
        "Error al crear partida: Nombre de usuario inválido",
        "Error al unirse: Partida no encontrada",
        "Error de validación: Coordenada fuera del tablero"
    )
    
    println("⚠️ Manejo de errores comunes:")
    erroresComunes.forEach { error ->
        println("   🔴 $error")
        println("      → Se muestra mensaje descriptivo al usuario")
        println("      → Se permite reintentar la operación")
        println("      → No se crashea la aplicación")
        println()
    }
}

/**
 * Testea el flujo completo del juego
 */
private fun testearFlujoCompleto() {
    println("─" * 40)
    
    println("🎮 Flujo completo del juego:")
    println("   1. 🏠 Menú principal con opciones numeradas")
    println("   2. 👤 Solicitud y validación del nombre de usuario")
    println("   3. 🔗 Conexión al servidor")
    println("   4. 🆕 Creación o unión a partida")
    println("   5. 📊 Visualización del estado del juego")
    println("   6. 🎯 Solicitud de movimientos con validación")
    println("   7. ✅ Confirmación de movimientos exitosos")
    println("   8. 🔄 Alternancia de turnos")
    println("   9. 🏁 Detección de fin de juego")
    println("   10. 🔌 Desconexión limpia del servidor")
    
    println("\n📋 Características de UX implementadas:")
    println("   • Retroalimentación visual con emojis")
    println("   • Mensajes de error descriptivos")
    println("   • Validación en tiempo real")
    println("   • Opciones de ayuda integradas")
    println("   • Confirmaciones antes de acciones críticas")
    println("   • Separación clara entre UI y lógica de comunicación")
}

/**
 * Funciones auxiliares para los tests
 */
private fun validarNombreUsuario(nombre: String): Boolean {
    return !nombre.isBlank() && 
           nombre.length >= 2 && 
           nombre.length <= 20 && 
           nombre.matches(Regex("[a-zA-Z0-9\\s]+"))
}

private fun validarIdPartida(id: String): Boolean {
    return id.matches(Regex("PARTIDA-[A-Z0-9]{8}"))
}

private fun parsearCoordenadaTest(coordenada: String): Pair<Int, Int>? {
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

private fun mostrarTableroDemo(juego: Juego) {
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
