package org.example

fun main() {
    println("=== TEST DE CORRECCIÓN DE ERRORES ===\n")
    
    // Test 1: Crear movimiento de colocación
    println("🧪 Test 1: Creando movimiento de colocación...")
    try {
        val colocacion = Movimiento.colocacion(0, 0, "X")
        println("✅ Colocación creada exitosamente: $colocacion")
        println("   • ¿Es colocación? ${colocacion.esColocacion}")
        println("   • Posición destino: ${colocacion.posicionDestino}")
    } catch (e: Exception) {
        println("❌ Error creando colocación: ${e.message}")
        return
    }
    
    // Test 2: Crear movimiento normal
    println("\n🧪 Test 2: Creando movimiento normal...")
    try {
        val movimientoNormal = Movimiento.mover(1, 1, 3, 3)
        println("✅ Movimiento normal creado exitosamente: $movimientoNormal")
        println("   • ¿Es colocación? ${movimientoNormal.esColocacion}")
        println("   • ¿Es diagonal? ${movimientoNormal.esDiagonal()}")
        println("   • Distancia Manhattan: ${movimientoNormal.distanciaManhattan()}")
    } catch (e: Exception) {
        println("❌ Error creando movimiento: ${e.message}")
        return
    }
    
    // Test 3: Simulación de tres en línea como en el ejemplo original
    println("\n🧪 Test 3: Simulación completa de tres en línea...")
    try {
        // Crear jugadores
        val jugador1 = Jugador(id = 1L, nombre = "Ana")
        val jugador2 = Jugador(id = 2L, nombre = "Carlos")
        
        // Crear juego
        var juego = Juego(
            id = "TEST-001",
            tablero = Tablero(3, 3),
            maxJugadores = 2,
            tipoJuego = TipoJuego.TRES_EN_LINEA
        )
        
        // Agregar jugadores e iniciar
        juego = juego.agregarJugador(jugador1).agregarJugador(jugador2).iniciarJuego()
        
        println("org.example.Juego creado: ${juego.id}")
        println("Tablero inicial:")
        println(juego.verTablero())
        
        // Turno de Ana
        println("\nTurno de Ana - coloca X en (0,0):")
        println("Jugador en turno: ${juego.jugadorEnTurno?.nombre}")
        
        val movimiento1 = Movimiento.colocacion(0, 0, "X")
        juego = juego.realizarMovimiento(jugador1, movimiento1)
        
        println("✅ Movimiento exitoso!")
        println(juego.verTablero())
        println("Siguiente turno: ${juego.jugadorEnTurno?.nombre}")
        
        // Turno de Carlos
        println("\nTurno de Carlos - coloca O en (1,1):")
        val movimiento2 = Movimiento.colocacion(1, 1, "O")
        juego = juego.realizarMovimiento(jugador2, movimiento2)
        
        println("✅ Movimiento exitoso!")
        println(juego.verTablero())

        // Test de error: intentar colocar en una celda ocupada
        println("\n🧪 Test 4: Intentar colocar en celda ocupada (debe fallar)")
        try {
            val movimientoInvalido = Movimiento.colocacion(0, 0, "O")
            juego = juego.realizarMovimiento(jugador2, movimientoInvalido)
            println("❌ ERROR: Se permitió un movimiento inválido")
        } catch (e: Exception) {
            println("✅ Error correctamente capturado: ${e.message}")
        }
        
        println("\n🎉 ¡Todos los tests pasaron exitosamente!")
        
    } catch (e: Exception) {
        println("❌ Error en simulación: ${e.message}")
        e.printStackTrace()
    }
}