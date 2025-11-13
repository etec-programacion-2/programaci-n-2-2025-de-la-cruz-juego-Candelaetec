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
 * Pruebas de integración para verificar consistencia entre clientes heterogéneos.
 * Simula escenarios donde clientes consola y GUI interactúan simultáneamente.
 */
class ClienteHeterogeneoTest {

    private val json = JsonConfig.default

    @Test
    fun `test consistencia estado entre cliente consola y simulador GUI`() {
        val puerto = 5060
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPrueba(puerto, 3) // Esperar 3 conexiones
        }

        Thread.sleep(500) // Esperar que servidor esté listo

        try {
            // Cliente 1 (simula GUI): crea partida
            val cliente1Future = CompletableFuture.supplyAsync {
                simularClienteGUI(puerto, "GUI_Player")
            }

            Thread.sleep(200)

            // Cliente 2 (simula consola): se une automáticamente
            val cliente2Future = CompletableFuture.supplyAsync {
                simularClienteConsola(puerto, "Console_Player")
            }

            // Esperar que ambos clientes terminen
            val estadoCliente1 = cliente1Future.get(5, TimeUnit.SECONDS)
            val estadoCliente2 = cliente2Future.get(5, TimeUnit.SECONDS)

            // Verificar consistencia
            assertNotNull(estadoCliente1.juegoId, "Cliente GUI debería tener ID de juego")
            assertNotNull(estadoCliente2.juegoId, "Cliente consola debería tener ID de juego")
            assertEquals(estadoCliente1.juegoId, estadoCliente2.juegoId, "Ambos clientes deberían estar en la misma partida")

            assertEquals(2, estadoCliente1.numJugadores, "Deberían haber 2 jugadores en la partida")
            assertEquals(2, estadoCliente2.numJugadores, "Deberían haber 2 jugadores en la partida")

            // Verificar que ambos ven el mismo estado inicial
            assertEquals(EstadoJuego.ESPERANDO_JUGADORES, estadoCliente1.estadoJuego)
            assertEquals(EstadoJuego.ESPERANDO_JUGADORES, estadoCliente2.estadoJuego)

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test sincronizacion movimientos entre clientes heterogeneos`() {
        val puerto = 5061
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPrueba(puerto, 4) // Crear, unirse, mover1, mover2
        }

        Thread.sleep(500)

        try {
            // Cliente 1 crea partida
            val juegoId = simularClienteGUI(puerto, "Creator").juegoId!!
            Thread.sleep(200)

            // Cliente 2 se une
            simularClienteConsolaUnirse(puerto, "Joiner", juegoId)
            Thread.sleep(200)

            // Cliente 1 hace primer movimiento
            val estadoDespuesMov1 = simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")
            Thread.sleep(200)

            // Cliente 2 hace segundo movimiento
            val estadoDespuesMov2 = simularMovimiento(puerto, juegoId, 2L, 1, 1, "O")

            // Verificar que ambos movimientos se registraron
            assertEquals("X", estadoDespuesMov1.tablero[0][0])
            assertEquals("O", estadoDespuesMov1.tablero[1][1])
            assertEquals("X", estadoDespuesMov2.tablero[0][0])
            assertEquals("O", estadoDespuesMov2.tablero[1][1])

            // Verificar estado del juego
            assertEquals(EstadoJuego.EN_CURSO, estadoDespuesMov2.estadoJuego)

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test desconexion parcial - un cliente se desconecta pero partida continua`() {
        val puerto = 5062
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPrueba(puerto, 3) // Crear, unirse, movimiento
        }

        Thread.sleep(500)

        try {
            // Cliente 1 crea partida
            val juegoId = simularClienteGUI(puerto, "Persistent").juegoId!!
            Thread.sleep(200)

            // Cliente 2 se une y luego se "desconecta" (no hace más operaciones)
            simularClienteConsolaUnirse(puerto, "Temporary", juegoId)
            Thread.sleep(200)

            // Cliente 1 continúa haciendo movimientos
            val estadoMovimiento = simularMovimiento(puerto, juegoId, 1L, 0, 0, "X")

            // Verificar que la partida continúa normalmente
            assertEquals("X", estadoMovimiento.tablero[0][0])
            assertEquals(EstadoJuego.EN_CURSO, estadoMovimiento.estadoJuego)
            assertEquals(2, estadoMovimiento.numJugadores)

        } finally {
            serverFuture.cancel(true)
        }
    }

    @Test
    fun `test concurrencia - multiples operaciones simultaneas de diferentes tipos cliente`() {
        val puerto = 5063
        val numOperaciones = 6
        val serverFuture = CompletableFuture.supplyAsync {
            ejecutarServidorPrueba(puerto, numOperaciones)
        }

        Thread.sleep(500)

        try {
            val operaciones = (1..numOperaciones).map { i ->
                CompletableFuture.supplyAsync {
                    when (i % 3) {
                        0 -> simularClienteGUI(puerto, "GUI_Client$i")
                        1 -> simularClienteConsola(puerto, "Console_Client$i")
                        else -> simularClienteConsolaUnirse(puerto, "Joiner$i", "PARTIDA-TEST123")
                    }
                }
            }

            // Esperar que todas las operaciones terminen
            val resultados = operaciones.map { it.get(10, TimeUnit.SECONDS) }

            // Verificar que no hubo errores
            val errores = resultados.filter { it.error != null }
            assertTrue(errores.isEmpty(), "No debería haber errores en operaciones concurrentes: ${errores.map { it.error }}")

            // Verificar que se crearon algunas partidas
            val partidasCreadas = resultados.filter { it.juegoId != null }
            assertTrue(partidasCreadas.isNotEmpty(), "Deberían haberse creado algunas partidas")

        } finally {
            serverFuture.cancel(true)
        }
    }

    // Funciones auxiliares para simular diferentes tipos de cliente

    private data class EstadoCliente(
        val juegoId: String? = null,
        val numJugadores: Int = 0,
        val estadoJuego: EstadoJuego? = null,
        val tablero: Array<Array<String>> = emptyArray(),
        val error: String? = null
    )

    private fun simularClienteGUI(puerto: Int, nombre: String): EstadoCliente {
        return try {
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Simular GUI: crear partida
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

    private fun simularClienteConsola(puerto: Int, nombre: String): EstadoCliente {
        return try {
            Socket("127.0.0.1", puerto).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Simular consola: unirse automáticamente
                val comando = Comando.UnirseAPartidaAuto(Jugador(System.currentTimeMillis(), nombre))
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

    private fun simularClienteConsolaUnirse(puerto: Int, nombre: String, juegoId: String): EstadoCliente {
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

    private fun ejecutarServidorPrueba(puerto: Int, conexionesEsperadas: Int) {
        val server = java.net.ServerSocket(puerto)
        var conexionesManejadas = 0

        try {
            while (conexionesManejadas < conexionesEsperadas) {
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                }.start()
                conexionesManejadas++
            }
        } finally {
            server.close()
        }
    }

    private fun manejarClientePrueba(socket: java.net.Socket) {
        socket.use { s ->
            val inReader = BufferedReader(InputStreamReader(s.getInputStream()))
            val outWriter = PrintWriter(s.getOutputStream(), true)

            val linea = inReader.readLine()
            if (linea != null) {
                try {
                    val comando = json.decodeFromString<Comando>(linea)
                    val evento = procesarComandoPrueba(comando)
                    val respuesta = json.encodeToString<Evento>(evento)
                    outWriter.println(respuesta)
                } catch (e: Exception) {
                    val error = Evento.Error(mensaje = e.message ?: "Error desconocido")
                    outWriter.println(json.encodeToString<Evento>(error))
                }
            }
        }
    }

    private fun procesarComandoPrueba(comando: Comando): Evento {
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