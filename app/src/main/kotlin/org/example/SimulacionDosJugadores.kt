package org.example

/**
 * Simulación automática de dos jugadores para demostrar el juego completo
 */
fun main() {
    println("=== SIMULACIÓN AUTOMÁTICA DE DOS JUGADORES ===\n")
    
    try {
        // Jugador 1: Crear partida
        println("🎮 JUGADOR 1: Creando partida...")
        val cliente1 = ClienteConsola("127.0.0.1", 5050)
        cliente1.conectar()
        
        val jugador1 = Jugador(id = 1001L, nombre = "Ana")
        val comandoCrear = Comando.CrearPartida(jugador1)
        val eventoCrear = cliente1.enviarComando(comandoCrear)
        
        when (eventoCrear) {
            is Evento.PartidaActualizada -> {
                val partidaId = eventoCrear.juego.id
                println("✅ Partida creada: $partidaId")
                println("📊 Estado: ${eventoCrear.juego.estado}")
                println("👥 Jugadores: ${eventoCrear.juego.jugadores.size}/${eventoCrear.juego.maxJugadores}")
                
                // Jugador 2: Unirse a la partida
                println("\n🎮 JUGADOR 2: Uniéndose a la partida...")
                val cliente2 = ClienteConsola("127.0.0.1", 5050)
                cliente2.conectar()
                
                val jugador2 = Jugador(id = 1002L, nombre = "Carlos")
                val comandoUnirse = Comando.UnirseAPartida(partidaId, jugador2)
                val eventoUnirse = cliente2.enviarComando(comandoUnirse)
                
                when (eventoUnirse) {
                    is Evento.PartidaActualizada -> {
                        println("✅ Jugador 2 se unió exitosamente")
                        println("📊 Estado: ${eventoUnirse.juego.estado}")
                        println("👥 Jugadores: ${eventoUnirse.juego.jugadores.size}/${eventoUnirse.juego.maxJugadores}")
                        
                        // Mostrar tablero inicial
                        println("\n🎯 TABLERO INICIAL:")
                        mostrarTablero(eventoUnirse.juego)
                        
                        // Simular algunos movimientos
                        println("\n🎮 SIMULANDO MOVIMIENTOS:")
                        
                        // Turno 1: Ana coloca X en a1
                        println("Turno de Ana: coloca X en a1")
                        val movimiento1 = Comando.RealizarMovimiento(partidaId, 1001L, 0, 0, "X")
                        val evento1 = cliente1.enviarComando(movimiento1)
                        
                        when (evento1) {
                            is Evento.PartidaActualizada -> {
                                println("✅ Movimiento exitoso")
                                mostrarTablero(evento1.juego)
                            }
                            is Evento.Error -> {
                                println("❌ Error: ${evento1.mensaje}")
                            }
                        }
                        
                        // Turno 2: Carlos coloca O en b2
                        println("\nTurno de Carlos: coloca O en b2")
                        val movimiento2 = Comando.RealizarMovimiento(partidaId, 1002L, 1, 1, "O")
                        val evento2 = cliente2.enviarComando(movimiento2)
                        
                        when (evento2) {
                            is Evento.PartidaActualizada -> {
                                println("✅ Movimiento exitoso")
                                mostrarTablero(evento2.juego)
                            }
                            is Evento.Error -> {
                                println("❌ Error: ${evento2.mensaje}")
                            }
                        }
                        
                        // Turno 3: Ana coloca X en c1
                        println("\nTurno de Ana: coloca X en c1")
                        val movimiento3 = Comando.RealizarMovimiento(partidaId, 1001L, 0, 2, "X")
                        val evento3 = cliente1.enviarComando(movimiento3)
                        
                        when (evento3) {
                            is Evento.PartidaActualizada -> {
                                println("✅ Movimiento exitoso")
                                mostrarTablero(evento3.juego)
                            }
                            is Evento.Error -> {
                                println("❌ Error: ${evento3.mensaje}")
                            }
                        }
                        
                        println("\n🎉 ¡Simulación completada!")
                        println("📋 Para jugar manualmente:")
                        println("   1. Abre dos terminales")
                        println("   2. En cada terminal ejecuta: ./gradlew runClient")
                        println("   3. Un jugador crea partida, el otro se une")
                        println("   4. ¡Disfruta el juego!")
                        
                    }
                    is Evento.Error -> {
                        println("❌ Error al unirse: ${eventoUnirse.mensaje}")
                    }
                }
                
                cliente2.desconectar()
            }
            is Evento.Error -> {
                println("❌ Error al crear partida: ${eventoCrear.mensaje}")
            }
        }
        
        cliente1.desconectar()
        
    } catch (e: Exception) {
        println("❌ Error durante la simulación: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Muestra el tablero de forma clara
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
