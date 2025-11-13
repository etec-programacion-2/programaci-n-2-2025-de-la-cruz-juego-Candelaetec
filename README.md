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
- **Gradle 9.0.0** (viene incluido en el proyecto)
- **Terminal** o línea de comandos
- **Sistema operativo**: Linux, macOS, o Windows

### Verificar Instalación
```bash
# Verificar Java
java -version
# Debe mostrar: Java 21.x.x o superior (actualmente probado con Java 24.0.2)

# Verificar Gradle
./gradlew --version
# Debe mostrar: Gradle 9.0.0

# Verificar que estamos en el directorio correcto
ls -la
# Debe mostrar: gradlew, app/, test-scripts/, etc.
```

## 📦 Instalación

### Paso 1: Clonar/Descargar el Proyecto
```bash
git@github.com:etec-programacion-2/programaci-n-2-2025-de-la-cruz-juego-Candelaetec.git
cd programaci-n-2-2025-de-la-cruz-juego-Candelaetec.git
```

### Paso 2: Verificar Estructura del Proyecto
```bash
ls -la
```
Debes ver exactamente estos archivos y directorios:
```
drwxr-xr-x  .git/                    # Repositorio Git
-rw-r--r--  .gitignore               # Archivos ignorados por Git
-rw-r--r--  gradlew                  # Script Gradle para Linux/macOS
-rw-r--r--  gradlew.bat              # Script Gradle para Windows
-rw-r--r--  gradle.properties        # Configuración de Gradle
-rw-r--r--  settings.gradle.kts      # Configuración del proyecto Gradle
-rw-r--r--  README.md                # Esta guía
-rw-r--r--  README_TESTING.md        # Guía de testing
-rw-r--r--  README_CLIENTE_CONSOLA.md # Documentación cliente consola
drwxr-xr-x  app/                     # Código fuente principal
drwxr-xr-x  gradle/                  # Archivos Gradle
drwxr-xr-x  test-scripts/            # Scripts de testing
```

### Paso 3: Construir el Proyecto
```bash
# Construir todo el proyecto (compilar + empaquetar)
./gradlew build

# Verificar que compiló correctamente
echo $?
# Debe mostrar: 0 (éxito)

# Si hay errores, limpiar y reconstruir
./gradlew clean
./gradlew build
```

## 🚀 Primeros Pasos

### Ejecutar Demo Rápida
```bash
# Ejecutar la aplicación principal (demo interactiva)
./gradlew run
```
Esto ejecutará el menú principal del cliente consola para una experiencia interactiva completa.

### Verificar que Todo Funciona
```bash
# Ejecutar pruebas básicas de serialización
./gradlew test --tests "*MensajesTest*"

# Si pasan, el sistema de mensajería está funcionando
# Debe mostrar: BUILD SUCCESSFUL
```

### Verificar Compilación Completa
```bash
# Compilar sin ejecutar pruebas
./gradlew compileKotlin

# Si no hay errores, el código está bien estructurado
```

## 🖥️ Ejecutar el Servidor

### Paso 1: Abrir Terminal para el Servidor
```bash
# Terminal 1 - Navegar al directorio del proyecto
# Verificar que estamos en el lugar correcto
pwd
# Debe terminar en: programaci-n-2-2025-de-la-cruz-juego-Candelaetec
```

### Paso 2: Iniciar el Servidor
```bash
# Ejecutar el servidor (deshabilitar cache de configuración para evitar problemas)
./gradlew runServer --no-configuration-cache
```

### Paso 3: Verificar que el Servidor Está Ejecutándose
Debes ver exactamente esta salida en la terminal:
```
[INFO] Iniciando servidor de juego multijugador
[INFO] Servidor: Servidor escuchando en puerto 5050...
[INFO] Logger inicializado correctamente
```

### Paso 4: Mantener el Servidor Ejecutándose
**IMPORTANTE:** Mantén esta terminal abierta. El servidor debe estar ejecutándose para que los clientes puedan conectarse.

### Paso 5: Verificar Puerto del Servidor (Opcional)
```bash
# En otra terminal, verificar que el puerto 5050 está en uso
netstat -tlnp | grep 5050
# O en sistemas sin netstat:
ss -tlnp | grep 5050
```

## 💻 Cliente Consola

### Opción 1: Menú Interactivo Completo (Recomendado para principiantes)
```bash
# Terminal 2 - Cliente Consola Interactivo
./gradlew run
```

### Opción 2: Cliente Consola con Comandos Directos
```bash
# Crear nueva partida
./gradlew runClient --args="--cmd=create --name=Jugador1"

# Unirse automáticamente a partida existente
./gradlew runClient --args="--cmd=joinAuto --name=Jugador2"

# Unirse a partida específica (necesitas el ID exacto)
./gradlew runClient --args="--cmd=join --id=PARTIDA-ABC12345 --name=Jugador3"

# Realizar movimiento (solo cuando sea tu turno)
./gradlew runClient --args="--cmd=move --id=PARTIDA-ABC12345 --playerId=1 --fila=0 --columna=0 --contenido=X"
```

### Opción 3: Cliente Consola Demo (sin servidor)
```bash
# Ejecutar simulación local sin servidor
./gradlew run --args="--demo"
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
# Terminal 3 - Cliente Gráfico
./gradlew runGUI
```

### Paso 2: Verificar que la Ventana se Abre
Debes ver una ventana emergente con el título "Juego de Tablero Multijugador" y campos para:
- **Nombre del Jugador**: Campo de texto
- **Host**: Campo con "127.0.0.1" pre-llenado
- **Puerto**: Campo con "5050" pre-llenado

### Paso 3: Configurar Conexión
En la ventana que se abre:
1. **Nombre**: Ingresa tu nombre de jugador (2-20 caracteres, sin espacios especiales)
2. **Host**: `127.0.0.1` (localhost) - ya está configurado
3. **Puerto**: `5050` - ya está configurado
4. **Botones disponibles**:
   - 🆕 **Crear Nueva Partida**: Crea una partida y espera a otro jugador
   - 🔗 **Unirse a Partida**: Abre diálogo para ingresar ID de partida específico
   - 🎯 **Unirse Automáticamente**: Busca automáticamente una partida disponible

### Paso 4: Jugar en GUI
- **Tablero**: Celdas coloreadas alternando clara/oscura
- **Información del Juego**: Muestra estado actual y turno del jugador
- **Mensajes**: Área de texto con log de eventos del juego
- **Controles**: Botón "📋 Volver al Menú" para regresar a la pantalla inicial

### Paso 5: Realizar Movimientos
- **Espera tu turno**: El tablero se actualiza automáticamente
- **Haz clic en una celda**: Para colocar tu símbolo (X/O)
- **Observa el log**: Los mensajes muestran qué sucedió

## 🎯 Jugar con Clientes Mixtos

### Escenario 1: Un Jugador Consola + Un Jugador GUI

#### Terminal 1: Servidor (mantener ejecutándose)
```bash
./gradlew runServer --no-configuration-cache
# Debe mostrar: Servidor escuchando en puerto 5050...
```

#### Terminal 2: Cliente Consola (crea partida)
```bash
./gradlew run
# Seleccionar: 1. Crear nueva partida
# Ingresar nombre: Alice
# Esperar mensaje: "Partida creada exitosamente"
```

#### Terminal 3: Cliente GUI (se une automáticamente)
```bash
./gradlew runGUI
# Nombre: Bob
# Host: 127.0.0.1, Puerto: 5050 (ya configurado)
# Click: 🎯 Unirse Automáticamente
# Debe mostrar: "Unido a partida existente"
```

### Escenario 2: Dos Jugadores Consola

#### Terminal 1: Servidor (mantener ejecutándose)
```bash
./gradlew runServer --no-configuration-cache
```

#### Terminal 2: Jugador 1 (crea partida)
```bash
./gradlew run
# 1. Crear nueva partida
# Nombre: Player1
# Esperar confirmación
```

#### Terminal 3: Jugador 2 (se une)
```bash
./gradlew run
# 3. Unirse automáticamente
# Nombre: Player2
# Debe unirse a la partida de Player1
```

### Escenario 3: Múltiples Jugadores con Script Automatizado

```bash
# Hacer ejecutable el script de testing
chmod +x test-scripts/run-mixed-clients.sh

# Ejecutar escenario completo automatizado
./test-scripts/run-mixed-clients.sh
```

### Escenario 4: Unirse a Partida Específica

```bash
# Terminal 1: Servidor
./gradlew runServer --no-configuration-cache

# Terminal 2: Crear partida y copiar ID
./gradlew runClient --args="--cmd=create --name=Jugador1"
# Copiar el ID que aparece: PARTIDA-XXXXXXXX

# Terminal 3: Unirse con ID específico
./gradlew runClient --args="--cmd=join --id=PARTIDA-XXXXXXXX --name=Jugador2"
```

## 🧪 Testing y Verificación

### Ejecutar Todas las Pruebas
```bash
# Ejecutar suite completa de pruebas
./gradlew test

# Debe mostrar: BUILD SUCCESSFUL
# Número de pruebas ejecutadas y pasadas
```

### Pruebas Específicas por Componente
```bash
# Serialización de mensajes JSON
./gradlew test --tests "*MensajesTest*"

# Integración del servidor con clientes
./gradlew test --tests "*IntegracionTest*"

# Compatibilidad entre diferentes tipos de cliente
./gradlew test --tests "*ClienteHeterogeneoTest*"

# Manejo de desconexiones y reconexiones
./gradlew test --tests "*DesconexionTest*"

# Pruebas de aplicación completa
./gradlew test --tests "*AppTest*"
```

### Verificar Consistencia del Sistema
1. **Estados del Juego**: Ambos clientes deben mostrar el mismo tablero y estado
2. **Turnos**: Ambos clientes deben mostrar el mismo jugador actual
3. **Movimientos**: Las jugadas deben aparecer inmediatamente en ambos clientes
4. **Logs del Servidor**: Monitorea la terminal del servidor para eventos
5. **Mensajes de Error**: Deben ser claros y útiles para debugging

### Verificación Manual Paso a Paso
```bash
# 1. Verificar compilación
./gradlew compileKotlin

# 2. Verificar que no hay errores de linting
./gradlew build

# 3. Ejecutar pruebas unitarias
./gradlew test --tests "*MensajesTest*"

# 4. Probar servidor
./gradlew runServer --no-configuration-cache &
# (en background)

# 5. Probar cliente consola
./gradlew runClient --args="--cmd=create --name=TestPlayer"

# 6. Matar servidor de prueba
pkill -f runServer
```

## 🔧 Solución de Problemas

### Problema: "Command not found: ./gradlew"
```bash
# Verificar directorio actual
pwd
# Debe terminar en: programaci-n-2-2025-de-la-cruz-juego-Candelaetec

# Verificar que gradlew existe
ls -la gradlew

# Dar permisos de ejecución
chmod +x gradlew

# Verificar que funciona
./gradlew --version
```

### Problema: "Java version X is not supported"
```bash
# Verificar versión actual de Java
java -version

# Si es menor a 21, instalar Java 21+
# En Ubuntu/Debian:
sudo apt update
sudo apt install openjdk-21-jdk

# En macOS con Homebrew:
brew install openjdk@21

# En Windows: Descargar de https://adoptium.net/

# Verificar instalación
java -version
# Debe mostrar: Java 21.x.x o superior
```

### Problema: "Could not determine the dependencies of task"
```bash
# Deshabilitar cache de configuración
./gradlew build --no-configuration-cache

# O permanentemente en gradle.properties
echo "org.gradle.configuration-cache=false" >> gradle.properties
```

### Problema: "Address already in use" (Puerto 5050 ocupado)
```bash
# Verificar qué proceso usa el puerto
lsof -ti:5050
# o en sistemas sin lsof:
netstat -tlnp | grep 5050

# Matar el proceso
lsof -ti:5050 | xargs kill -9

# Verificar que el puerto está libre
ss -tlnp | grep 5050
# No debe mostrar nada
```

### Problema: "BUILD FAILED" - Errores de compilación
```bash
# Limpiar cache y reconstruir
./gradlew clean
./gradlew build --no-configuration-cache

# Verificar errores específicos
./gradlew build --stacktrace

# Si hay errores de Kotlin, verificar sintaxis
find app/src -name "*.kt" -exec kotlinc -classpath "$(./gradlew -q dependencies --configuration runtimeClasspath)" {} \;
```

### Problema: Cliente GUI no se abre
```bash
# Verificar que JavaFX está incluido
./gradlew dependencies | grep javafx

# Verificar configuración en build.gradle.kts
cat app/build.gradle.kts | grep -A 5 javafx

# En Linux, verificar display
echo $DISPLAY

# Si no hay display, usar X11 forwarding o VNC
```

### Problema: "Partida no encontrada"
- Verificar que el servidor esté ejecutándose en terminal separada
- Copiar exactamente el ID de partida (formato: PARTIDA-XXXXXXXX)
- Intentar unirse automáticamente en su lugar con `--cmd=joinAuto`
- Verificar logs del servidor para confirmar creación de partida

### Problema: "Connection refused" o "Connection timed out"
- Verificar que el servidor esté ejecutándose: `ps aux | grep runServer`
- Verificar puerto correcto: `ss -tlnp | grep 5050`
- Verificar firewall: `sudo ufw status` o `sudo iptables -L`
- Probar conexión local: `telnet 127.0.0.1 5050`
- Verificar que el host es correcto (127.0.0.1 para local)

### Problema: "Main class not found"
```bash
# Verificar mainClass en build.gradle.kts
cat app/build.gradle.kts | grep mainClass

# Debe mostrar: mainClass.set("org.example.MainLauncherKt")

# Limpiar y reconstruir
./gradlew clean build
```

### Problema: Tests fallan
```bash
# Ejecutar tests con más detalle
./gradlew test --info

# Ejecutar test específico
./gradlew test --tests "*MensajesTest*" --debug

# Verificar que las dependencias de test están correctas
./gradlew dependencies --configuration testRuntimeClasspath
```

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

- **[README_TESTING.md](README_TESTING.md)**: Guía completa de testing y verificación
- **[README_CLIENTE_CONSOLA.md](README_CLIENTE_CONSOLA.md)**: Documentación específica del cliente consola

## ⚙️ Configuración Avanzada

### Variables de Entorno
```bash
# Puerto personalizado del servidor
export SERVER_PORT=8080

# Host personalizado
export SERVER_HOST=192.168.1.100

# Nivel de logging (DEBUG, INFO, WARN, ERROR)
export LOG_LEVEL=DEBUG
```

### Configuración del Proyecto
- **gradle.properties**: Configuración de Gradle
- **settings.gradle.kts**: Configuración del proyecto multi-módulo
- **app/build.gradle.kts**: Dependencias y configuración de build

### Archivos de Configuración
- **JsonConfig.kt**: Configuración JSON del juego
- **Logger.kt**: Sistema de logging personalizado
- **styles.css**: Estilos para la interfaz GUI

## 🔍 Verificación Final

Antes de entregar el proyecto, ejecuta esta checklist:

```bash
# ✅ 1. Compilación exitosa
./gradlew clean build

# ✅ 2. Tests pasan
./gradlew test

# ✅ 3. Servidor inicia correctamente
./gradlew runServer --no-configuration-cache &
sleep 2
pkill -f runServer

# ✅ 4. Cliente consola funciona
./gradlew runClient --args="--cmd=create --name=Test"

# ✅ 5. Cliente GUI se abre (en entorno gráfico)
./gradlew runGUI &
sleep 3
pkill -f runGUI

# ✅ 6. Demo funciona
./gradlew run --args="--demo"

# ✅ 7. Documentación completa
ls README*.md
```

## 🎯 Requisitos del Curso Cumplidos

Este proyecto cumple con todos los requisitos obligatorios de Programación 2:

- ✅ **Función main ≤ 10 líneas** en `MainLauncher.kt`
- ✅ **Estructura POO pura** (sin funciones fuera de clases)
- ✅ **Herencia implementada** (`ClienteBase`, etc.)
- ✅ **Encapsulamiento aplicado** (modificadores de acceso)
- ✅ **Polimorfismo funcionando** (Comandos/Eventos)
- ✅ **Principios SOLID respetados**
- ✅ **Documentación completa y verificable**

## 🎉 ¡Proyecto Listo para Entrega!

Tu sistema de juego multijugador está completamente funcional y documentado:

- ✅ **Servidor robusto** con manejo de conexiones
- ✅ **Cliente consola** con menú interactivo completo
- ✅ **Cliente GUI** moderno con JavaFX
- ✅ **Compatibilidad mixta** entre tipos de cliente
- ✅ **Sistema de testing** completo y automatizado
- ✅ **Documentación exhaustiva** a prueba de tontos
- ✅ **Código refactorizado** según estándares del curso

**¡El proyecto está listo para ser evaluado!** 🎮✨
