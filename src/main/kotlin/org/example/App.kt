package org.example

open class Entidad(val id: Int, var x: Int, var y: Int)

class Personaje(id: Int, x: Int, y: Int, val nombre: String) : Entidad(id, x, y) {
    fun mover(dx: Int, dy: Int, ancho: Int, alto: Int) {
        val nuevoX = x + dx
        val nuevoY = y + dy
        if (nuevoX in 0 until ancho && nuevoY in 0 until alto) {
            x = nuevoX
            y = nuevoY
        } else {
            println("❌ Movimiento inválido: fuera del mapa")
        }
    }
}

class Mundo(val ancho: Int, val alto: Int) {
    private val entidades = mutableListOf<Entidad>()
    fun agregar(entidad: Entidad) { entidades.add(entidad) }
    fun mostrar() {
        for (y in 0 until alto) {
            for (x in 0 until ancho) {
                val e = entidades.find { it.x == x && it.y == y }
                if (e is Personaje) print("😀 ") else print("· ")
            }
            println()
        }
    }
}

fun main() {
    val mundo = Mundo(5, 5)
    val jugador = Personaje(1, 2, 2, "Candela")
    mundo.agregar(jugador)

    var input: String
    do {
        mundo.mostrar()
        println("\nMover (W/A/S/D) o Q para salir:")
        input = readlnOrNull()?.trim()?.uppercase() ?: ""
        val opcion = if (input.isNotEmpty()) input[0].toString() else ""
        when (opcion) {
            "W" -> jugador.mover(0, -1, mundo.ancho, mundo.alto)
            "S" -> jugador.mover(0, 1, mundo.ancho, mundo.alto)
            "A" -> jugador.mover(-1, 0, mundo.ancho, mundo.alto)
            "D" -> jugador.mover(1, 0, mundo.ancho, mundo.alto)
        }
    } while (input != "Q")

    println("👋 ¡Chau, ${jugador.nombre}!")
}

