package org.example

fun main() {
    println("=== DEMO AVANZADO: MOTORJUEGO EN ACCIÓN ===\n")

    // ==========================================
    // 1. DEMO TRES EN LÍNEA CON MOTORJUEGO
    // ==========================================
    println("🎯 JUEGO: TRES EN LÍNEA (con MotorJuego)")
    println("=${"=".repeat(45)}")

    val jugador1 = Jugador(id = 1L, nombre = "Ana")
    val jugador2 = Jugador(id = 2L, nombre = "Carlos")

    var tresEnLinea = Juego(
        id = "TRES-EN-LINEA-001",
        filasTablero = 3,
        columnasTablero = 3,
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
        .agregarJugador(jugador1)
        .agregarJugador(jugador2)
        .iniciarJuego()

    val motorTresEnLinea = MotorJuego(tresEnLinea)

    println("Jugadores: ${tresEnLinea.jugadores.map { it.nombre }}")
    println("Turno inicial: ${motorTresEnLinea.determinarJugadorActual()?.nombre}")
    println("Tablero inicial:")
    println(tresEnLinea.verTablero())

    val movimientosTresEnLinea = listOf(
        Triple(jugador1, Movimiento.colocacion(0, 0, "X"), "Ana coloca X en (0,0)"),
        Triple(jugador2, Movimiento.colocacion(1, 1, "O"), "Carlos coloca O en (1,1)"),
        Triple(jugador1, Movimiento.colocacion(0, 1, "X"), "Ana coloca X en (0,1)"),
        Triple(jugador2, Movimiento.colocacion(2, 0, "O"), "Carlos coloca O en (2,0)"),
        Triple(jugador1, Movimiento.colocacion(0, 2, "X"), "Ana coloca X en (0,2) - ¡GANA!")
    )

    for ((jugador, movimiento, descripcion) in movimientosTresEnLinea) {
        println("\n👉 $descripcion")
        println("Turno de: ${motorTresEnLinea.determinarJugadorActual()?.nombre}")

        try {
            tresEnLinea = tresEnLinea.realizarMovimiento(jugador, movimiento)
            println("✅ Movimiento exitoso")
            println(tresEnLinea.verTablero())
            val motorActualizado = MotorJuego(tresEnLinea)
            println("Siguiente turno: ${motorActualizado.determinarJugadorActual()?.nombre}")
            println("Estado del juego: ${tresEnLinea.estado}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }

    // ==========================================
    // 2. DEMO ESTADÍSTICAS Y ESTADO ACTUAL
    // ==========================================
    val motorFinal = MotorJuego(tresEnLinea)
    println("\n📊 ESTADO FINAL DEL JUEGO")
    println(motorFinal.obtenerEstadoJuego())
    println("Estadísticas: ${motorFinal.obtenerEstadisticas()}")
    
    println("\n🏁 ¡Demo completado exitosamente con MotorJuego!")
}
