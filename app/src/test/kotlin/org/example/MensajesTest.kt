package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Pruebas unitarias para la serialización y deserialización de mensajes.
 * Verifica que los comandos y eventos se serialicen correctamente en JSON
 * y que el proceso sea reversible (round-trip).
 */
class MensajesTest {

    private val json = JsonConfig.default

    @Test
    fun `test serializacion Comando CrearPartida`() {
        val jugador = Jugador(id = 123L, nombre = "TestPlayer")
        val comando = Comando.CrearPartida(jugador)

        val jsonString = json.encodeToString(comando)
        assertTrue(jsonString.contains("CrearPartida"))
        assertTrue(jsonString.contains("TestPlayer"))
        assertTrue(jsonString.contains("123"))

        // Round-trip test
        val decoded = json.decodeFromString<Comando>(jsonString)
        assertTrue(decoded is Comando.CrearPartida)
        assertEquals(jugador, (decoded as Comando.CrearPartida).jugador)
    }

    @Test
    fun `test serializacion Comando UnirseAPartida`() {
        val jugador = Jugador(id = 456L, nombre = "JoinPlayer")
        val comando = Comando.UnirseAPartida("PARTIDA-ABC123", jugador)

        val jsonString = json.encodeToString(comando)
        assertTrue(jsonString.contains("UnirseAPartida"))
        assertTrue(jsonString.contains("PARTIDA-ABC123"))
        assertTrue(jsonString.contains("JoinPlayer"))

        val decoded = json.decodeFromString<Comando>(jsonString)
        assertTrue(decoded is Comando.UnirseAPartida)
        val decodedCmd = decoded as Comando.UnirseAPartida
        assertEquals("PARTIDA-ABC123", decodedCmd.idPartida)
        assertEquals(jugador, decodedCmd.jugador)
    }

    @Test
    fun `test serializacion Comando UnirseAPartidaAuto`() {
        val jugador = Jugador(id = 789L, nombre = "AutoPlayer")
        val comando = Comando.UnirseAPartidaAuto(jugador)

        val jsonString = json.encodeToString(comando)
        assertTrue(jsonString.contains("UnirseAPartidaAuto"))
        assertTrue(jsonString.contains("AutoPlayer"))

        val decoded = json.decodeFromString<Comando>(jsonString)
        assertTrue(decoded is Comando.UnirseAPartidaAuto)
        assertEquals(jugador, (decoded as Comando.UnirseAPartidaAuto).jugador)
    }

    @Test
    fun `test serializacion Comando RealizarMovimiento`() {
        val comando = Comando.RealizarMovimiento(
            idPartida = "PARTIDA-XYZ789",
            jugadorId = 111L,
            fila = 1,
            columna = 2,
            contenido = "X"
        )

        val jsonString = json.encodeToString(comando)
        assertTrue(jsonString.contains("RealizarMovimiento"))
        assertTrue(jsonString.contains("PARTIDA-XYZ789"))
        assertTrue(jsonString.contains("111"))
        assertTrue(jsonString.contains("1"))
        assertTrue(jsonString.contains("2"))
        assertTrue(jsonString.contains("X"))

        val decoded = json.decodeFromString<Comando>(jsonString)
        assertTrue(decoded is Comando.RealizarMovimiento)
        val decodedCmd = decoded as Comando.RealizarMovimiento
        assertEquals("PARTIDA-XYZ789", decodedCmd.idPartida)
        assertEquals(111L, decodedCmd.jugadorId)
        assertEquals(1, decodedCmd.fila)
        assertEquals(2, decodedCmd.columna)
        assertEquals("X", decodedCmd.contenido)
    }

    @Test
    fun `test serializacion Evento PartidaActualizada`() {
        val tablero = Tablero(3, 3)
        tablero.colocarEnCelda(0, 0, "X")
        tablero.colocarEnCelda(1, 1, "O")

        val jugador1 = Jugador(id = 1L, nombre = "Player1")
        val jugador2 = Jugador(id = 2L, nombre = "Player2")

        val juego = Juego(
            id = "PARTIDA-TEST123",
            tablero = tablero,
            maxJugadores = 2,
            tipoJuego = TipoJuego.TRES_EN_LINEA
        ).agregarJugador(jugador1).agregarJugador(jugador2)

        val evento = Evento.PartidaActualizada(juego)

        val jsonString = json.encodeToString(evento)
        assertTrue(jsonString.contains("PartidaActualizada"))
        assertTrue(jsonString.contains("PARTIDA-TEST123"))
        assertTrue(jsonString.contains("Player1"))
        assertTrue(jsonString.contains("Player2"))

        val decoded = json.decodeFromString<Evento>(jsonString)
        assertTrue(decoded is Evento.PartidaActualizada)
        val decodedEvento = decoded as Evento.PartidaActualizada
        assertEquals(juego.id, decodedEvento.juego.id)
        assertEquals(juego.jugadores.size, decodedEvento.juego.jugadores.size)
    }

    @Test
    fun `test serializacion Evento Error`() {
        val evento = Evento.Error("Partida no encontrada", "PARTIDA_NOT_FOUND")

        val jsonString = json.encodeToString(evento)
        assertTrue(jsonString.contains("Error"))
        assertTrue(jsonString.contains("Partida no encontrada"))
        assertTrue(jsonString.contains("PARTIDA_NOT_FOUND"))

        val decoded = json.decodeFromString<Evento>(jsonString)
        assertTrue(decoded is Evento.Error)
        val decodedEvento = decoded as Evento.Error
        assertEquals("Partida no encontrada", decodedEvento.mensaje)
        assertEquals("PARTIDA_NOT_FOUND", decodedEvento.codigo)
    }

    @Test
    fun `test polimorfismo de comandos - sealed classes`() {
        val comandos = listOf(
            Comando.CrearPartida(Jugador(1L, "P1")),
            Comando.UnirseAPartida("ID123", Jugador(2L, "P2")),
            Comando.UnirseAPartidaAuto(Jugador(3L, "P3")),
            Comando.RealizarMovimiento("ID456", 4L, 0, 1, "X")
        )

        comandos.forEach { comando ->
            val jsonString = json.encodeToString(comando)
            val decoded = json.decodeFromString<Comando>(jsonString)

            // Verificar que mantiene el tipo correcto
            when (comando) {
                is Comando.CrearPartida -> assertTrue(decoded is Comando.CrearPartida)
                is Comando.UnirseAPartida -> assertTrue(decoded is Comando.UnirseAPartida)
                is Comando.UnirseAPartidaAuto -> assertTrue(decoded is Comando.UnirseAPartidaAuto)
                is Comando.RealizarMovimiento -> assertTrue(decoded is Comando.RealizarMovimiento)
            }
        }
    }

    @Test
    fun `test polimorfismo de eventos - sealed classes`() {
        val tablero = Tablero(3, 3)
        val jugador1 = Jugador(id = 1L, nombre = "Player1")
        val juego = Juego("TEST", tablero, listOf(jugador1), EstadoJuego.ESPERANDO_JUGADORES, 0, 0L, 2, 1, TipoJuego.TRES_EN_LINEA)

        val eventos = listOf(
            Evento.PartidaActualizada(juego),
            Evento.Error("Error de prueba")
        )

        eventos.forEach { evento ->
            val jsonString = json.encodeToString(evento)
            val decoded = json.decodeFromString<Evento>(jsonString)

            // Verificar que mantiene el tipo correcto
            when (evento) {
                is Evento.PartidaActualizada -> assertTrue(decoded is Evento.PartidaActualizada)
                is Evento.Error -> assertTrue(decoded is Evento.Error)
            }
        }
    }

    @Test
    fun `test formato ID de partida valido`() {
        val idsValidos = listOf(
            "PARTIDA-ABC12345",
            "PARTIDA-12345678",
            "PARTIDA-A1B2C3D4"
        )

        val idsInvalidos = listOf(
            "PARTIDA-ABC123",      // muy corto
            "PARTIDA-ABC123456",   // muy largo
            "PARTIDA-abc12345",    // minúsculas
            "PARTIDA-ABC-12345",   // guión extra
            "PARTIDAABC12345",     // sin guión
            "PARTIDA-",            // solo prefijo
            "PARTIDA-ABCDEFGH"     // letras solo
        )

        val regex = Regex("PARTIDA-[A-Z0-9]{8}")

        idsValidos.forEach { id ->
            assertTrue(regex.matches(id), "ID válido debería coincidir: $id")
        }

        idsInvalidos.forEach { id ->
            assertTrue(!regex.matches(id), "ID inválido no debería coincidir: $id")
        }
    }

    @Test
    fun `test serializacion con caracteres especiales`() {
        val nombresEspeciales = listOf(
            "Jugador ñoño",
            "Player with spaces",
            "玩家",  // Chino
            "Игрок", // Ruso
            "Játékos", // Húngaro
            "José María"
        )

        nombresEspeciales.forEach { nombre ->
            val jugador = Jugador(id = 1L, nombre = nombre)
            val comando = Comando.CrearPartida(jugador)

            val jsonString = json.encodeToString(comando)
            val decoded = json.decodeFromString<Comando>(jsonString)

            assertTrue(decoded is Comando.CrearPartida)
            assertEquals(nombre, (decoded as Comando.CrearPartida).jugador.nombre)
        }
    }
}