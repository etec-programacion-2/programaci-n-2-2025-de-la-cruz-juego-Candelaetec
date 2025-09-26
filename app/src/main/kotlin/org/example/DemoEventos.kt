package org.example

fun main() {
    println("=== DEMOSTRACIÓN DEL SISTEMA DE EVENTOS ===\n")
    
    // ==========================================
    // 1. CONFIGURACIÓN INICIAL - CREACIÓN DE LISTENERS
    // ==========================================
    println("🎭 CONFIGURANDO LISTENERS")
    println("=${"=".repeat(30)}")
    
    // Crear diferentes tipos de listeners
    val logger = LoggerListener()
    val interfazUI = InterfazUsuarioListener()
    val estadisticas = EstadisticasListener()
    val historial = HistorialListener()
    
    // Crear jugadores
    val ana = Jugador(id = 1L, nombre = "Ana")
    val carlos = Jugador(id = 2L, nombre = "Carlos")
    
    println("✅ Listeners creados: Logger, UI, Estadísticas, Historial")
    println("✅ Jugadores creados: ${ana.nombre} y ${carlos.nombre}")
    
    // ==========================================
    // 2. CREAR JUEGO Y REGISTRAR LISTENERS
    // ==========================================
    println("\n🎮 CREANDO JUEGO Y REGISTRANDO LISTENERS")
    println("=${"=".repeat(45)}")
    
    var juego = Juego(
        id = "DEMO-EVENTOS-001",
        filasTablero = 3,
        columnasTablero = 3,
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
        .agregarListener(logger)
        .agregarListener(interfazUI)
        .agregarListener(estadisticas)
        .agregarListener(historial)
    
    println("✅ Juego creado con 4 listeners registrados")
    println("Listeners activos: ${juego.obtenerListeners().size}")
    
    // ==========================================
    // 3. AGREGAR JUGADORES (EVENTOS DE JUGADOR_AGREGADO)
    // ==========================================
    println("\n👥 AGREGANDO JUGADORES")
    println("=${"=".repeat(25)}")
    
    juego = juego.agregarJugador(ana)
    Thread.sleep(500) // Pausa para ver secuencia de eventos
    
    juego = juego.agregarJugador(carlos)
    Thread.sleep(500)
    
    // ==========================================
    // 4. INICIAR JUEGO (EVENTOS DE CAMBIO DE ESTADO Y TURNO)
    // ==========================================
    println("\n🚀 INICIANDO JUEGO")
    println("=${"=".repeat(20)}")
    
    juego = juego.iniciarJuego()
    Thread.sleep(500)
    
    // ==========================================
    // 5. SIMULAR PARTIDA COMPLETA CON EVENTOS
    // ==========================================
    println("\n🎯 SIMULANDO PARTIDA COMPLETA")
    println("=${"=".repeat(33)}")
    
    // Secuencia de movimientos que terminará en victoria para Ana
    val secuenciaMovimientos = listOf(
        Triple(ana, Movimiento.colocacion(0, 0, "X"), "Ana abre en esquina superior izquierda"),
        Triple(carlos, Movimiento.colocacion(1, 1, "O"), "Carlos toma el centro"),
        Triple(ana, Movimiento.colocacion(0, 1, "X"), "Ana continúa en la fila superior"),
        Triple(carlos, Movimiento.colocacion(2, 0, "O"), "Carlos bloquea una diagonal"),
        Triple(ana, Movimiento.colocacion(0, 2, "X"), "Ana completa tres en línea horizontal - ¡GANA!")
    )
    
    for ((index, movimientoInfo) in secuenciaMovimientos.withIndex()) {
        val (jugador, movimiento, descripcion) = movimientoInfo
        
        println("\n--- MOVIMIENTO ${index + 1}/5 ---")
        println("📝 $descripcion")
        
        try {
            Thread.sleep(800) // Pausa dramática
            juego = juego.realizarMovimiento(jugador, movimiento)
            
            // Esperar un poco para que se procesen todos los eventos
            Thread.sleep(500)
            
        } catch (e: Exception) {
            println("💥 Error inesperado: ${e.message}")
            break
        }
        
        // Mostrar estadísticas parciales cada 2 movimientos
        if ((index + 1) % 2 == 0) {
            println("\n📊 Estadísticas parciales:")
            estadisticas.obtenerEstadisticas().forEach { (clave, valor) ->
                println("   • $clave: $valor")
            }
        }
    }
    
    // ==========================================
    // 6. DEMOSTRAR MANEJO DE ERRORES CON EVENTOS
    // ==========================================
    println("\n❌ DEMOSTRANDO MANEJO DE ERRORES")
    println("=${"=".repeat(35)}")
    
    // Intentar movimiento inválido (el juego ya terminó)
    try {
        println("Intentando movimiento después de que el juego terminó...")
        juego.realizarMovimiento(carlos, Movimiento.colocacion(1, 0, "O"))
    } catch (e: Exception) {
        println("✅ Error manejado correctamente")
    }
    
    Thread.sleep(500)
    
    // ==========================================
    // 7. SIMULAR DESCONEXIÓN DE JUGADOR
    // ==========================================
    println("\n🔌 SIMULANDO DESCONEXIÓN DE JUGADOR")
    println("=${"=".repeat(38)}")
    
    println("Carlos se desconecta...")
    juego = juego.actualizarConexionJugador(carlos.id, false)
    Thread.sleep(500)
    
    println("Carlos se reconecta...")
    juego = juego.actualizarConexionJugador(carlos.id, true)
    Thread.sleep(500)
    
    // ==========================================
    // 8. MOSTRAR ESTADÍSTICAS FINALES
    // ==========================================
    println("\n📈 ESTADÍSTICAS FINALES")
    println("=${"=".repeat(26)}")
    
    println("\n🎲 Estadísticas de la partida:")
    estadisticas.obtenerEstadisticas().forEach { (clave, valor) ->
        println("   • $clave: $valor")
    }
    
    println("\n🏃 Movimientos por jugador:")
    estadisticas.obtenerMovimientosPorJugador().forEach { (jugadorId, movimientos) ->
        val nombreJugador = juego.jugadores.find { it.id == jugadorId }?.nombre ?: "Desconocido"
        println("   • $nombreJugador: $movimientos movimientos")
    }
    
    println("\n📚 Historial de la partida:")
    println(historial.obtenerResumenPartida())
    
    println("\n🎯 Eventos registrados en el historial:")
    historial.obtenerHistorial().forEachIndexed { index, evento ->
        val tipoEvento = when (evento.tipo) {
            EventoJuego.TipoEvento.MOVIMIENTO_REALIZADO -> "🎮 Movimiento"
            EventoJuego.TipoEvento.JUEGO_TERMINADO -> "🏁 Juego terminado"
            else -> "📝 ${evento.tipo}"
        }
        println("   ${index + 1}. $tipoEvento - ${evento.jugador?.nombre ?: "Sistema"}")
    }
    
    // ==========================================
    // 9. CREAR LISTENER PERSONALIZADO EN TIEMPO DE EJECUCIÓN
    // ==========================================
    println("\n🎨 DEMOSTRACIÓN DE LISTENER PERSONALIZADO")
    println("=${"=".repeat(42)}")
    
    // Crear un listener personalizado usando expresión lambda
    val listenerPersonalizado = object : ListenerJuegoBase() {
        private var contadorEventos = 0
        
        override fun onMovimiento(juego: Juego, jugador: Jugador, movimiento: Movimiento) {
            contadorEventos++
            println("🎪 [Listener Personalizado] Evento #$contadorEventos: ${jugador.nombre} hizo un movimiento épico!")
        }
        
        override fun onJuegoTerminado(juego: Juego, ganador: Jugador?, razonFinalizacion: String) {
            println("🎪 [Listener Personalizado] ¡Qué partida tan emocionante! Total de eventos: $contadorEventos")
        }
    }
    
    // Crear nuevo juego para demostrar el listener personalizado
    println("Creando nueva partida rápida para probar el listener personalizado...")
    
    var juegoNuevo = Juego(
        id = "DEMO-PERSONALIZADO",
        filasTablero = 3,
        columnasTablero = 3,
        maxJugadores = 2,
        tipoJuego = TipoJuego.TRES_EN_LINEA
    )
        .agregarListener(listenerPersonalizado)
        .agregarJugador(Jugador(3L, "Pedro"))
        .agregarJugador(Jugador(4L, "María"))
        .iniciarJuego()
    
    // Hacer algunos movimientos rápidos
    juegoNuevo = juegoNuevo.realizarMovimiento(
        juegoNuevo.jugadores[0], 
        Movimiento.colocacion(1, 1, "X")
    )
    
    juegoNuevo = juegoNuevo.realizarMovimiento(
        juegoNuevo.jugadores[1], 
        Movimiento.colocacion(0, 0, "O")
    )
    
    // ==========================================
    // 10. DEMOSTRACIÓN DEL DIAGRAMA DE SECUENCIA
    // ==========================================
    println("\n📋 DIAGRAMA DE SECUENCIA (TEXTUAL)")
    println("=${"=".repeat(37)}")
    
    println("""
    SECUENCIA DE EVENTOS PARA UN MOVIMIENTO:
    
    1. Jugador solicita movimiento
       ↓
    2. Juego.realizarMovimiento()
       ↓
    3. MotorJuego.procesarMovimiento()
       ↓
    4. Validaciones y ejecución
       ↓
    5. Juego.notificarMovimiento()
       ↓
    6. Para cada Listener registrado:
       → Logger.onMovimiento()
       → UI.onMovimiento()
       → Estadisticas.onMovimiento()
       → Historial.onMovimiento()
       ↓
    7. Si hay cambio de turno:
       → Juego.notificarCambioTurno()
       ↓
    8. Si el juego termina:
       → Juego.notificarJuegoTerminado()
       ↓
    9. Retorna nuevo estado del juego
    
    """.trimIndent())
    
    // ==========================================
    // 11. RESUMEN Y BENEFICIOS
    // ==========================================
    println("🎉 RESUMEN DEL PATRÓN OBSERVER")
    println("=${"=".repeat(33)}")
    
    println("""
    ✅ BENEFICIOS DEMOSTRADOS:
    
    📦 SEPARACIÓN DE RESPONSABILIDADES:
    • Juego: Gestiona estado y lógica
    • Listeners: Reaccionan a eventos específicos
    • UI, Logger, Estadísticas: Funcionan independientemente
    
    🔄 DESACOPLAMIENTO:
    • Juego no conoce implementaciones específicas de listeners
    • Listeners pueden agregarse/removerse dinámicamente
    • Cambios en un componente no afectan otros
    
    📊 FLEXIBILIDAD:
    • Múltiples listeners pueden reaccionar al mismo evento
    • Nuevas funcionalidades sin modificar código existente
    • Manejo robusto de errores en listeners
    
    🎯 POLIMORFISMO:
    • Interfaz ListenerJuego define contrato común
    • Implementaciones específicas para cada necesidad
    • Fácil extensión con nuevos tipos de listeners
    
    """.trimIndent())
    
    println("🏁 ¡Demostración del sistema de eventos completada!")
}