package org.example

fun main() {
    println("=== JUEGO COMPLETO CON TABLERO ===\n")
    
    // 1. Crear jugadores
    val jugador1 = Jugador(id = 1L, nombre = "Ana")
    val jugador2 = Jugador(id = 2L, nombre = "Carlos")
    
    println("Jugadores creados:")
    println("- ${jugador1.nombre} (ID: ${jugador1.id})")
    println("- ${jugador2.nombre} (ID: ${jugador2.id})\n")
    
    // Demo rápido: uso de ServicioPartidas como orquestador central
    println("=== DEMO ServicioPartidas ===")
    val partidaCreada = ServicioPartidas.crearPartida(jugador1)
    println("Partida creada vía ServicioPartidas: ${partidaCreada.id}")
    val partidaConDos = ServicioPartidas.unirseAPartida(partidaCreada.id, jugador2)
    println("Jugadores en la partida: ${partidaConDos?.jugadores?.size}")
    println("Partidas activas: ${ServicioPartidas.listarPartidas().map { it.id }}\n")

    // 2. Crear un juego con tablero 3x3 (como tres en línea)
    var juego = Juego(
        id = "PARTIDA-001",
        tablero = Tablero(3, 3),
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
    
    println("org.example.Juego creado: ${juego.id}")
    println("Estado inicial: ${juego.estado}")
    println("Tablero inicial:")
    println(juego.verTablero())
    
    // 3. Agregar jugadores al juego
    juego = juego.agregarJugador(jugador1)
    juego = juego.agregarJugador(jugador2)
    
    println("Jugadores agregados: ${juego.jugadores.size}")
    
    // 4. Iniciar el juego
    juego = juego.iniciarJuego()
    println("¡org.example.Juego iniciado! Estado: ${juego.estado}\n")
    
    // 5. Simular algunos movimientos en el tablero
    println("=== SIMULACIÓN DE MOVIMIENTOS ===")
    
    // Turno del jugador Ana (X)
    println("Turno de Ana - coloca X en (0,0):")
    juego = juego.realizarMovimiento(1L, 0, 0, "X")
    println(juego.verTablero())
    
    // Turno del jugador Carlos (O)
    println("Turno de Carlos - coloca O en (1,1):")
    juego = juego.realizarMovimiento(2L, 1, 1, "O")
    println(juego.verTablero())
    
    // Más movimientos
    println("Ana coloca X en (0,1):")
    juego = juego.realizarMovimiento(1L, 0, 1, "X")
    println(juego.verTablero())
    
    println("Carlos coloca O en (2,0):")
    juego = juego.realizarMovimiento(2L, 2, 0, "O")
    println(juego.verTablero())
    
    // 6. Verificar estado del tablero
    println("=== ANÁLISIS DEL TABLERO ===")
    println("Posiciones disponibles: ${juego.posicionesDisponibles()}")
    println("¿Posición (0,2) disponible? ${juego.posicionDisponible(0, 2)}")
    println("¿Posición (0,0) disponible? ${juego.posicionDisponible(0, 0)}")
    
    // 7. Demostrar manejo de errores
    println("\n=== MANEJO DE ERRORES ===")
    try {
        juego.realizarMovimiento(1L, 0, 0, "X") // Intentar ocupar celda ya ocupada
    } catch (e: Exception) {
        println("Error capturado: ${e.message}")
    }
    
    try {
        juego.realizarMovimiento(1L, 5, 5, "X") // Coordenadas fuera del tablero
    } catch (e: Exception) {
        println("Error capturado: ${e.message}")
    }
    
    // 8. Ejemplo con tablero más grande (ajedrez)
    println("\n=== JUEGO DE AJEDREZ (8x8) ===")
    var juegoAjedrez = Juego(
        id = "AJEDREZ-001",
        tablero = Tablero(8, 8),
        maxJugadores = 2,
        tipoJuego = TipoJuego.AJEDREZ
    )
    
    juegoAjedrez = juegoAjedrez.agregarJugador(
        Jugador(id = 3L, nombre = "María")
    )
    juegoAjedrez = juegoAjedrez.agregarJugador(
        Jugador(id = 4L, nombre = "Luis")
    )
    juegoAjedrez = juegoAjedrez.iniciarJuego()
    
    // Crear tablero con algunas piezas de ajedrez preconfiguradas
    val tableroAjedrez = Tablero(8, 8)
    tableroAjedrez.colocarEnCelda(0, 0, "♜")
    tableroAjedrez.colocarEnCelda(7, 7, "♖")
    tableroAjedrez.colocarEnCelda(0, 4, "♚")
    
    // Actualizar el juego con el tablero configurado
    juegoAjedrez = juegoAjedrez.copy(tablero = tableroAjedrez)
    
    println("Tablero de ajedrez:")
    println(juegoAjedrez.verTablero())
    
    println("Estado del juego de ajedrez: $juegoAjedrez")
}

/**
 * Enum para diferentes tipos de juego que requieren validaciones específicas
 */
enum class TipoJuego {
    GENERICO,      // Juego genérico con validaciones mínimas
    TRES_EN_LINEA, // Tres en línea (solo colocaciones)
    AJEDREZ,       // Ajedrez (movimientos complejos)
    DAMAS          // Damas (movimientos diagonales)
} 