package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Pruebas para verificar el manejo de desconexiones parciales y reconexiones.
 * Simula escenarios donde clientes se desconectan y reconectan.
 */
class DesconexionTest {

    private val json = JsonConfig.default

    @Test
    fun `test desconexion cliente durante partida - partida continua con jugadores restantes`() {
        val puerto = 5070
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPruebaDesconexion(puerto, 4) // Crear, unirse, movimiento1, movimiento2
        }

        Thread.sleep(500)

        try {
            // Cliente 1 crea partida
            val juegoId = simularClienteCrear(puerto, "Creator").juegoId!!
            Thread.sleep(200)

            // Cliente 2 se une
            simularClienteUnirse(puerto, "Joiner", juegoId)
            Thread.sleep(200)

            // Cliente 1 hace movimiento
            simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")
            Thread.sleep(200)

            // Simular que Cliente 2 se desconecta (no hace más operaciones)
            // Cliente 1 continúa y hace otro movimiento
            val estadoFinal = simularMovimiento(puerto, juegoId, 1L, 0, 1, "X")

            // Verificar que la partida continúa normalmente
            assertEquals(2, estadoFinal.numJugadores, "Deberían seguir habiendo 2 jugadores")
            assertEquals(EstadoJuego.EN_CURSO, estadoFinal.estadoJuego, "La partida debería continuar")
            assertEquals("X", estadoFinal.tablero[0][0], "Primer movimiento debería persistir")
            assertEquals("X", estadoFinal.tablero[0][1], "Segundo movimiento debería registrarse")

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test reconexion a partida existente despues de desconexion temporal`() {
        val puerto = 5071
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPruebaDesconexion(puerto, 3) // Crear, movimiento, reconectar
        }

        Thread.sleep(500)

        try {
            // Cliente crea partida
            val juegoId = simularClienteCrear(puerto, "Player1").juegoId!!
            Thread.sleep(200)

            // Cliente hace un movimiento
            simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")
            Thread.sleep(200)

            // Simular reconexión: cliente intenta unirse a la misma partida
            val estadoReconectado = simularClienteUnirse(puerto, "Player1_Reconnected", juegoId)

            // Verificar que puede reconectar y ver el estado actual
            assertEquals(juegoId, estadoReconectado.juegoId, "Debería reconectar a la misma partida")
            assertEquals(1, estadoReconectado.numJugadores, "Debería haber 1 jugador")
            assertEquals("X", estadoReconectado.tablero[0][0], "Debería ver el movimiento anterior")

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test intento reconexion a partida en curso falla para nuevos jugadores`() {
        val puerto = 5072
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPruebaDesconexion(puerto, 4) // Crear, unirse, movimiento, intento fallido
        }

        Thread.sleep(500)

        try {
            // Cliente 1 crea partida
            val juegoId = simularClienteCrear(puerto, "Player1").juegoId!!
            Thread.sleep(200)

            // Cliente 2 se une
            simularClienteUnirse(puerto, "Player2", juegoId)
            Thread.sleep(200)

            // Hacer un movimiento para cambiar estado a EN_CURSO
            simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")
            Thread.sleep(200)

            // Cliente 3 intenta unirse (debería fallar porque partida está en curso)
            val resultadoNuevoCliente = simularClienteUnirse(puerto, "Player3_Late", juegoId)

            // Verificar que la unión falló
            assertNotNull(resultadoNuevoCliente.error, "Debería haber error al unirse tarde")
            assertTrue(resultadoNuevoCliente.error!!.contains("no encontrada") ||
                      resultadoNuevoCliente.error!!.contains("llena"),
                     "Error debería indicar que no puede unirse")

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test servidor maneja desconexiones abruptas sin errores`() {
        val puerto = 5073
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPruebaDesconexion(puerto, 2) // Solo crear y desconectar abruptamente
        }

        Thread.sleep(500)

        try {
            // Cliente se conecta y envía comando pero se desconecta sin esperar respuesta
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)

                // Enviar comando y desconectar inmediatamente (sin leer respuesta)
                val comando = Comando.CrearPartida(Jugador(1L, "DisconnectingClient"))
                val jsonComando = json.encodeToString(comando)
                writer.println(jsonComando)

                // Cerrar socket inmediatamente
                socket.close()
            }

            Thread.sleep(500) // Dar tiempo al servidor para manejar la desconexión

            // Verificar que el servidor sigue funcionando conectándose con otro cliente
            val resultado = simularClienteCrear(puerto, "TestClient")

            // Debería funcionar normalmente
            assertNotNull(resultado.juegoId, "Servidor debería seguir funcionando después de desconexión abrupta")
            assertTrue(resultado.juegoId!!.startsWith("PARTIDA-"), "Debería crear partida normalmente")

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test limpieza estado despues de desconexion completa de partida`() {
        val puerto = 5074
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPruebaDesconexion(puerto, 5) // Crear, unirse, movimientos, finalizar
        }

        Thread.sleep(500)

        try {
            // Crear partida con 2 jugadores
            val juegoId = simularClienteCrear(puerto, "FinalPlayer1").juegoId!!
            simularClienteUnirse(puerto, "FinalPlayer2", juegoId)

            // Hacer movimientos para completar el juego (tres en línea)
            simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")
            simularMovimiento(puerto, juegoId, 2L, 1, 1, "O")
            simularMovimiento(puerto, juegoId, 1L, 0, 1, "X")
            simularMovimiento(puerto, juegoId, 2L, 2, 2, "O")
            simularMovimiento(puerto, juegoId, 1L, 0, 2, "X") // Tres en línea

            // Verificar que la partida se marcó como finalizada
            val estadoFinal = simularMovimiento(puerto, juegoId, 1L, 1, 0, "X") // Movimiento después de finalización

            // Debería fallar porque la partida terminó
            assertNotNull(estadoFinal.error, "Movimiento después de finalización debería fallar")

            // Verificar que se puede crear una nueva partida
            val nuevaPartida = simularClienteCrear(puerto, "NewPlayer")
            assertNotNull(nuevaPartida.juegoId, "Debería poder crear nueva partida después de finalizar la anterior")

        } finally {
            serverFuture.cancel(true)
        }
    }

    // Funciones auxiliares

    private data class EstadoCliente(
        val juegoId: String? = null,
        val numJugadores: Int = 0,
        val estadoJuego: EstadoJuego? = null,
        val tablero: Array<Array<String>> = emptyArray(),
        val error: String? = null
    )

    private fun simularClienteCrear(puerto: Int, nombre: String): EstadoCliente {
        return try {
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                val comando = Comando.CrearPartida(Jugador(System.currentTimeMillis(), nombre))
                val jsonComando = json.encodeToString(comando)
                writer.println(jsonComando)

                val respuesta = reader.readLine()
                val evento = json.decodeFromString<Evento>(respuesta)

                when (evento) {
                    is Evento.PartidaActualizada -> EstadoCliente(
                        juegoId = evento.juego.id,
                        numJugadores = evento.juego.jugadores.size,
                        estadoJuego = evento.juego.estado,
                        tablero = extraerTablero(evento.juego)
                    )
                    is Evento.Error -> EstadoCliente(error = evento.mensaje)
                }
            }
        } catch (e: Exception) {
            EstadoCliente(error = e.message)
        }
    }

    private fun simularClienteUnirse(puerto: Int, nombre: String, juegoId: String): EstadoCliente {
        return try {
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                val comando = Comando.UnirseAPartida(juegoId, Jugador(System.currentTimeMillis(), nombre))
                val jsonComando = json.encodeToString(comando)
                writer.println(jsonComando)

                val respuesta = reader.readLine()
                val evento = json.decodeFromString<Evento>(respuesta)

                when (evento) {
                    is Evento.PartidaActualizada -> EstadoCliente(
                        juegoId = evento.juego.id,
                        numJugadores = evento.juego.jugadores.size,
                        estadoJuego = evento.juego.estado,
                        tablero = extraerTablero(evento.juego)
                    )
                    is Evento.Error -> EstadoCliente(error = evento.mensaje)
                }
            }
        } catch (e: Exception) {
            EstadoCliente(error = e.message)
        }
    }

    private fun simularMovimiento(puerto: Int, juegoId: String, jugadorId: Long, fila: Int, columna: Int, contenido: String): EstadoCliente {
        return try {
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                val comando = Comando.RealizarMovimiento(juegoId, jugadorId, fila, columna, contenido)
                val jsonComando = json.encodeToString(comando)
                writer.println(jsonComando)

                val respuesta = reader.readLine()
                val evento = json.decodeFromString<Evento>(respuesta)

                when (evento) {
                    is Evento.PartidaActualizada -> EstadoCliente(
                        juegoId = evento.juego.id,
                        numJugadores = evento.juego.jugadores.size,
                        estadoJuego = evento.juego.estado,
                        tablero = extraerTablero(evento.juego)
                    )
                    is Evento.Error -> EstadoCliente(error = evento.mensaje)
                }
            }
        } catch (e: Exception) {
            EstadoCliente(error = e.message)
        }
    }

    private fun extraerTablero(juego: Juego): Array<Array<String>> {
        val tablero = Array(juego.tablero.filas) { Array(juego.tablero.columnas) { "" } }
        for (fila in 0 until juego.tablero.filas) {
            for (columna in 0 until juego.tablero.columnas) {
                tablero[fila][columna] = juego.tablero.obtenerCelda(fila, columna).contenido ?: ""
            }
        }
        return tablero
    }

    private fun ejecutarServidorPruebaDesconexion(puerto: Int, conexionesEsperadas: Int) {
        val server = java.net.ServerSocket(puerto)
        var conexionesManejadas = 0

        try {
            while (conexionesManejadas < conexionesEsperadas) {
                val socket = server.accept()
                Thread {
                    manejarClientePruebaDesconexion(socket)
                }.start()
                conexionesManejadas++
            }
        } finally {
            server.close()
        }
    }

    private fun manejarClientePruebaDesconexion(socket: java.net.Socket) {
        socket.use { s ->
            try {
                val inReader = BufferedReader(InputStreamReader(s.getInputStream()))
                val outWriter = PrintWriter(s.getOutputStream(), true)

                val linea = inReader.readLine()
                if (linea != null) {
                    try {
                        val comando = json.decodeFromString<Comando>(linea)
                        val evento = procesarComandoPruebaDesconexion(comando)
                        val respuesta = json.encodeToString<Evento>(evento)
                        outWriter.println(respuesta)
                    } catch (e: Exception) {
                        val error = Evento.Error(mensaje = e.message ?: "Error desconocido")
                        outWriter.println(json.encodeToString<Evento>(error))
                    }
                }
            } catch (e: Exception) {
                // Ignorar errores de desconexión abrupta
                Logger.debug("Desconexión abrupta manejada correctamente: ${e.message}")
            }
        }
    }

    private fun procesarComandoPruebaDesconexion(comando: Comando): Evento {
        return when (comando) {
            is Comando.CrearPartida -> {
                val juego = ServicioPartidas.crearPartida(comando.jugador)
                Evento.PartidaActualizada(juego)
            }
            is Comando.UnirseAPartida -> {
                val juego = ServicioPartidas.unirseAPartida(comando.idPartida, comando.jugador)
                    ?: return Evento.Error("Partida no encontrada")
                Evento.PartidaActualizada(juego)
            }
            is Comando.UnirseAPartidaAuto -> {
                val elegible = ServicioPartidas.listarPartidas()
                    .firstOrNull { it.estado == EstadoJuego.ESPERANDO_JUGADORES && it.jugadores.size < it.maxJugadores }
                    ?: return Evento.Error("No hay partidas disponibles")
                val juego = ServicioPartidas.unirseAPartida(elegible.id, comando.jugador)
                    ?: return Evento.Error("Partida no encontrada")
                Evento.PartidaActualizada(juego)
            }
            is Comando.RealizarMovimiento -> {
                val juego = ServicioPartidas.obtenerPartida(comando.idPartida)
                    ?: return Evento.Error("Partida no encontrada")
                val actualizado = try {
                    juego.realizarMovimiento(
                        jugadorId = comando.jugadorId,
                        fila = comando.fila,
                        columna = comando.columna,
                        contenido = comando.contenido
                    )
                } catch (e: Exception) {
                    return Evento.Error("Movimiento inválido: ${e.message}")
                }
                ServicioPartidas.actualizarPartida(actualizado)
                Evento.PartidaActualizada(actualizado)
            }
        }
    }
}