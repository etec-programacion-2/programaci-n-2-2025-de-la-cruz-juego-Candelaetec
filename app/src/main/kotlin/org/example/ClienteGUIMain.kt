package org.example

import javafx.application.Application

/**
 * Punto de entrada principal para la aplicación GUI del cliente de juego.
 *
 * Esta clase lanza la aplicación JavaFX que proporciona una interfaz gráfica
 * para interactuar con el servidor de juegos de tablero multijugador.
 */
class ClienteGUIMain {

    fun ejecutar(args: Array<String>) {
        // Lanzar la aplicación JavaFX
        Application.launch(ClienteGUI::class.java, *args)
    }
}

fun main(args: Array<String>) {
    val gui = ClienteGUIMain()
    gui.ejecutar(args)
}