# 🎮 Juego de Tablero Multijugador - Guía Completa

Esta guía contiene **TODO** lo necesario para ejecutar el sistema de juego multijugador con clientes consola y GUI compatibles.

## 📋 Tabla de Contenidos

- [Requisitos del Sistema](#requisitos-del-sistema)
- [Instalación](#instalación)
- [Primeros Pasos](#primeros-pasos)
- [Ejecutar el Servidor](#ejecutar-el-servidor)
- [Cliente Consola](#cliente-consola)
- [Cliente GUI](#cliente-gui)
- [Jugar con Clientes Mixtos](#jugar-con-clientes-mixtos)
- [Testing y Verificación](#testing-y-verificación)
- [Solución de Problemas](#solución-de-problemas)
- [Arquitectura del Sistema](#arquitectura-del-sistema)

## 🔧 Requisitos del Sistema

### Software Necesario
- **Java 21** o superior instalado
- **Gradle** (viene incluido en el proyecto)
- **Terminal** o línea de comandos
- **Sistema operativo**: Linux, macOS, o Windows

### Verificar Instalación
```bash
# Verificar Java
java -version
# Debe mostrar: Java 21.x.x

# Verificar que estamos en el directorio correcto
ls -la
# Debe mostrar: gradlew, app/, test-scripts/, etc.
```

## 📦 Instalación

### Paso 1: Clonar/Descargar el Proyecto
```bash
# Si clonaste el repositorio, ya tienes todo
# Si descargaste un ZIP, descomprímelo
unzip programaci-n-2-2025-de-la-cruz-juego-Candelaetec.zip
cd programaci-n-2-2025-de-la-cruz-juego-Candelaetec
```

### Paso 2: Verificar Estructura del Proyecto
```bash
ls -la
```
Debes ver:
```
drwxr-xr-x  .git/
-rw-r--r--  .gitignore
-rw-r--r--  gradlew
-rw-r--r--  gradlew.bat
drwxr-xr-x  app/
drwxr-xr-x  test-scripts/
-rw-r--r--  README.md
-rw-r--r--  README_TESTING.md
```

### Paso 3: Construir el Proyecto
```bash
# Construir todo el proyecto
./gradlew build

# Verificar que compiló correctamente
echo $?
# Debe mostrar: 0
```

## 🚀 Primeros Pasos

### Ejecutar Demo Rápida
```bash
# Ejecutar la aplicación principal (demo)
./gradlew run
```
Esto ejecutará una simulación del juego sin servidor.

### Verificar que Todo Funciona
```bash
# Ejecutar pruebas básicas
./gradlew test --tests "*MensajesTest*"

# Si pasan, el sistema está funcionando
```

## 🖥️ Ejecutar el Servidor

### Paso 1: Abrir Terminal para el Servidor
```bash
# Terminal 1
cd /ruta/al/proyecto/programaci-n-2-2025-de-la-cruz-juego-Candelaetec
```

### Paso 2: Iniciar el Servidor
```bash
./gradlew runServer
```

### Paso 3: Verificar que el Servidor Está Ejecutándose
Debes ver en la terminal:
```
[INFO] Iniciando servidor de juego multijugador
[INFO] Servidor: Servidor escuchando en puerto 5050...
```

### Paso 4: Mantener el Servidor Ejecutándose
**IMPORTANTE:** Mantén esta terminal abierta. El servidor debe estar ejecutándose para que los clientes puedan conectarse.

## 💻 Cliente Consola

### Opción 1: Menú Interactivo (Recomendado)
```bash
# Terminal 2
cd /ruta/al/proyecto/programaci-n-2-2025-de-la-cruz-juego-Candelaetec
./gradlew run
```

### Opción 2: Cliente Consola Simple
```bash
# Crear partida
./gradlew runClient --args="--cmd=create --name=Jugador1"

# Unirse automáticamente
./gradlew runClient --args="--cmd=joinAuto --name=Jugador2"

# Realizar movimiento
./gradlew runClient --args="--cmd=move --id=PARTIDA-ABC12345 --playerId=1 --fila=0 --columna=0 --contenido=X"
```

### Navegación del Menú Consola

1. **Pantalla de Bienvenida**: Presiona Enter
2. **Menú Principal**:
   - `1`: Crear nueva partida
   - `2`: Unirse a partida existente
   - `3`: Unirse automáticamente
   - `4`: Ver reglas
   - `5`: Ayuda
   - `6`: Salir

3. **Crear Partida**:
   - Ingresa tu nombre (2-20 caracteres)
   - Espera a que otro jugador se una

4. **Unirse a Partida**:
   - Ingresa tu nombre
   - Ingresa ID de partida (formato: PARTIDA-XXXXXXXX)

5. **Durante el Juego**:
   - Espera tu turno
   - Ingresa movimiento: `a1 X` (coordenada + símbolo)
   - `q` + Enter: Salir del juego
   - `m` + Enter: Volver al menú

## 🖼️ Cliente GUI

### Paso 1: Ejecutar Cliente GUI
```bash
# Terminal 3
cd /ruta/al/proyecto/programaci-n-2-2025-de-la-cruz-juego-Candelaetec
./gradlew runGUI
```

### Paso 2: Configurar Conexión
En la ventana que se abre:
1. **Nombre**: Ingresa tu nombre de jugador
2. **Host**: `127.0.0.1` (localhost)
3. **Puerto**: `5050`
4. **Botones**:
   - 🆕 **Crear Nueva Partida**: Crea una partida y espera
   - 🔗 **Unirse a Partida**: Pide ID de partida
   - 🎯 **Unirse Automáticamente**: Busca partida disponible

### Paso 3: Jugar en GUI
- **Tablero**: Celdas coloreadas (clara/oscura)
- **Información**: Estado del juego y turno actual
- **Mensajes**: Log de eventos del juego
- **Botón**: "📋 Volver al Menú" para regresar

## 🎯 Jugar con Clientes Mixtos

### Escenario 1: Un Jugador Consola + Un Jugador GUI

#### Terminal 1: Servidor
```bash
./gradlew runServer
```

#### Terminal 2: Cliente Consola
```bash
./gradlew run
# 1. Crear nueva partida
# Ingresa nombre: Alice
```

#### Terminal 3: Cliente GUI
```bash
./gradlew runGUI
# Nombre: Bob
# Host: 127.0.0.1, Puerto: 5050
# Click: 🎯 Unirse Automáticamente
```

### Escenario 2: Dos Jugadores Consola

#### Terminal 1: Servidor
```bash
./gradlew runServer
```

#### Terminal 2: Jugador 1
```bash
./gradlew run
# 1. Crear nueva partida
# Nombre: Player1
```

#### Terminal 3: Jugador 2
```bash
./gradlew run
# 3. Unirse automáticamente
# Nombre: Player2
```

### Escenario 3: Múltiples Jugadores

```bash
# Hacer ejecutable el script
chmod +x test-scripts/run-mixed-clients.sh

# Ejecutar escenario interactivo
./test-scripts/run-mixed-clients.sh
```

## 🧪 Testing y Verificación

### Ejecutar Todas las Pruebas
```bash
./gradlew test
```

### Pruebas Específicas
```bash
# Serialización de mensajes
./gradlew test --tests "*MensajesTest*"

# Integración del servidor
./gradlew test --tests "*IntegracionTest*"

# Compatibilidad entre clientes
./gradlew test --tests "*ClienteHeterogeneoTest*"

# Manejo de desconexiones
./gradlew test --tests "*DesconexionTest*"
```

### Verificar Consistencia
1. **Estados del Juego**: Ambos clientes deben mostrar el mismo tablero
2. **Turnos**: Ambos clientes deben mostrar el mismo jugador actual
3. **Movimientos**: Las jugadas deben aparecer en ambos clientes
4. **Logs del Servidor**: Monitorea la terminal del servidor

## 🔧 Solución de Problemas

### Problema: "Command not found: ./gradlew"
```bash
# Asegurarse de que estamos en el directorio correcto
pwd
# Debe terminar en: programaci-n-2-2025-de-la-cruz-juego-Candelaetec

# Dar permisos de ejecución
chmod +x gradlew
```

### Problema: "Java version X is not supported"
```bash
# Verificar versión de Java
java -version

# Si es menor a 21, instalar Java 21+
# En Ubuntu/Debian:
sudo apt update
sudo apt install openjdk-21-jdk
```

### Problema: "Address already in use"
```bash
# Matar procesos en puerto 5050
lsof -ti:5050 | xargs kill -9

# O cambiar puerto en el código
```

### Problema: "BUILD FAILED"
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build

# Si persiste, verificar archivos
find . -name "*.kt" -exec grep -l "error" {} \;
```

### Problema: Cliente GUI no se abre
```bash
# Verificar JavaFX
./gradlew dependencies | grep javafx

# Si no está, verificar build.gradle.kts
cat app/build.gradle.kts | grep javafx
```

### Problema: "Partida no encontrada"
- Verificar que el servidor esté ejecutándose
- Copiar exactamente el ID de partida (PARTIDA-XXXXXXXX)
- Intentar unirse automáticamente en su lugar

### Problema: "Connection refused"
- Verificar que el servidor esté en puerto 5050
- Verificar que no hay firewall bloqueando
- Intentar con `telnet 127.0.0.1 5050`

## 🏗️ Arquitectura del Sistema

### Componentes Principales

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Cliente GUI   │    │   Cliente       │    │   Servidor      │
│   (JavaFX)      │    │   Consola       │    │   (Sockets)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │ ServicioPartidas│
                    │   (Singleton)   │
                    └─────────────────┘
```

### Protocolo de Comunicación

#### Comandos (Cliente → Servidor)
```json
// Crear partida
{"tipo":"org.example.Comando.CrearPartida","jugador":{"id":123,"nombre":"Alice"}}

// Unirse a partida
{"tipo":"org.example.Comando.UnirseAPartida","idPartida":"PARTIDA-ABC123","jugador":{...}}

// Movimiento
{"tipo":"org.example.Comando.RealizarMovimiento","idPartida":"PARTIDA-ABC123","jugadorId":1,"fila":0,"columna":0,"contenido":"X"}
```

#### Eventos (Servidor → Cliente)
```json
// Partida actualizada
{"tipo":"org.example.Evento.PartidaActualizada","juego":{...}}

// Error
{"tipo":"org.example.Evento.Error","mensaje":"Partida no encontrada"}
```

### Estados del Juego
- **ESPERANDO_JUGADORES**: Esperando que se unan más jugadores
- **EN_CURSO**: Juego activo, aceptando movimientos
- **FINALIZADO**: Juego terminado
- **PAUSADO**: Juego pausado temporalmente
- **CANCELADO**: Juego cancelado

### Logging del Sistema

#### Niveles de Log
- **DEBUG**: Información detallada para desarrollo
- **INFO**: Eventos importantes del sistema
- **WARN**: Advertencias que no detienen el sistema
- **ERROR**: Errores que requieren atención

#### Ver Logs del Servidor
```
[INFO] Servidor: Nueva conexión aceptada -> 127.0.0.1
[DEBUG] RECIBIDO mensaje #1: {"tipo":"CrearPartida",...}
[INFO] Juego [PARTIDA-ABC123]: Estado cambió de ESPERANDO_JUGADORES a EN_CURSO
[DEBUG] ENVIADO respuesta #1: {"tipo":"PartidaActualizada",...}
```

## 📚 Referencias Adicionales

- **[README_TESTING.md](README_TESTING.md)**: Guía completa de testing
- **[README_CLIENTE_CONSOLA.md](README_CLIENTE_CONSOLA.md)**: Documentación específica del cliente consola

## 🎉 ¡Listo para Jugar!

Ahora tienes un sistema completo de juego multijugador con:

- ✅ **Servidor robusto** con logging detallado
- ✅ **Cliente consola** con menú interactivo
- ✅ **Cliente GUI** moderno con JavaFX
- ✅ **Compatibilidad mixta** entre tipos de cliente
- ✅ **Sistema de testing** completo
- ✅ **Documentación exhaustiva**

¡Disfruta jugando y experimentando con el sistema! 🎮
