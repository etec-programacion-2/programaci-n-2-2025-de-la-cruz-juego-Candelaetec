package org.example

/**
 * Clase principal que lanza la aplicación según los argumentos proporcionados.
 * Cumple con el requisito de que la función main no exceda las 10 líneas.
 */
class MainLauncher {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val launcher = MainLauncher()
            launcher.procesarArgumentos(args)
        }
    }

    /**
     * Procesa los argumentos de línea de comandos y delega la ejecución
     * a la clase apropiada según el modo solicitado.
     */
    private fun procesarArgumentos(args: Array<String>) {
        when {
            args.isEmpty() -> ClienteConsolaLauncher().ejecutar()
            args.contains("--server") -> ServidorLauncher().ejecutar()
            args.contains("--gui") -> ClienteGUILauncher().ejecutar()
            args.contains("--demo") -> DemoLauncher().ejecutar()
            else -> ClienteConsolaLauncher().ejecutar()
        }
    }
}

/**
 * Launcher para el cliente de consola interactivo.
 */
class ClienteConsolaLauncher {
    fun ejecutar() {
        val cliente = ClienteConsola()
        cliente.ejecutar()
    }
}

/**
 * Launcher para el servidor de sockets.
 */
class ServidorLauncher {
    fun ejecutar() {
        val servidor = ServidorMain()
        servidor.iniciar()
    }
}

/**
 * Launcher para el cliente GUI.
 */
class ClienteGUILauncher {
    fun ejecutar() {
        val gui = ClienteGUIMain()
        gui.ejecutar(emptyArray())
    }
}

/**
 * Launcher para la demo del juego.
 */
class DemoLauncher {
    fun ejecutar() {
        val app = App()
        app.ejecutar()
    }
}