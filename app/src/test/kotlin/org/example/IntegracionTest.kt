package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Pruebas de integración para verificar el funcionamiento del servidor
 * con múltiples conexiones simultáneas y escenarios realistas.
 */
class IntegracionTest {

    private val json = JsonConfig.default

    @Test
    fun `test servidor maneja multiples conexiones simultaneas`() {
        val puerto = 5051 // Puerto diferente para pruebas
        val numClientes = 5
        val latch = CountDownLatch(numClientes)
        val conexionesExitosas = AtomicInteger(0)

        // Iniciar servidor en un hilo separado
        val serverThread = Thread {
            val server = ServerSocket(puerto)
            repeat(numClientes) {
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                    conexionesExitosas.incrementAndGet()
                    latch.countDown()
                }.start()
            }
        }
        serverThread.start()

        // Esperar un poco para que el servidor esté listo
        Thread.sleep(100)

        // Crear múltiples clientes
        val clientThreads = (1..numClientes).map { i ->
            Thread {
                try {
                    Socket("127.0.0.1", puerto).use { socket ->
                        val writer = PrintWriter(socket.getOutputStream(), true)
                        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                        // Enviar un comando simple
                        val comando = Comando.CrearPartida(Jugador(i.toLong(), "Cliente$i"))
                        val jsonComando = json.encodeToString(comando)
                        writer.println(jsonComando)

                        // Leer respuesta
                        val respuesta = reader.readLine()
                        assertNotNull(respuesta, "Debería recibir respuesta del servidor")

                        val evento = json.decodeFromString<Evento>(respuesta)
                        assertTrue(evento is Evento.PartidaActualizada ||
                                 evento is Evento.Error,
                                 "Respuesta debería ser PartidaActualizada o Error")
                    }
                } catch (e: Exception) {
                    println("Error en cliente $i: ${e.message}")
                }
            }
        }

        // Iniciar todos los clientes
        clientThreads.forEach { it.start() }

        // Esperar a que todos terminen
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Todos los clientes deberían terminar")
        assertEquals(numClientes, conexionesExitosas.get(), "Todas las conexiones deberían ser exitosas")

        serverThread.interrupt()
    }

    @Test
    fun `test creacion y union a partida con multiples clientes`() {
        val puerto = 5052
        val serverThread = Thread {
            val server = ServerSocket(puerto)
            val clientsHandled = AtomicInteger(0)

            repeat(2) {
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                    clientsHandled.incrementAndGet()
                }.start()
            }
        }
        serverThread.start()
        Thread.sleep(100)

        // Cliente 1 crea partida
        val resultado1 = conectarYEnviar(puerto, Comando.CrearPartida(Jugador(1L, "Creador")))
        assertTrue(resultado1 is Evento.PartidaActualizada, "Cliente 1 debería crear partida exitosamente")

        val juegoCreado = (resultado1 as Evento.PartidaActualizada).juego
        assertEquals(1, juegoCreado.jugadores.size, "Debería haber 1 jugador inicialmente")

        // Cliente 2 se une
        val resultado2 = conectarYEnviar(puerto, Comando.UnirseAPartida(juegoCreado.id, Jugador(2L, "Unidor")))
        assertTrue(resultado2 is Evento.PartidaActualizada, "Cliente 2 debería unirse exitosamente")

        val juegoActualizado = (resultado2 as Evento.PartidaActualizada).juego
        assertEquals(2, juegoActualizado.jugadores.size, "Deberían haber 2 jugadores")
        assertEquals(juegoCreado.id, juegoActualizado.id, "Debería ser la misma partida")

        serverThread.interrupt()
    }

    @Test
    fun `test union automatica a partida disponible`() {
        val puerto = 5053
        val serverThread = Thread {
            val server = ServerSocket(puerto)
            val clientsHandled = AtomicInteger(0)

            repeat(2) {
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                    clientsHandled.incrementAndGet()
                }.start()
            }
        }
        serverThread.start()
        Thread.sleep(100)

        // Cliente 1 crea partida
        val resultado1 = conectarYEnviar(puerto, Comando.CrearPartida(Jugador(1L, "Creador")))
        assertTrue(resultado1 is Evento.PartidaActualizada)

        // Cliente 2 se une automáticamente
        val resultado2 = conectarYEnviar(puerto, Comando.UnirseAPartidaAuto(Jugador(2L, "AutoUnidor")))
        assertTrue(resultado2 is Evento.PartidaActualizada, "Unión automática debería funcionar")

        val juego = (resultado2 as Evento.PartidaActualizada).juego
        assertEquals(2, juego.jugadores.size, "Deberían unirse 2 jugadores")

        serverThread.interrupt()
    }

    @Test
    fun `test movimientos en partida con estado consistente`() {
        val puerto = 5054
        val serverThread = Thread {
            val server = ServerSocket(puerto)
            val clientsHandled = AtomicInteger(0)

            repeat(3) { // Crear, unirse, mover
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                    clientsHandled.incrementAndGet()
                }.start()
            }
        }
        serverThread.start()
        Thread.sleep(100)

        // Crear partida
        val resultado1 = conectarYEnviar(puerto, Comando.CrearPartida(Jugador(1L, "Jugador1")))
        val juego = (resultado1 as Evento.PartidaActualizada).juego

        // Unirse
        val resultado2 = conectarYEnviar(puerto, Comando.UnirseAPartida(juego.id, Jugador(2L, "Jugador2")))
        val juegoConDos = (resultado2 as Evento.PartidaActualizada).juego

        // Iniciar juego (agregar estado EN_CURSO)
        val juegoIniciado = juegoConDos.iniciarJuego()

        // Realizar movimiento
        val movimiento = Comando.RealizarMovimiento(juego.id, 1L, 0, 0, "X")
        val resultado3 = conectarYEnviar(puerto, movimiento)

        assertTrue(resultado3 is Evento.PartidaActualizada, "Movimiento debería ser aceptado")
        val juegoConMovimiento = (resultado3 as Evento.PartidaActualizada).juego

        // Verificar que el movimiento se registró
        assertEquals("X", juegoConMovimiento.tablero.obtenerCelda(0, 0).contenido,
                    "La celda debería contener 'X'")

        serverThread.interrupt()
    }

    @Test
    fun `test manejo de errores - partida no encontrada`() {
        val puerto = 5055
        val serverThread = Thread {
            val server = ServerSocket(puerto)
            val socket = server.accept()
            Thread { manejarClientePrueba(socket) }.start()
        }
        serverThread.start()
        Thread.sleep(100)

        val resultado = conectarYEnviar(puerto, Comando.UnirseAPartida("PARTIDA-INVENTADA", Jugador(1L, "Test")))

        assertTrue(resultado is Evento.Error, "Debería devolver error para partida inexistente")
        assertEquals("Partida no encontrada", (resultado as Evento.Error).mensaje)

        serverThread.interrupt()
    }

    @Test
    fun `test concurrencia - multiples operaciones simultaneas`() {
        val puerto = 5056
        val numOperaciones = 10
        val operacionesCompletadas = AtomicInteger(0)

        val serverThread = Thread {
            val server = ServerSocket(puerto)
            repeat(numOperaciones) {
                val socket = server.accept()
                Thread {
                    manejarClientePrueba(socket)
                    operacionesCompletadas.incrementAndGet()
                }.start()
            }
        }
        serverThread.start()
        Thread.sleep(100)

        // Ejecutar múltiples operaciones en paralelo
        val threads = (1..numOperaciones).map { i ->
            Thread {
                val comando = Comando.CrearPartida(Jugador(i.toLong(), "Concurrente$i"))
                conectarYEnviar(puerto, comando)
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Esperar a que el servidor procese todo
        Thread.sleep(1000)

        assertEquals(numOperaciones, operacionesCompletadas.get(),
                    "Todas las operaciones deberían completarse")

        serverThread.interrupt()
    }

    // Funciones auxiliares

    private fun conectarYEnviar(puerto: Int, comando: Comando): Evento {
        Socket("127.0.0.1", puerto).use { socket ->
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val jsonComando = json.encodeToString(comando)
            writer.println(jsonComando)

            val respuesta = reader.readLine()
                ?: throw Exception("No se recibió respuesta")

            return json.decodeFromString(respuesta)
        }
    }

    private fun manejarClientePrueba(socket: Socket) {
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