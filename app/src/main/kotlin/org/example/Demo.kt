package org.example

fun main() {
    println("=== DEMO AVANZADO: GESTIÓN DE MOVIMIENTOS ===\n")
    
    // ==========================================
    // 1. DEMO TRES EN LÍNEA CON TURNOS
    // ==========================================
    println("🎯 JUEGO: TRES EN LÍNEA")
    println("=" * 40)
    
    // Crear jugadores
    val jugador1 = Jugador(id = 1L, nombre = "Ana")
    val jugador2 = Jugador(id = 2L, nombre = "Carlos")
    
    // Crear juego de tres en línea
    var tresEnLinea = Juego(
        id = "TRES-EN-LINEA-001",
        filasTablero = 3,
        columnasTablero = 3,
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
    
    // Agregar jugadores e iniciar
    tresEnLinea = tresEnLinea.agregarJugador(jugador1).agregarJugador(jugador2).iniciarJuego()
    
    println("Jugadores: ${tresEnLinea.jugadores.map { it.nombre }}")
    println("Turno inicial: ${tresEnLinea.jugadorEnTurno?.nombre}")
    println("\nTablero inicial:")
    println(tresEnLinea.verTablero())
    
    // Simular partida completa con gestión de turnos
    val movimientosTresEnLinea = listOf(
        Triple(jugador1, Movimiento.colocacion(0, 0, "X"), "Ana coloca X en (0,0)"),
        Triple(jugador2, Movimiento.colocacion(1, 1, "O"), "Carlos coloca O en (1,1)"),
        Triple(jugador1, Movimiento.colocacion(0, 1, "X"), "Ana coloca X en (0,1)"),
        Triple(jugador2, Movimiento.colocacion(2, 0, "O"), "Carlos coloca O en (2,0)"),
        Triple(jugador1, Movimiento.colocacion(0, 2, "X"), "Ana coloca X en (0,2) - ¡GANA!")
    )
    
    for ((jugador, movimiento, descripcion) in movimientosTresEnLinea) {
        println("\n$descripcion")
        println("Turno de: ${tresEnLinea.jugadorEnTurno?.nombre}")
        
        try {
            tresEnLinea = tresEnLinea.realizarMovimiento(jugador, movimiento)
            println("✅ Movimiento exitoso")
            println(tresEnLinea.verTablero())
            println("Siguiente turno: ${tresEnLinea.jugadorEnTurno?.nombre}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    
    // ==========================================
    // 2. DEMO AJEDREZ CON MOVIMIENTOS COMPLEJOS
    // ==========================================
    println("\n🏰 JUEGO: AJEDREZ")
    println("=" * 40)
    
    val jugador3 = Jugador(id = 3L, nombre = "María")
    val jugador4 = Jugador(id = 4L, nombre = "Luis")
    
    var ajedrez = Juego(
        id = "AJEDREZ-001",
        filasTablero = 8,
        columnasTablero = 8,
        maxJugadores = 2,
        tipoJuego = TipoJuego.AJEDREZ
    )
    
    ajedrez = ajedrez.agregarJugador(jugador3).agregarJugador(jugador4).iniciarJuego()
    
    // Primero colocamos algunas piezas manualmente para el demo
    println("Configurando tablero inicial con algunas piezas...")
    ajedrez.tablero.colocarEnCelda(0, 0, "♜") // Torre negra
    ajedrez.tablero.colocarEnCelda(0, 4, "♚") // Rey negro
    ajedrez.tablero.colocarEnCelda(7, 0, "♖") // Torre blanca
    ajedrez.tablero.colocarEnCelda(7, 4, "♔") // Rey blanco
    ajedrez.tablero.colocarEnCelda(1, 1, "♟") // Peón negro
    
    println(ajedrez.verTablero())
    
    // Movimientos de ajedrez
    val movimientosAjedrez = listOf(
        Triple(
            jugador3, 
            Movimiento.mover(1, 1, 2, 1), 
            "María mueve peón negro de (1,1) a (2,1)"
        ),
        Triple(
            jugador4, 
            Movimiento.mover(7, 0, 7, 3), 
            "Luis mueve torre blanca de (7,0) a (7,3)"
        ),
        Triple(
            jugador3, 
            Movimiento.mover(0, 0, 0, 3), 
            "María mueve torre negra de (0,0) a (0,3)"
        )
    )
    
    for ((jugador, movimiento, descripcion) in movimientosAjedrez) {
        println("\n$descripcion")
        println("Turno de: ${ajedrez.jugadorEnTurno?.nombre}")
        
        try {
            ajedrez = ajedrez.realizarMovimiento(jugador, movimiento)
            println("✅ Movimiento exitoso")
            println(ajedrez.verTablero())
            println("Siguiente turno: ${ajedrez.jugadorEnTurno?.nombre}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    
    // ==========================================
    // 3. DEMO VALIDACIONES Y MANEJO DE ERRORES
    // ==========================================
    println("\n🚫 DEMO: VALIDACIONES Y MANEJO DE ERRORES")
    println("=" * 50)
    
    // Intentar movimientos inválidos
    val movimientosInvalidos = listOf(
        Triple(
            jugador4, // No es su turno (es turno de jugador3)
            Movimiento.mover(7, 4, 7, 5),
            "Intentar mover fuera de turno"
        ),
        Triple(
            jugador3,
            Movimiento.mover(5, 5, 6, 6), // No hay pieza en el origen
            "Intentar mover desde casilla vacía"
        ),
        Triple(
            jugador3,
            Movimiento.mover(0, 3, 10, 10), // Coordenadas fuera del tablero
            "Intentar mover fuera del tablero"
        ),
        Triple(
            jugador3,
            Movimiento.colocacion(3, 3, "X"), // Colocación en juego de ajedrez
            "Intentar colocación en ajedrez"
        )
    )
    
    for ((jugador, movimiento, descripcion) in movimientosInvalidos) {
        println("\n🧪 Test: $descripcion")
        try {
            ajedrez = ajedrez.realizarMovimiento(jugador, movimiento)
            println("⚠️ ¡Esto no debería haber funcionado!")
        } catch (e: Exception) {
            println("✅ Error correctamente capturado: ${e.message}")
        }
    }
    
    // ==========================================
    // 4. DEMO ANÁLISIS DE MOVIMIENTOS
    // ==========================================
    println("\n📊 DEMO: ANÁLISIS DE MOVIMIENTOS")
    println("=" * 40)
    
    val movimientosParaAnalisis = listOf(
        Movimiento.mover(0, 0, 7, 7), // Diagonal larga
        Movimiento.mover(3, 3, 3, 7), // Horizontal
        Movimiento.mover(2, 1, 6, 1), // Vertical
        Movimiento.mover(4, 4, 4, 4), // Movimiento nulo
        Movimiento.colocacion(5, 5, "★") // Colocación
    )
    
    for (movimiento in movimientosParaAnalisis) {
        println("\n🔍 Analizando: $movimiento")
        println("   • ¿Es colocación? ${movimiento.esColocacion}")
        if (!movimiento.esColocacion) {
            println("   • ¿Es diagonal? ${movimiento.esDiagonal()}")
            println("   • ¿Es horizontal? ${movimiento.esHorizontal()}")
            println("   • ¿Es vertical? ${movimiento.esVertical()}")
            println("   • ¿Es movimiento nulo? ${movimiento.esMovimientoNulo()}")
            println("   • Distancia Manhattan: ${movimiento.distanciaManhattan()}")
            println("   • Distancia Euclidiana: ${String.format("%.2f", movimiento.distanciaEuclidiana())}")
        }
    }
    
    // ==========================================
    // 5. DEMO JUEGO GENÉRICO
    // ==========================================
    println("\n🎲 DEMO: JUEGO GENÉRICO")
    println("=" * 30)
    
    val jugador5 = Jugador(id = 5L, nombre = "Pedro")
    val jugador6 = Jugador(id = 6L, nombre = "Sofia")
    
    var juegoGenerico = Juego(
        id = "GENERICO-001",
        filasTablero = 5,
        columnasTablero = 5,
        maxJugadores = 2,
        tipoJuego = TipoJuego.GENERICO
    )
    
    juegoGenerico = juegoGenerico.agregarJugador(jugador5).agregarJugador(jugador6).iniciarJuego()
    
    // Colocar algunas piezas para poder moverlas
    juegoGenerico.tablero.colocarEnCelda(0, 0, "🔴")
    juegoGenerico.tablero.colocarEnCelda(4, 4, "🔵")
    
    println("Configuración inicial:")
    println(juegoGenerico.verTablero())
    
    // Realizar algunos movimientos genéricos
    val movimientosGenericos = listOf(
        Triple(
            jugador5,
            Movimiento.mover(0, 0, 2, 2),
            "Pedro mueve pieza roja"
        ),
        Triple(
            jugador6,
            Movimiento.mover(4, 4, 1, 1),
            "Sofia mueve pieza azul"
        ),
        Triple(
            jugador5,
            Movimiento.colocacion(3, 3, "🟡"),
            "Pedro coloca nueva pieza amarilla"
        )
    )
    
    for ((jugador, movimiento, descripcion) in movimientosGenericos) {
        println("\n$descripcion")
        try {
            juegoGenerico = juegoGenerico.realizarMovimiento(jugador, movimiento)
            println("✅ Movimiento exitoso")
            println(juegoGenerico.verTablero())
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    
    // ==========================================
    // 6. DEMO GESTIÓN DE TURNOS AVANZADA
    // ==========================================
    println("\n🔄 DEMO: GESTIÓN AVANZADA DE TURNOS")
    println("=" * 40)
    
    val jugadorMultiple1 = Jugador(id = 10L, nombre = "Jugador1")
    val jugadorMultiple2 = Jugador(id = 11L, nombre = "Jugador2")
    val jugadorMultiple3 = Jugador(id = 12L, nombre = "Jugador3")
    val jugadorMultiple4 = Jugador(id = 13L, nombre = "Jugador4")
    
    var juegoMultiple = Juego(
        id = "MULTI-001",
        filasTablero = 4,
        columnasTablero = 4,
        maxJugadores = 4,
        tipoJuego = TipoJuego.GENERICO
    )
    
    juegoMultiple = juegoMultiple
        .agregarJugador(jugadorMultiple1)
        .agregarJugador(jugadorMultiple2)
        .agregarJugador(jugadorMultiple3)
        .agregarJugador(jugadorMultiple4)
        .iniciarJuego()
    
    println("Juego con 4 jugadores creado")
    println("Orden de turnos: ${juegoMultiple.jugadores.map { it.nombre }}")
    
    // Simular varios turnos
    for (i in 1..8) {
        val jugadorActual = juegoMultiple.jugadorEnTurno!!
        println("\nTurno $i: ${jugadorActual.nombre}")
        
        // Hacer un movimiento simple (colocación)
        val movimiento = Movimiento.colocacion(
            (i - 1) / 4, 
            (i - 1) % 4, 
            jugadorActual.nombre.first().toString()
        )
        
        juegoMultiple = juegoMultiple.realizarMovimiento(jugadorActual, movimiento)
        println("Siguiente turno será de: ${juegoMultiple.jugadorEnTurno?.nombre}")
    }
    
    println("\nTablero final del juego múltiple:")
    println(juegoMultiple.verTablero())
    
    // ==========================================
    // 7. DEMO DESCONEXIÓN Y RECONEXIÓN
    // ==========================================
    println("\n🔌 DEMO: MANEJO DE DESCONEXIONES")
    println("=" * 35)
    
    // Simular desconexión de un jugador
    val jugadorDesconectado = juegoMultiple.jugadores[1].cambiarEstadoConexion(false)
    juegoMultiple.jugadores[1] = jugadorDesconectado
    
    println("${jugadorDesconectado.nombre} se ha desconectado")
    println("Jugadores conectados: ${juegoMultiple.jugadoresConectados.map { it.nombre }}")
    
    // El sistema debería saltar automáticamente a jugadores conectados
    println("Probando avance de turno con jugador desconectado...")
    val siguienteTurno = juegoMultiple.siguienteTurno()
    println("Turno actual: ${siguienteTurno.jugadorEnTurno?.nombre}")
    
    // ==========================================
    // 8. RESUMEN Y ESTADÍSTICAS
    // ==========================================
    println("\n📈 RESUMEN DEL DEMO")
    println("=" * 25)
    
    println("✅ Funcionalidades demostradas:")
    println("   • Gestión de turnos automática")
    println("   • Validaciones específicas por tipo de juego")
    println("   • Movimientos complejos (origen → destino)")
    println("   • Colocaciones simples")
    println("   • Análisis de movimientos")
    println("   • Manejo robusto de errores")
    println("   • Soporte para múltiples jugadores")
    println("   • Gestión de desconexiones")
    
    println("\n🎯 Encapsulamiento y Responsabilidades:")
    println("   • Clase Juego: Gestiona reglas y turnos")
    println("   • Clase Tablero: Maneja el estado del tablero")
    println("   • Clase Movimiento: Encapsula datos del movimiento")
    println("   • Clase Jugador: Representa estado del jugador")
    
    println("\n🏁 ¡Demo completado exitosamente!")
}

// Función auxiliar para repetir strings (como Python's * operator)
private operator fun String.times(n: Int): String = this.repeat(n)