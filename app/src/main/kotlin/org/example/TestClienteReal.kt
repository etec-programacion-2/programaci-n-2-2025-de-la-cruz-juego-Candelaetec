package org.example

/**
 * Script de prueba para demostrar el cliente interactivo funcionando
 * con el servidor real.
 */
fun main() {
    println("=== PRUEBA DEL CLIENTE INTERACTIVO CON SERVIDOR REAL ===\n")
    
    // Crear cliente
    val cliente = ClienteConsola("127.0.0.1", 5050)
    
    println("🧪 Probando conexión al servidor...")
    
    try {
        // Probar conexión básica
        cliente.conectar()
        println("✅ Conexión exitosa al servidor")
        
        // Probar creación de partida
        println("\n🧪 Probando creación de partida...")
        val jugador = Jugador(id = System.currentTimeMillis(), nombre = "TestPlayer")
        val comando = Comando.CrearPartida(jugador)
        val evento = cliente.enviarComando(comando)
        
        when (evento) {
            is Evento.PartidaActualizada -> {
                println("✅ Partida creada exitosamente: ${evento.juego.id}")
                println("📊 Estado del juego: ${evento.juego.estado}")
                println("👥 Jugadores: ${evento.juego.jugadores.size}/${evento.juego.maxJugadores}")
                
                // Mostrar tablero
                println("\n🎯 Tablero inicial:")
                mostrarTableroSimple(evento.juego)
            }
            is Evento.Error -> {
                println("❌ Error al crear partida: ${evento.mensaje}")
            }
        }
        
        // Probar movimiento
        println("\n🧪 Probando movimiento...")
        val movimientoComando = Comando.RealizarMovimiento(
            idPartida = (evento as? Evento.PartidaActualizada)?.juego?.id ?: "",
            jugadorId = jugador.id,
            fila = 0,
            columna = 0,
            contenido = "X"
        )
        
        val movimientoEvento = cliente.enviarComando(movimientoComando)
        when (movimientoEvento) {
            is Evento.PartidaActualizada -> {
                println("✅ Movimiento realizado exitosamente")
                println("🎯 Tablero después del movimiento:")
                mostrarTableroSimple(movimientoEvento.juego)
            }
            is Evento.Error -> {
                println("❌ Error en movimiento: ${movimientoEvento.mensaje}")
            }
        }
        
        cliente.desconectar()
        println("\n🎉 ¡Prueba completada exitosamente!")
        
    } catch (e: Exception) {
        println("❌ Error durante la prueba: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Muestra el tablero de forma simple
 */
private fun mostrarTableroSimple(juego: Juego) {
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
