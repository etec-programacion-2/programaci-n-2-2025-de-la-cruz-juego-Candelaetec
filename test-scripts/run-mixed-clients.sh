#!/bin/bash

# Script para ejecutar múltiples clientes simultáneamente (consola y GUI)
# para probar escenarios mixtos de juego

set -e

# Configuración
SERVER_HOST="127.0.0.1"
SERVER_PORT="5050"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== PRUEBA DE CLIENTES MIXTOS ==="
echo "Proyecto: $PROJECT_DIR"
echo "Servidor: $SERVER_HOST:$SERVER_PORT"
echo ""

# Función para esperar a que el servidor esté listo
wait_for_server() {
    local max_attempts=30
    local attempt=1

    echo "Esperando a que el servidor esté listo..."
    while [ $attempt -le $max_attempts ]; do
        if nc -z $SERVER_HOST $SERVER_PORT 2>/dev/null; then
            echo "✅ Servidor listo en intento $attempt"
            return 0
        fi
        echo "Intento $attempt/$max_attempts - Servidor no responde..."
        sleep 1
        ((attempt++))
    done

    echo "❌ Servidor no respondió después de $max_attempts intentos"
    return 1
}

# Función para ejecutar cliente en background
run_client() {
    local client_type=$1
    local client_name=$2
    local delay=$3

    sleep $delay

    echo "🚀 Iniciando cliente $client_type: $client_name"

    case $client_type in
        "console")
            # Cliente consola con comandos predefinidos
            (
                cd "$PROJECT_DIR"
                echo "create:$client_name" | ./gradlew runClient --args="--cmd=create --name=$client_name" &
                CLIENT_PID=$!
                echo "Cliente consola PID: $CLIENT_PID"
                wait $CLIENT_PID 2>/dev/null || true
            ) &
            ;;
        "console_join")
            # Cliente consola que se une
            (
                cd "$PROJECT_DIR"
                sleep 2  # Esperar a que haya una partida
                ./gradlew runClient --args="--cmd=joinAuto --name=$client_name" &
                CLIENT_PID=$!
                echo "Cliente consola join PID: $CLIENT_PID"
                wait $CLIENT_PID 2>/dev/null || true
            ) &
            ;;
        "gui")
            # Cliente GUI
            (
                cd "$PROJECT_DIR"
                ./gradlew runGUI &
                GUI_PID=$!
                echo "Cliente GUI PID: $GUI_PID"
                # GUI se ejecuta indefinidamente, no esperamos
            ) &
            ;;
    esac
}

# Función para ejecutar escenario específico
run_scenario() {
    local scenario_name=$1
    local description=$2
    shift 2

    echo ""
    echo "=== ESCENARIO: $scenario_name ==="
    echo "Descripción: $description"
    echo ""

    # Ejecutar clientes según el escenario
    while [ $# -gt 0 ]; do
        local client_spec=$1
        IFS=':' read -r client_type client_name delay <<< "$client_spec"
        delay=${delay:-0}

        run_client "$client_type" "$client_name" "$delay"
        shift
    done

    echo "⏳ Escenario ejecutándose... Presiona Ctrl+C para detener"
    echo "Monitorea los logs del servidor para ver la actividad"
    echo ""

    # Mantener el script vivo
    wait
}

# Función para mostrar menú de escenarios
show_menu() {
    echo "=== ESCENARIOS DE PRUEBA ==="
    echo ""
    echo "1. Un jugador consola + un jugador GUI"
    echo "2. Dos jugadores consola"
    echo "3. Un jugador GUI crea, otro consola se une"
    echo "4. Múltiples clientes consola simultáneos"
    echo "5. Stress test: muchos clientes"
    echo "6. Escenario personalizado"
    echo "7. Ejecutar todas las pruebas unitarias"
    echo "8. Ejecutar todas las pruebas de integración"
    echo ""
    echo "0. Salir"
    echo ""
}

# Función principal
main() {
    cd "$PROJECT_DIR"

    # Verificar que estamos en el directorio correcto
    if [ ! -f "gradlew" ]; then
        echo "❌ Error: No se encuentra gradlew. Ejecuta desde el directorio raíz del proyecto."
        exit 1
    fi

    # Iniciar servidor en background
    echo "🔧 Iniciando servidor..."
    ./gradlew runServer &
    SERVER_PID=$!
    echo "Servidor PID: $SERVER_PID"

    # Esperar a que el servidor esté listo
    if ! wait_for_server; then
        echo "❌ No se pudo iniciar el servidor. Abortando."
        kill $SERVER_PID 2>/dev/null || true
        exit 1
    fi

    # Menú interactivo
    while true; do
        show_menu
        read -p "Selecciona un escenario (0-8): " choice

        case $choice in
            1)
                run_scenario "Consola + GUI" "Un jugador usa consola, otro usa GUI" \
                    "console:Alice:0" \
                    "gui:Bob:3"
                ;;
            2)
                run_scenario "Dos Consolas" "Dos jugadores usando consola" \
                    "console:Player1:0" \
                    "console_join:Player2:1"
                ;;
            3)
                run_scenario "GUI crea, Consola se une" "GUI crea partida, consola se une automáticamente" \
                    "gui:Creator:0" \
                    "console_join:Joiner:5"
                ;;
            4)
                run_scenario "Múltiples Consolas" "Varios clientes consola simultáneos" \
                    "console:Multi1:0" \
                    "console_join:Multi2:1" \
                    "console_join:Multi3:2" \
                    "console_join:Multi4:3"
                ;;
            5)
                run_scenario "Stress Test" "Múltiples clientes para probar concurrencia" \
                    "console:Stress1:0" \
                    "console:Stress2:0" \
                    "console:Stress3:0" \
                    "console_join:Stress4:1" \
                    "console_join:Stress5:1" \
                    "console_join:Stress6:1"
                ;;
            6)
                echo "Escenario personalizado - Modifica el script para añadir tu escenario"
                ;;
            7)
                echo "🧪 Ejecutando pruebas unitarias..."
                ./gradlew test --tests "*MensajesTest*"
                ;;
            8)
                echo "🧪 Ejecutando pruebas de integración..."
                ./gradlew test --tests "*IntegracionTest*"
                ;;
            0)
                echo "👋 Saliendo..."
                break
                ;;
            *)
                echo "❌ Opción inválida. Intenta de nuevo."
                continue
                ;;
        esac

        echo ""
        read -p "Presiona Enter para continuar..."
    done

    # Limpiar procesos
    echo "🧹 Limpiando procesos..."
    kill $SERVER_PID 2>/dev/null || true
    pkill -f "java.*ClienteGUI" 2>/dev/null || true
    pkill -f "java.*ClienteConsola" 2>/dev/null || true

    echo "✅ Script terminado."
}

# Manejar señales para limpieza
trap 'echo ""; echo "🛑 Interrumpiendo..."; kill $SERVER_PID 2>/dev/null || true; pkill -f "java.*Cliente" 2>/dev/null || true; exit 1' INT TERM

# Ejecutar función principal
main "$@"