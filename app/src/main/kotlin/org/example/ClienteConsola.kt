package org.example

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Cliente de consola mejorado con interfaz de usuario amigable.
 * Hereda de ClienteBase para compartir funcionalidad común.
 *
 * Características:
 * - Menús interactivos numerados
 * - Validación de entrada del usuario
 * - Visualización clara del tablero con coordenadas
 * - Manejo robusto de errores
 * - Flujo completo del juego desde conexión hasta desconexión
 * - Retroalimentación clara al usuario
 */
class ClienteConsola(
    private val host: String = "127.0.0.1",
    private val puerto: Int = 5050
) : ClienteBase() {
    private val json = JsonConfig.default
    private var socket: Socket? = null
    private var inReader: BufferedReader? = null
    private var outWriter: PrintWriter? = null
    internal override var jugadorActual: Jugador? = null
    internal override var juegoActual: Juego? = null
    private var partidaId: String? = null
    private var ejecutando = false
    private var pool: java.util.concurrent.ExecutorService? = null

    /**
     * Ejecuta el cliente principal con menús interactivos
     */
    fun ejecutar() {
        ejecutando = true
        // Crear un pool de hilos limitado para evitar OOM
        pool = java.util.concurrent.Executors.newFixedThreadPool(2)
        mostrarBienvenida()

        val tiempoInicio = System.currentTimeMillis()
        val tiempoMinimo = 60 * 1000L // 60 segundos mínimo

        while (ejecutando) {
            try {
                mostrarMenuPrincipal()
                val opcion = leerOpcionMenu()
                procesarOpcionPrincipal(opcion)

                // Verificar si ha pasado el tiempo mínimo
                val tiempoActual = System.currentTimeMillis()
                if (tiempoActual - tiempoInicio < tiempoMinimo) {
                    println("\n⏰ El menú debe permanecer visible al menos 1 minuto.")
                    println("Tiempo restante: ${((tiempoMinimo - (tiempoActual - tiempoInicio)) / 1000)} segundos")
                    println("Presione Enter para continuar...")
                    readLine()
                }
            } catch (e: Exception) {
                mostrarError("Error inesperado: ${e.message}")
                println("Presione Enter para continuar...")
                readLine()
            }
        }

        // Solo desconectar al salir completamente del programa
        desconectar()
        pool?.shutdown()
        try {
            pool?.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)
        } catch (_: InterruptedException) {}
        println("¡Gracias por jugar! 👋")
    }

    /**
     * Muestra la pantalla de bienvenida
     */
    private fun mostrarBienvenida() {
        println("=" * 60)
        println("🎮 BIENVENIDO AL JUEGO DE TABLERO MULTIJUGADOR 🎮")
        println("=" * 60)
        println()
        println("Este cliente te permite:")
        println("• Crear nuevas partidas")
        println("• Unirte a partidas existentes")
        println("• Jugar tres en línea y otros juegos")
        println("• Ver el tablero en tiempo real")
        println()
    }

    /**
     * Muestra el menú principal con opciones numeradas
     */
    private fun mostrarMenuPrincipal() {
        println("\n" + "─" * 50)
        println("📋 MENÚ PRINCIPAL")
        println("─" * 50)
        println("1. 🆕 Crear nueva partida")
        println("2. 🔗 Unirse a partida existente")
        println("3. 🎯 Unirse automáticamente a cualquier partida")
        println("4. ❓ Ver reglas del juego")
        println("5. ℹ️  Ayuda y comandos")
        println("6. 🚪 Salir")
        println("─" * 50)
        print("Seleccione una opción (1-6): ")
    }

    /**
     * Lee y valida la opción del menú principal
     */
    private fun leerOpcionMenu(): Int {
        var intentos = 0
        val maxIntentos = 20 // Límite de seguridad

        while (true && intentos < maxIntentos) {
            intentos++
            val entrada = readLine()?.trim()
            val opcion = entrada?.toIntOrNull()
            
            if (opcion != null && opcion in 1..6) {
                return opcion
            }

            println("❌ Opción inválida. Por favor, ingrese un número del 1 al 6.")
            print("Seleccione una opción (1-6): ")
        }

        if (intentos >= maxIntentos) {
            println("⚠️ Se alcanzó el límite máximo de intentos. Saliendo...")
            ejecutando = false
            return 6 // Opción de salir
        }
        return 6 // Fallback por si acaso
    }

    /**
     * Procesa la opción seleccionada del menú principal
     */
    private fun procesarOpcionPrincipal(opcion: Int) {
        when (opcion) {
            1 -> crearPartida()
            2 -> unirseAPartida()
            3 -> unirseAutomaticamente()
            4 -> mostrarReglas()
            5 -> mostrarAyuda()
            6 -> salir()
        }
    }

    /**
     * Crea una nueva partida
     */
    private fun crearPartida() {
        println("\n🆕 CREAR NUEVA PARTIDA")
        println("─" * 30)

        val nombre = solicitarNombreJugador()
        if (nombre == null) return

        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        try {
            conectar()
            val comando = Comando.CrearPartida(jugador)
            val evento = enviarComando(comando)

            when (evento) {
                is Evento.PartidaActualizada -> {
                    juegoActual = evento.juego
                    partidaId = evento.juego.id
                    jugadorActual = jugador

                    println("✅ ¡Partida creada exitosamente!")
                    println("🆔 ID de partida: ${partidaId}")
                    println("👤 Jugador: ${jugador.nombre}")
                    println("⏳ Esperando a que otro jugador se una...")

                    mostrarTablero(evento.juego)
                    iniciarFlujoJuego()
                }
                is Evento.Error -> {
                    mostrarError("Error al crear partida: ${evento.mensaje}")
                }
            }
        } catch (e: Exception) {
            mostrarError("Error de conexión: ${e.message}")
        }
    }

    /**
     * Se une a una partida existente
     */
    private fun unirseAPartida() {
        println("\n🔗 UNIRSE A PARTIDA EXISTENTE")
        println("─" * 30)

        val nombre = solicitarNombreJugador()
        if (nombre == null) return

        val idPartida = solicitarIdPartida()
        if (idPartida == null) return

        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        try {
            conectar()
            val comando = Comando.UnirseAPartida(idPartida, jugador)
            val evento = enviarComando(comando)

            when (evento) {
                is Evento.PartidaActualizada -> {
                    juegoActual = evento.juego
                    partidaId = evento.juego.id
                    jugadorActual = jugador

                    println("✅ ¡Te has unido a la partida exitosamente!")
                    println("🆔 ID de partida: ${partidaId}")
                    println("👤 Jugador: ${jugador.nombre}")

                    mostrarTablero(evento.juego)
                    iniciarFlujoJuego()
                }
                is Evento.Error -> {
                    mostrarError("Error al unirse a la partida: ${evento.mensaje}")
                }
            }
        } catch (e: Exception) {
            mostrarError("Error de conexión: ${e.message}")
        }
    }

    /**
     * Se une automáticamente a cualquier partida disponible
     */
    private fun unirseAutomaticamente() {
        println("\n🎯 UNIRSE AUTOMÁTICAMENTE")
        println("─" * 30)

        val nombre = solicitarNombreJugador()
        if (nombre == null) return

        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        try {
            conectar()
            val comando = Comando.UnirseAPartidaAuto(jugador)
            val evento = enviarComando(comando)

            when (evento) {
                is Evento.PartidaActualizada -> {
                    juegoActual = evento.juego
                    partidaId = evento.juego.id
                    jugadorActual = jugador

                    println("✅ ¡Te has unido automáticamente a una partida!")
                    println("🆔 ID de partida: ${partidaId}")
                    println("👤 Jugador: ${jugador.nombre}")

                    mostrarTablero(evento.juego)
                    iniciarFlujoJuego()
                }
                is Evento.Error -> {
                    mostrarError("Error al unirse automáticamente: ${evento.mensaje}")
                }
            }
        } catch (e: Exception) {
            mostrarError("Error de conexión: ${e.message}")
        }
    }

    /**
     * Solicita el nombre del jugador con validación
     */
    private fun solicitarNombreJugador(): String? {
        var intentos = 0
        val maxIntentos = 20 // Límite de seguridad

        while (true && intentos < maxIntentos) {
            intentos++
            print("👤 Ingrese su nombre (mínimo 2 caracteres): ")
            val nombre = readLine()?.trim()
            
            when {
                nombre.isNullOrBlank() -> {
                    println("❌ El nombre no puede estar vacío.")
                    continue
                }
                nombre.length < 2 -> {
                    println("❌ El nombre debe tener al menos 2 caracteres.")
                    continue
                }
                nombre.length > 20 -> {
                    println("❌ El nombre no puede tener más de 20 caracteres.")
                    continue
                }
                !nombre.matches(Regex("[a-zA-Z0-9\\s]+")) -> {
                    println("❌ El nombre solo puede contener letras, números y espacios.")
                    continue
                }
                else -> return nombre
            }
        }

        if (intentos >= maxIntentos) {
            println("⚠️ Se alcanzó el límite máximo de intentos. Cancelando...")
            return null
        }
        return null
    }

    /**
     * Solicita el ID de partida con validación
     */
    private fun solicitarIdPartida(): String? {
        var intentos = 0
        val maxIntentos = 20 // Límite de seguridad

        while (true && intentos < maxIntentos) {
            intentos++
            print("🆔 Ingrese el ID de la partida (ej: PARTIDA-ABC12345): ")
            val id = readLine()?.trim()
            
            when {
                id.isNullOrBlank() -> {
                    println("❌ El ID no puede estar vacío.")
                    continue
                }
                !id.matches(Regex("PARTIDA-[A-Z0-9]{8}")) -> {
                    println("❌ Formato de ID inválido. Debe ser: PARTIDA-XXXXXXXX")
                    println("   Ejemplo: PARTIDA-ABC12345")
                    continue
                }
                else -> return id
            }
        }

        if (intentos >= maxIntentos) {
            println("⚠️ Se alcanzó el límite máximo de intentos. Cancelando...")
            return null
        }
        return null // Fallback por si acaso
    }

    /**
     * Inicia el flujo principal del juego
     */
    private fun iniciarFlujoJuego() {
        println("\n🎮 ¡JUEGO INICIADO!")
        println("─" * 30)

        var iteraciones = 0
        val maxIteraciones = 1000 // Límite de seguridad para evitar bucles infinitos

        while (juegoActual != null && ejecutando && iteraciones < maxIteraciones) {
            iteraciones++
            try {
                mostrarEstadoJuego()

                if (juegoActual?.estado == EstadoJuego.FINALIZADO) {
                    mostrarJuegoTerminado()
                    break
                }

                if (esMiTurno()) {
                    procesarTurnoJugador()
                } else {
                    esperarTurno()
                }

                // Actualizar estado del juego desde el servidor en segundo plano
                ejecutarEnPool { actualizarEstadoJuego() }

                // Pequeña pausa para evitar consumo excesivo de CPU
                Thread.sleep(100)

            } catch (e: Exception) {
                mostrarError("Error durante el juego: ${e.message}")
                println("Presione Enter para continuar...")
                readLine()
            }
        }

        if (iteraciones >= maxIteraciones) {
            println("⚠️ Se alcanzó el límite máximo de iteraciones. Saliendo del juego...")
            ejecutando = false
        }
    }

    /**
     * Muestra el estado actual del juego
     */
    private fun mostrarEstadoJuego() {
        val juego = juegoActual ?: return
        
        println("\n📊 ESTADO DEL JUEGO")
        println("─" * 20)
        println("🆔 Partida: ${juego.id}")
        println("🎯 Estado: ${traducirEstado(juego.estado)}")
        println("👥 Jugadores: ${juego.jugadores.size}/${juego.maxJugadores}")
        println("🔄 Ronda: ${juego.rondaActual}")
        
        val jugadorEnTurno = juego.jugadorEnTurno
        if (jugadorEnTurno != null) {
            val esMiTurno = jugadorEnTurno.id == jugadorActual?.id
            val indicador = if (esMiTurno) "👑 TÚ" else "⏳"
            println("$indicador Turno: ${jugadorEnTurno.nombre}")
        }
        
        println()
    }

    /**
     * Procesa el turno del jugador actual
     */
    private fun procesarTurnoJugador() {
        val juego = juegoActual ?: return
        
        println("🎯 ES TU TURNO - ${juego.jugadorEnTurno?.nombre}")
        println("─" * 30)
        
        mostrarTablero(juego)
        
        val movimiento = solicitarMovimiento()
        if (movimiento == null) return
        
        try {
            val comando = Comando.RealizarMovimiento(
                idPartida = partidaId!!,
                jugadorId = jugadorActual!!.id,
                fila = movimiento.first,
                columna = movimiento.second,
                contenido = movimiento.third
            )
            
            val evento = enviarComando(comando)
            
            when (evento) {
                is Evento.PartidaActualizada -> {
                    juegoActual = evento.juego
                    println("✅ ¡Movimiento realizado exitosamente!")
                    mostrarTablero(evento.juego)
                }
                is Evento.Error -> {
                    mostrarError("Error en el movimiento: ${evento.mensaje}")
                    println("Intenta con otro movimiento.")
                }
            }
        } catch (e: Exception) {
            mostrarError("Error al enviar movimiento: ${e.message}")
        }
    }

    /**
     * Espera el turno de otros jugadores
     */
    private fun esperarTurno() {
        println("⏳ Esperando turno de otros jugadores...")
        println("Presiona Enter para actualizar el estado del juego.")
        println("Presiona 'q' + Enter para salir del juego.")
        println("Presiona 'm' + Enter para volver al menú principal.")

        val entrada = readLine()?.trim()?.lowercase()
        if (entrada == "q" || entrada == "quit" || entrada == "salir") {
            println("👋 Saliendo del juego...")
            juegoActual = null
            partidaId = null
            ejecutando = false
        } else if (entrada == "m" || entrada == "menu") {
            println("📋 Volviendo al menú principal...")
            juegoActual = null
            partidaId = null
            // No desconectar, mantener conexión para crear/join nuevas partidas
        }
    }

    /**
     * Solicita un movimiento al jugador con validación
     */
    private fun solicitarMovimiento(): Triple<Int, Int, String>? {
        val juego = juegoActual ?: return null

        var intentos = 0
        val maxIntentos = 50 // Límite de seguridad

        while (true && intentos < maxIntentos) {
            intentos++
            println("\n📝 INGRESE SU MOVIMIENTO")
            println("Formato: coordenada contenido (ej: a1 X, b2 O)")
            println("Coordenadas disponibles: ${obtenerCoordenadasDisponibles()}")
            println("Presiona 'q' + Enter para salir del juego")
            println("Presiona 'm' + Enter para volver al menú principal")
            print("Movimiento: ")

            val entrada = readLine()?.trim()?.lowercase()

            if (entrada.isNullOrBlank()) {
                println("❌ No se ingresó ningún movimiento.")
                continue
            }

            // Opción de salir
            if (entrada == "q" || entrada == "quit" || entrada == "salir") {
                println("👋 Saliendo del juego...")
                juegoActual = null
                partidaId = null
                ejecutando = false
                return null
            }

            // Opción de volver al menú
            if (entrada == "m" || entrada == "menu") {
                println("📋 Volviendo al menú principal...")
                juegoActual = null
                partidaId = null
                // No desconectar, mantener conexión
                return null
            }

            val partes = entrada.split("\\s+".toRegex())
            if (partes.size != 2) {
                println("❌ Formato inválido. Use: coordenada contenido")
                println("   Ejemplo: a1 X")
                continue
            }

            val coordenada = partes[0]
            val contenido = partes[1]

            // Validar coordenada
            val posicion = parsearCoordenada(coordenada)
            if (posicion == null) {
                println("❌ Coordenada inválida: $coordenada")
                println("   Use formato: letra + número (ej: a1, b2, c3)")
                continue
            }

            // Validar contenido
            if (contenido.length != 1 || !contenido.matches(Regex("[XO♔♕♖♗♘♙♚♛♜♝♞♟]"))) {
                println("❌ Contenido inválido: $contenido")
                println("   Use: X, O, o símbolos de ajedrez")
                continue
            }

            // Validar que la posición esté disponible
            if (!juego.posicionDisponible(posicion.first, posicion.second)) {
                println("❌ La posición ${coordenada.uppercase()} ya está ocupada.")
                continue
            }

            return Triple(posicion.first, posicion.second, contenido.uppercase())
        }

        if (intentos >= maxIntentos) {
            println("⚠️ Se alcanzó el límite máximo de intentos. Cancelando movimiento...")
            return null
        }
        return null // Fallback por si acaso
    }

    /**
     * Muestra el tablero con coordenadas ASCII
     */
    private fun mostrarTablero(juego: Juego) {
        println("\n🎯 TABLERO DE JUEGO")
        println("─" * 30)
        
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
        println("─" * 30)
    }

    /**
     * Obtiene las coordenadas disponibles en formato legible
     */
    private fun obtenerCoordenadasDisponibles(): String {
        val juego = juegoActual ?: return ""
        val disponibles = juego.posicionesDisponibles()
        return disponibles.take(5).joinToString(", ") { 
            "${('a' + it.second).uppercase()}${it.first + 1}" 
        } + if (disponibles.size > 5) "..." else ""
    }

    /**
     * Parsea una coordenada en formato "a1" a posición (fila, columna)
     */
    private fun parsearCoordenada(coordenada: String): Pair<Int, Int>? {
        if (coordenada.length != 2) return null
        
        val letra = coordenada[0]
        val numero = coordenada[1]
        
        if (!letra.isLetter() || !numero.isDigit()) return null
        
        val columna = letra.lowercaseChar() - 'a'
        val fila = numero.digitToIntOrNull()?.minus(1) ?: return null
        
        val juego = juegoActual
        if (juego != null && !juego.tablero.coordenadasValidas(fila, columna)) {
            return null
        }
        
        return Pair(fila, columna)
    }

    /**
     * Verifica si es el turno del jugador actual
     */
    private fun esMiTurno(): Boolean {
        val juego = juegoActual ?: return false
        val jugadorEnTurno = juego.jugadorEnTurno ?: return false
        return jugadorEnTurno.id == jugadorActual?.id
    }

    /**
     * Actualiza el estado del juego desde el servidor
     */
    private fun actualizarEstadoJuego() {
        val partidaId = this.partidaId ?: return

        try {
            // Enviar comando para obtener estado actual de la partida
            val comando = Comando.UnirseAPartida(partidaId, jugadorActual!!)
            val evento = enviarComando(comando)

            when (evento) {
                is Evento.PartidaActualizada -> {
                    juegoActual = evento.juego
                }
                is Evento.Error -> {
                    println("⚠️ Error al actualizar estado: ${evento.mensaje}")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Error de conexión al actualizar estado: ${e.message}")
        }
    }

    /**
     * Ejecuta una tarea en el pool de hilos para evitar bloqueos
     */
    private fun ejecutarEnPool(tarea: () -> Unit) {
        pool?.execute {
            try {
                tarea()
            } catch (e: Exception) {
                println("⚠️ Error en tarea en segundo plano: ${e.message}")
            }
        }
    }

    /**
     * Muestra cuando el juego ha terminado
     */
    private fun mostrarJuegoTerminado() {
        val juego = juegoActual ?: return

        println("\n🏁 ¡JUEGO TERMINADO!")
        println("─" * 20)
        println("Estado final: ${traducirEstado(juego.estado)}")
        mostrarTablero(juego)

        println("\nPresione Enter para volver al menú principal...")
        readLine()

        // Limpiar estado del juego pero mantener conexión
        juegoActual = null
        partidaId = null
        // No desconectar automáticamente para permitir crear/join nuevas partidas
    }

    /**
     * Traduce el estado del juego a texto legible
     */
    private fun traducirEstado(estado: EstadoJuego): String {
        return when (estado) {
            EstadoJuego.ESPERANDO_JUGADORES -> "Esperando jugadores"
            EstadoJuego.EN_CURSO -> "En curso"
            EstadoJuego.FINALIZADO -> "Finalizado"
            EstadoJuego.PAUSADO -> "Pausado"
            EstadoJuego.CANCELADO -> "Cancelado"
        }
    }

    /**
     * Muestra las reglas del juego
     */
    private fun mostrarReglas() {
        println("\n📖 REGLAS DEL JUEGO")
        println("─" * 30)
        println("🎯 TRES EN LÍNEA:")
        println("• Objetivo: Formar una línea de 3 símbolos iguales")
        println("• Líneas válidas: horizontal, vertical o diagonal")
        println("• Símbolos: X (jugador 1) y O (jugador 2)")
        println("• Turnos alternados entre jugadores")
        println()
        println("♟️ AJEDREZ:")
        println("• Objetivo: Capturar el rey del oponente")
        println("• Cada pieza tiene movimientos específicos")
        println("• Símbolos: ♔♕♖♗♘♙ (blancas) y ♚♛♜♝♞♟ (negras)")
        println()
        println("📝 COORDENADAS:")
        println("• Columnas: A, B, C, D, E, F, G, H")
        println("• Filas: 1, 2, 3, 4, 5, 6, 7, 8")
        println("• Ejemplo: A1, B2, C3")
        println()
        println("Presione Enter para continuar...")
        readLine()
    }

    /**
     * Muestra la ayuda y comandos disponibles
     */
    private fun mostrarAyuda() {
        println("\nℹ️ AYUDA Y COMANDOS")
        println("─" * 30)
        println("🎮 COMANDOS PRINCIPALES:")
        println("• 1-6: Seleccionar opción del menú")
        println("• Enter: Confirmar entrada")
        println("• Ctrl+C: Salir del programa")
        println()
        println("📝 FORMATO DE MOVIMIENTOS:")
        println("• Tres en línea: 'a1 X' (coordenada + símbolo)")
        println("• Ajedrez: 'e2 e4' (origen + destino)")
        println()
        println("🔧 SOLUCIÓN DE PROBLEMAS:")
        println("• Error de conexión: Verificar que el servidor esté ejecutándose")
        println("• Movimiento inválido: Verificar coordenadas y formato")
        println("• Partida no encontrada: Verificar ID de partida")
        println()
        println("📞 SOPORTE:")
        println("• Servidor: $host:$puerto")
        println("• Protocolo: JSON sobre TCP")
        println()
        println("Presione Enter para continuar...")
        readLine()
    }

    /**
     * Sale del programa
     */
    private fun salir() {
        println("\n¿Está seguro de que desea salir? (s/n): ")
        val respuesta = readLine()?.trim()?.lowercase()
        if (respuesta == "s" || respuesta == "si" || respuesta == "sí" || respuesta == "y" || respuesta == "yes") {
            ejecutando = false
            desconectar() // Solo desconectar al salir del programa
        }
    }

    /**
     * Conecta al servidor
     */
    fun conectar() {
        println("Intentando conectar")
        if (socket?.isConnected == true) return
        
        try {
            println("🔌 Conectando al servidor $host:$puerto...")
            socket = Socket(host, puerto)
            socket?.soTimeout = 30000 // 30 segundos de timeout
            inReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            outWriter = PrintWriter(socket!!.getOutputStream(), true)
            println("✅ Conectado al servidor $host:$puerto")
        } catch (e: Exception) {
            throw Exception("No se pudo conectar al servidor: ${e.message}")
        }
    }

    /**
     * Desconecta del servidor
     */
    fun desconectar() {
        try {
            outWriter?.close()
            inReader?.close()
            socket?.close()
            println("🔌 Desconectado del servidor")
        } catch (e: Exception) {
            // Ignorar errores de desconexión
        }
    }

    /**
     * Envía un comando al servidor y recibe la respuesta
     */
    fun enviarComando(comando: Comando): Evento {
        val socket = this.socket ?: throw Exception("No hay conexión al servidor")
        val outWriter = this.outWriter ?: throw Exception("No hay escritor disponible")
        val inReader = this.inReader ?: throw Exception("No hay lector disponible")
        
        try {
            val jsonComando = json.encodeToString(comando)
            outWriter.println(jsonComando)
            
            val respuesta = inReader.readLine()
                ?: throw Exception("No se recibió respuesta del servidor (timeout o conexión cerrada)")
            
            return json.decodeFromString<Evento>(respuesta)
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Timeout al comunicarse con el servidor")
        } catch (e: java.net.SocketException) {
            throw Exception("Conexión perdida con el servidor")
        }
    }

    /**
     * Implementa el método abstracto de ClienteBase
     * Polimorfismo: cada tipo de cliente muestra errores de manera diferente
     */
    override fun mostrarError(mensaje: String) {
        println("\n❌ ERROR: $mensaje")
        println("─" * 50)
    }
}

/**
 * Extensión para repetir strings
 */
private operator fun String.times(n: Int): String = this.repeat(n)

