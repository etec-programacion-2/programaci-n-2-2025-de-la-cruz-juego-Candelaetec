# Guía de Testing: Clientes Mixtos y Compatibilidad

Esta guía documenta cómo probar la compatibilidad entre clientes consola y GUI, incluyendo escenarios de testing, scripts automatizados y verificación de consistencia.

## 📋 Tabla de Contenidos

- [Prerrequisitos](#prerrequisitos)
- [Ejecución de Pruebas](#ejecución-de-pruebas)
- [Escenarios de Prueba](#escenarios-de-prueba)
- [Scripts de Automatización](#scripts-de-automatización)
- [Verificación de Consistencia](#verificación-de-consistencia)
- [Debugging y Logs](#debugging-y-logs)
- [Casos Edge](#casos-edge)

## 🔧 Prerrequisitos

- **Java 21+** instalado
- **Gradle** (viene incluido)
- **Terminal** con soporte para procesos en background
- **Puertos 5050-5060** disponibles (para pruebas múltiples)

## 🧪 Ejecución de Pruebas

### Pruebas Unitarias

```bash
# Ejecutar todas las pruebas unitarias
./gradlew test

# Solo pruebas de mensajes
./gradlew test --tests "*MensajesTest*"

# Solo pruebas de integración
./gradlew test --tests "*IntegracionTest*"
```

### Pruebas de Integración Manuales

```bash
# Terminal 1: Servidor
./gradlew runServer

# Terminal 2: Cliente GUI
./gradlew runGUI

# Terminal 3: Cliente Consola
./gradlew run
```

### Pruebas Automatizadas

```bash
# Hacer ejecutable el script
chmod +x test-scripts/run-mixed-clients.sh

# Ejecutar pruebas interactivas
./test-scripts/run-mixed-clients.sh
```

## 🎭 Escenarios de Prueba

### 1. Un Jugador Consola + Un Jugador GUI

**Objetivo:** Verificar que ambos tipos de cliente pueden jugar juntos.

**Pasos:**
1. Iniciar servidor
2. Cliente GUI crea partida
3. Cliente consola se une automáticamente
4. Ambos pueden realizar movimientos
5. Verificar que ambos ven los mismos estados

**Verificación:**
- ✅ Ambos clientes muestran el mismo tablero
- ✅ Turnos alternan correctamente
- ✅ Movimientos se reflejan en ambos clientes
- ✅ Logs muestran comunicación bidireccional

### 2. Dos Jugadores Consola

**Objetivo:** Verificar funcionamiento básico con clientes homogéneos.

**Pasos:**
1. Cliente 1 crea partida
2. Cliente 2 se une
3. Jugar partida completa
4. Verificar estados finales

### 3. Múltiples Clientes Simultáneos

**Objetivo:** Probar concurrencia y manejo de múltiples conexiones.

**Pasos:**
1. Iniciar servidor
2. Ejecutar 5+ clientes simultáneamente
3. Verificar que todos se procesan correctamente
4. Monitorear logs de rendimiento

### 4. Desconexión y Reconexión

**Objetivo:** Verificar manejo de desconexiones parciales.

**Pasos:**
1. Dos clientes conectados jugando
2. Desconectar un cliente (cerrar terminal)
3. Verificar que el otro cliente continúa
4. Intentar reconectar (debería fallar para partida en curso)

### 5. Stress Test

**Objetivo:** Verificar estabilidad bajo carga.

**Pasos:**
1. Iniciar servidor
2. Ejecutar 10+ operaciones simultáneas
3. Verificar que no hay pérdidas de mensajes
4. Monitorear uso de memoria y CPU

## 📜 Scripts de Automatización

### `run-mixed-clients.sh`

Script interactivo que permite ejecutar diferentes escenarios:

```bash
./test-scripts/run-mixed-clients.sh
```

**Opciones del menú:**
- **1:** Consola + GUI
- **2:** Dos consolas
- **3:** GUI crea, consola se une
- **4:** Múltiples consolas
- **5:** Stress test
- **7-8:** Ejecutar pruebas unitarias/integración

### Ejecución Programática

```bash
# Crear partida con cliente específico
./gradlew runClient --args="--cmd=create --name=TestPlayer"

# Unirse automáticamente
./gradlew runClient --args="--cmd=joinAuto --name=Joiner"

# Realizar movimiento
./gradlew runClient --args="--cmd=move --id=PARTIDA-ABC12345 --playerId=1 --fila=0 --columna=0 --contenido=X"
```

## 🔍 Verificación de Consistencia

### Estados del Juego

Verificar que todos los clientes ven los mismos estados:

```kotlin
// En logs, buscar líneas como:
[INFO] Juego [PARTIDA-ABC123]: Estado cambió de ESPERANDO_JUGADORES a EN_CURSO
[INFO] Juego [PARTIDA-ABC123]: Jugador Alice (ID:1) realizó: movimiento en (0,0) = 'X'
```

### Sincronización de Tableros

1. **Después de cada movimiento:** Todos los clientes deberían mostrar el mismo tablero
2. **Cambios de turno:** Todos los clientes deberían mostrar el mismo jugador actual
3. **Estados finales:** Victoria/derrota debería ser consistente

### Protocolo de Comunicación

Verificar formato JSON consistente:

```json
// Comando enviado
{"tipo":"org.example.Comando.RealizarMovimiento","idPartida":"PARTIDA-ABC123","jugadorId":1,"fila":0,"columna":0,"contenido":"X"}

// Evento recibido
{"tipo":"org.example.Evento.PartidaActualizada","juego":{...}}
```

## 🐛 Debugging y Logs

### Niveles de Logging

```kotlin
// En código, cambiar nivel:
Logger.setLevel(Logger.Level.DEBUG)  // Más detallado
Logger.setLevel(Logger.Level.INFO)   // Producción
```

### Logs Importantes

```
[INFO] Servidor: Nueva conexión aceptada -> 127.0.0.1
[DEBUG] RECIBIDO mensaje #1: {"tipo":"CrearPartida",...}
[INFO] Juego [PARTIDA-ABC123]: Jugador Alice creó nueva partida
[DEBUG] ENVIADO respuesta #1: {"tipo":"PartidaActualizada",...}
[INFO] Servidor: Cliente desconectado -> 127.0.0.1 (mensajes procesados: 3)
```

### Debugging de Problemas

1. **Cliente no se conecta:**
   - Verificar que servidor esté ejecutándose en puerto 5050
   - Revisar logs del servidor

2. **Movimientos no se reflejan:**
   - Verificar ID de partida correcto
   - Revisar logs de "RealizarMovimiento"

3. **Estados inconsistentes:**
   - Comparar logs entre clientes
   - Verificar timestamps de mensajes

## ⚠️ Casos Edge

### Desconexiones Parciales

```bash
# Simular desconexión: matar proceso de cliente
kill <CLIENT_PID>

# Verificar que el otro cliente continúa
# Intentar reconectar debería fallar para partida en curso
```

### Mensajes Corruptos

- Enviar JSON malformado
- Verificar que servidor maneja errores gracefully
- Logs deberían mostrar "Error procesando mensaje"

### Concurrencia Extrema

```bash
# Ejecutar múltiples instancias simultáneas
for i in {1..10}; do
    ./gradlew runClient --args="--cmd=create --name=Bot$i" &
done
```

### Memoria y Rendimiento

- Monitorear uso de memoria durante stress tests
- Verificar que no hay memory leaks
- Medir tiempos de respuesta

## 📊 Métricas de Éxito

### Funcionales
- ✅ Todos los escenarios pasan sin errores
- ✅ Estados consistentes entre clientes
- ✅ Comunicación bidireccional funciona
- ✅ Manejo correcto de errores

### No Funcionales
- ✅ Tiempo de respuesta < 100ms para operaciones normales
- ✅ Memoria estable durante ejecución prolongada
- ✅ Logs útiles para debugging
- ✅ Código cubierto por pruebas unitarias

## 🔧 Troubleshooting

### Problema: "Address already in use"
```bash
# Matar procesos en puerto 5050
lsof -ti:5050 | xargs kill -9
```

### Problema: Tests fallan
```bash
# Limpiar y reconstruir
./gradlew clean build test
```

### Problema: GUI no se lanza
```bash
# Verificar JavaFX
./gradlew dependencies | grep javafx
```

## 📝 Notas para Desarrolladores

- **Thread Safety:** Todas las operaciones de red están en hilos separados
- **Platform.runLater:** UI updates se ejecutan en JavaFX Application Thread
- **Logging:** Usar Logger en lugar de println para consistencia
- **Testing:** Agregar nuevos tests en `MensajesTest.kt` o `IntegracionTest.kt`

---

**Estado del Testing:** ✅ Completo con cobertura para escenarios mixtos, concurrencia y casos edge.