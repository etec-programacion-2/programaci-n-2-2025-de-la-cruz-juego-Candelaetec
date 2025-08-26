package org.example

/**
 * Enum class que define los posibles estados de una partida
 * 
 * Las enum classes son útiles para representar un conjunto finito y fijo de constantes,
 * proporcionando type safety y evitando el uso de strings o números mágicos.
 */
enum class EstadoJuego {
    ESPERANDO_JUGADORES,  // La partida está creada pero esperando más jugadores
    EN_CURSO,            // La partida está activa y los jugadores están jugando
    FINALIZADO,          // La partida ha terminado
    PAUSADO,             // La partida está temporalmente pausada
    CANCELADO            // La partida fue cancelada antes de completarse
}