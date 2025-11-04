package org.example

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.Executors
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Cliente GUI con JavaFX para el juego de tablero multijugador.
 * Hereda de ClienteBase para compartir funcionalidad común.
 *
 * Características:
 * - Interfaz gráfica intuitiva con JavaFX
 * - Representación visual del tablero con GridPane
 * - Actualizaciones en tiempo real desde el servidor
 * - Manejo de eventos de UI con hilos separados
 * - Estilos CSS para mejor apariencia
 */
class ClienteGUI : Application() {

    // Implementa la interfaz ClienteBase a través de composición
    private val clienteBase = object : ClienteBase() {
        override fun mostrarError(mensaje: String) {
            Platform.runLater {
                val alert = Alert(Alert.AlertType.ERROR).apply {
                    title = "Error"
                    headerText = null
                    contentText = mensaje
                }
                alert.showAndWait()
            }
        }
    }

    private val json = JsonConfig.default
    private var socket: Socket? = null
    private var inReader: BufferedReader? = null
    private var outWriter: PrintWriter? = null
    private var jugadorActual: Jugador? = null
    private var juegoActual: Juego? = null
    private var partidaId: String? = null

    // UI Components
    private lateinit var primaryStage: Stage
    private lateinit var tableroView: TableroView
    private lateinit var mensajesArea: TextArea
    private lateinit var turnoLabel: Label
    private lateinit var estadoLabel: Label

    // Executor para operaciones de red
    private val executor = Executors.newCachedThreadPool()

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage

        primaryStage.title = "Juego de Tablero Multijugador"
        primaryStage.minWidth = 800.0
        primaryStage.minHeight = 600.0

        // Crear escena del menú principal
        val menuScene = crearEscenaMenu()
        primaryStage.scene = menuScene
        primaryStage.show()

        // Cargar estilos CSS
        menuScene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")
    }

    /**
     * Crea la escena del menú principal
     */
    private fun crearEscenaMenu(): Scene {
        val root = VBox(20.0).apply {
            padding = Insets(20.0)
            alignment = Pos.CENTER
            styleClass.add("menu-root")
        }

        // Título
        val tituloLabel = Label("🎮 JUEGO DE TABLERO MULTIJUGADOR 🎮").apply {
            font = Font.font("Arial", FontWeight.BOLD, 24.0)
            styleClass.add("titulo")
        }

        // Campo de nombre
        val nombreField = TextField().apply {
            promptText = "Ingresa tu nombre"
            maxWidth = 300.0
            styleClass.add("input-field")
        }

        // Campo de host
        val hostField = TextField("127.0.0.1").apply {
            promptText = "Host del servidor"
            maxWidth = 300.0
            styleClass.add("input-field")
        }

        // Campo de puerto
        val puertoField = TextField("5050").apply {
            promptText = "Puerto del servidor"
            maxWidth = 300.0
            styleClass.add("input-field")
        }

        // Botones
        val crearButton = Button("🆕 Crear Nueva Partida").apply {
            styleClass.add("boton-principal")
            setOnAction {
                val nombre = nombreField.text.trim()
                if (nombre.isNotEmpty()) {
                    conectarYSolicitarAccion(hostField.text, puertoField.text.toIntOrNull() ?: 5050) {
                        crearPartidaGUI(nombre)
                    }
                } else {
                    mostrarAlerta("Error", "Por favor ingresa tu nombre")
                }
            }
        }

        val unirseButton = Button("🔗 Unirse a Partida").apply {
            styleClass.add("boton-principal")
            setOnAction {
                val nombre = nombreField.text.trim()
                if (nombre.isNotEmpty()) {
                    mostrarDialogoUnirse(hostField.text, puertoField.text.toIntOrNull() ?: 5050, nombre)
                } else {
                    mostrarAlerta("Error", "Por favor ingresa tu nombre")
                }
            }
        }

        val automaticoButton = Button("🎯 Unirse Automáticamente").apply {
            styleClass.add("boton-secundario")
            setOnAction {
                val nombre = nombreField.text.trim()
                if (nombre.isNotEmpty()) {
                    conectarYSolicitarAccion(hostField.text, puertoField.text.toIntOrNull() ?: 5050) {
                        unirseAutomaticamenteGUI(nombre)
                    }
                } else {
                    mostrarAlerta("Error", "Por favor ingresa tu nombre")
                }
            }
        }

        val salirButton = Button("🚪 Salir").apply {
            styleClass.add("boton-salir")
            setOnAction {
                Platform.exit()
            }
        }

        root.children.addAll(
            tituloLabel,
            Label("Nombre:").apply { styleClass.add("label-input") },
            nombreField,
            Label("Servidor:").apply { styleClass.add("label-input") },
            HBox(10.0, Label("Host:").apply { styleClass.add("label-input") }, hostField,
                 Label("Puerto:").apply { styleClass.add("label-input") }, puertoField).apply {
                alignment = Pos.CENTER
            },
            crearButton,
            unirseButton,
            automaticoButton,
            salirButton
        )

        return Scene(root, 600.0, 500.0)
    }

    /**
     * Crea la escena del juego
     */
    private fun crearEscenaJuego(): Scene {
        val root = BorderPane().apply {
            padding = Insets(10.0)
            styleClass.add("juego-root")
        }

        // Panel superior - Información del juego
        val infoPanel = VBox(10.0).apply {
            padding = Insets(10.0)
            styleClass.add("info-panel")
        }

        turnoLabel = Label("Esperando jugadores...").apply {
            font = Font.font("Arial", FontWeight.BOLD, 16.0)
            styleClass.add("turno-label")
        }

        estadoLabel = Label("Estado: Esperando").apply {
            styleClass.add("estado-label")
        }

        infoPanel.children.addAll(turnoLabel, estadoLabel)

        // Panel central - Tablero
        tableroView = TableroView(3, 3) // Por defecto 3x3, se actualizará según el juego

        // Panel derecho - Controles y mensajes
        val controlPanel = VBox(10.0).apply {
            padding = Insets(10.0)
            prefWidth = 250.0
            styleClass.add("control-panel")
        }

        val volverMenuButton = Button("📋 Volver al Menú").apply {
            styleClass.add("boton-secundario")
            setOnAction {
                volverAlMenu()
            }
        }

        mensajesArea = TextArea().apply {
            prefHeight = 200.0
            isEditable = false
            styleClass.add("mensajes-area")
        }

        controlPanel.children.addAll(
            Label("Mensajes del Juego:").apply { styleClass.add("label-mensajes") },
            mensajesArea,
            volverMenuButton
        )

        root.top = infoPanel
        root.center = tableroView
        root.right = controlPanel

        return Scene(root, 900.0, 700.0)
    }

    /**
     * Conecta al servidor y ejecuta una acción
     */
    private fun conectarYSolicitarAccion(host: String, puerto: Int, accion: () -> Unit) {
        executor.execute {
            try {
                conectar(host, puerto)
                Platform.runLater {
                    accion()
                }
            } catch (e: Exception) {
                Platform.runLater {
                    mostrarAlerta("Error de Conexión", "No se pudo conectar al servidor: ${e.message}")
                }
            }
        }
    }

    /**
     * Crea una partida desde la GUI
     */
    private fun crearPartidaGUI(nombre: String) {
        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        executor.execute {
            try {
                val comando = Comando.CrearPartida(jugador)
                val evento = enviarComando(comando)

                Platform.runLater {
                    when (evento) {
                        is Evento.PartidaActualizada -> {
                            juegoActual = evento.juego
                            partidaId = evento.juego.id
                            jugadorActual = jugador

                            agregarMensaje("✅ ¡Partida creada exitosamente!")
                            agregarMensaje("🆔 ID: ${partidaId}")
                            agregarMensaje("👤 Jugador: ${jugador.nombre}")
                            agregarMensaje("⏳ Esperando a que otro jugador se una...")

                            mostrarEscenaJuego()
                            iniciarActualizacionEstado()
                        }
                        is Evento.Error -> {
                            mostrarAlerta("Error", "Error al crear partida: ${evento.mensaje}")
                        }
                    }
                }
            } catch (e: Exception) {
                Platform.runLater {
                    mostrarAlerta("Error", "Error al crear partida: ${e.message}")
                }
            }
        }
    }

    /**
     * Se une automáticamente a una partida
     */
    private fun unirseAutomaticamenteGUI(nombre: String) {
        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        executor.execute {
            try {
                val comando = Comando.UnirseAPartidaAuto(jugador)
                val evento = enviarComando(comando)

                Platform.runLater {
                    when (evento) {
                        is Evento.PartidaActualizada -> {
                            juegoActual = evento.juego
                            partidaId = evento.juego.id
                            jugadorActual = jugador

                            agregarMensaje("✅ ¡Te has unido automáticamente a una partida!")
                            agregarMensaje("🆔 ID: ${partidaId}")
                            agregarMensaje("👤 Jugador: ${jugador.nombre}")

                            mostrarEscenaJuego()
                            iniciarActualizacionEstado()
                        }
                        is Evento.Error -> {
                            mostrarAlerta("Error", "Error al unirse automáticamente: ${evento.mensaje}")
                        }
                    }
                }
            } catch (e: Exception) {
                Platform.runLater {
                    mostrarAlerta("Error", "Error al unirse automáticamente: ${e.message}")
                }
            }
        }
    }

    /**
     * Muestra diálogo para unirse a partida específica
     */
    private fun mostrarDialogoUnirse(host: String, puerto: Int, nombre: String) {
        val dialog = TextInputDialog().apply {
            title = "Unirse a Partida"
            headerText = "Ingresa el ID de la partida"
            contentText = "ID de partida:"
        }

        val result = dialog.showAndWait()
        result.ifPresent { idPartida ->
            if (idPartida.isNotBlank()) {
                conectarYSolicitarAccion(host, puerto) {
                    unirseAPartidaGUI(idPartida, nombre)
                }
            }
        }
    }

    /**
     * Se une a una partida específica
     */
    private fun unirseAPartidaGUI(idPartida: String, nombre: String) {
        val jugador = Jugador(
            id = System.currentTimeMillis(),
            nombre = nombre
        )

        executor.execute {
            try {
                val comando = Comando.UnirseAPartida(idPartida, jugador)
                val evento = enviarComando(comando)

                Platform.runLater {
                    when (evento) {
                        is Evento.PartidaActualizada -> {
                            juegoActual = evento.juego
                            partidaId = evento.juego.id
                            jugadorActual = jugador

                            agregarMensaje("✅ ¡Te has unido a la partida exitosamente!")
                            agregarMensaje("🆔 ID: ${partidaId}")
                            agregarMensaje("👤 Jugador: ${jugador.nombre}")

                            mostrarEscenaJuego()
                            iniciarActualizacionEstado()
                        }
                        is Evento.Error -> {
                            mostrarAlerta("Error", "Error al unirse a la partida: ${evento.mensaje}")
                        }
                    }
                }
            } catch (e: Exception) {
                Platform.runLater {
                    mostrarAlerta("Error", "Error al unirse a la partida: ${e.message}")
                }
            }
        }
    }

    /**
     * Muestra la escena del juego
     */
    private fun mostrarEscenaJuego() {
        val juegoScene = crearEscenaJuego()
        juegoScene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")
        primaryStage.scene = juegoScene
        actualizarUI()
    }

    /**
     * Vuelve al menú principal
     */
    private fun volverAlMenu() {
        juegoActual = null
        partidaId = null
        val menuScene = crearEscenaMenu()
        menuScene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")
        primaryStage.scene = menuScene
    }

    /**
     * Actualiza la interfaz de usuario
     */
    private fun actualizarUI() {
        val juego = juegoActual ?: return

        Platform.runLater {
            // Actualizar tablero
            tableroView.actualizarTablero(juego.tablero)

            // Actualizar labels
            estadoLabel.text = "Estado: ${traducirEstado(juego.estado)}"
            turnoLabel.text = juego.jugadorEnTurno?.let { "Turno de: ${it.nombre}" } ?: "Esperando jugadores..."

            // Actualizar tablero si cambió el tamaño
            if (tableroView.filas != juego.tablero.filas || tableroView.columnas != juego.tablero.columnas) {
                tableroView = TableroView(juego.tablero.filas, juego.tablero.columnas)
                (primaryStage.scene.root as BorderPane).center = tableroView
            }
        }
    }

    /**
     * Inicia la actualización periódica del estado del juego
     */
    private fun iniciarActualizacionEstado() {
        executor.execute {
            while (juegoActual != null && partidaId != null) {
                try {
                    Thread.sleep(1000) // Actualizar cada segundo

                    val comando = Comando.UnirseAPartida(partidaId!!, jugadorActual!!)
                    val evento = enviarComando(comando)

                    when (evento) {
                        is Evento.PartidaActualizada -> {
                            val juegoAnterior = juegoActual
                            juegoActual = evento.juego

                            // Solo actualizar UI si el juego cambió
                            if (juegoAnterior != evento.juego) {
                                actualizarUI()
                            }
                        }
                        is Evento.Error -> {
                            agregarMensaje("⚠️ Error al actualizar estado: ${evento.mensaje}")
                        }
                    }
                } catch (e: Exception) {
                    agregarMensaje("⚠️ Error de conexión: ${e.message}")
                    break
                }
            }
        }
    }

    /**
     * Agrega un mensaje al área de mensajes
     */
    private fun agregarMensaje(mensaje: String) {
        Platform.runLater {
            mensajesArea.appendText("$mensaje\n")
        }
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
     * Implementa el método abstracto de ClienteBase
     * Polimorfismo: el cliente GUI muestra errores con diálogos
     */
    private fun mostrarAlerta(titulo: String, mensaje: String) {
        val alert = Alert(Alert.AlertType.ERROR).apply {
            this.title = titulo
            headerText = null
            contentText = mensaje
        }
        alert.showAndWait()
    }

    /**
     * Conecta al servidor
     */
    private fun conectar(host: String, puerto: Int) {
        if (socket?.isConnected == true) return

        socket = Socket(host, puerto)
        socket?.soTimeout = 30000
        inReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        outWriter = PrintWriter(socket!!.getOutputStream(), true)
    }

    /**
     * Desconecta del servidor
     */
    private fun desconectar() {
        try {
            outWriter?.close()
            inReader?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignorar errores de desconexión
        }
    }

    /**
     * Envía un comando al servidor y recibe la respuesta
     */
    private fun enviarComando(comando: Comando): Evento {
        val socket = this.socket ?: throw Exception("No hay conexión al servidor")
        val outWriter = this.outWriter ?: throw Exception("No hay escritor disponible")
        val inReader = this.inReader ?: throw Exception("No hay lector disponible")

        val jsonComando = json.encodeToString(comando)
        outWriter.println(jsonComando)

        val respuesta = inReader.readLine()
            ?: throw Exception("No se recibió respuesta del servidor")

        return json.decodeFromString<Evento>(respuesta)
    }

    override fun stop() {
        executor.shutdown()
        desconectar()
    }
}

/**
 * Componente personalizado para representar el tablero del juego
 */
class TableroView(val filas: Int, val columnas: Int) : GridPane() {

    private val botones: Array<Array<Button>> = Array(filas) { Array(columnas) { Button() } }

    init {
        styleClass.add("tablero")
        padding = Insets(10.0)
        hgap = 2.0
        vgap = 2.0

        // Crear botones para cada celda
        for (fila in 0 until filas) {
            for (columna in 0 until columnas) {
                val button = Button().apply {
                    prefWidth = 60.0
                    prefHeight = 60.0
                    font = Font.font("Arial", FontWeight.BOLD, 20.0)
                    styleClass.add("celda-tablero")

                    // Color alternado para las celdas
                    if ((fila + columna) % 2 == 0) {
                        styleClass.add("celda-clara")
                    } else {
                        styleClass.add("celda-oscura")
                    }
                }

                botones[fila][columna] = button
                add(button, columna, fila)
            }
        }
    }

    /**
     * Actualiza el tablero con el estado actual del juego
     */
    fun actualizarTablero(tablero: Tablero) {
        for (fila in 0 until minOf(filas, tablero.filas)) {
            for (columna in 0 until minOf(columnas, tablero.columnas)) {
                val celda = tablero.obtenerCelda(fila, columna)
                val button = botones[fila][columna]
                button.text = celda.contenido ?: ""
            }
        }
    }
}