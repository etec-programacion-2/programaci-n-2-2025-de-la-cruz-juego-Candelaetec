package org.example
fun main() {
    // Crear jugadores
    val jugador1 = Jugador(id = 1L, nombre = "Ana")
    val jugador2 = Jugador(id = 2L, nombre = "Carlos", puntuacion = 50)
    
    println("Jugador creado: $jugador1")
    println("Jugador con puntuación: $jugador2")
    
    // Crear un juego
    var juego = Juego(id = "PARTIDA-001")
    println("Juego creado: Estado inicial = ${juego.estado}")
    
    // Agregar jugadores
    juego = juego.agregarJugador(jugador1)
    juego = juego.agregarJugador(jugador2)
    println("Jugadores en la partida: ${juego.jugadores.size}")
    
    // Iniciar el juego
    juego = juego.iniciarJuego()
    println("Estado del juego después de iniciar: ${juego.estado}")
    
    // Actualizar puntuación de un jugador
    val jugadorActualizado = jugador1.actualizarPuntuacion(100)
    println("Jugador con nueva puntuación: $jugadorActualizado")
    
    // Ejemplo de las propiedades computadas
    println("¿Está lleno el juego? ${juego.estaLleno}")
    println("Jugadores conectados: ${juego.jugadoresConectados.size}")
}