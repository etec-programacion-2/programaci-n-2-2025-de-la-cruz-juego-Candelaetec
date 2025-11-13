# Cliente de Consola Mejorado - Juego de Tablero Multijugador

## Descripción

Este proyecto implementa un cliente de consola mejorado para el juego de tablero multijugador, con una interfaz de usuario amigable que incluye menús interactivos, validación de entrada, visualización clara del tablero y manejo robusto de errores.

## Características Principales

### 🎮 Interfaz de Usuario Mejorada
- **Menús interactivos numerados**: Opciones claras del 1 al 6
- **Retroalimentación visual**: Uso de emojis y símbolos para mejor UX
- **Navegación intuitiva**: Flujo lógico desde conexión hasta desconexión
- **Ayuda integrada**: Reglas del juego y comandos disponibles

### ✅ Validación Robusta de Entrada
- **Nombres de usuario**: 2-20 caracteres, solo letras, números y espacios
- **IDs de partida**: Formato estricto `PARTIDA-XXXXXXXX`
- **Coordenadas**: Formato `a1`, `b2`, etc. con validación de rango
- **Contenido de movimientos**: Símbolos válidos según tipo de juego

### 📊 Visualización Clara del Tablero
- **Coordenadas ASCII**: Columnas (A-H) y filas (1-8)
- **Símbolos diferenciados**: X/O para tres en línea, símbolos de ajedrez
- **Estado en tiempo real**: Actualización visual después de cada movimiento
- **Posiciones disponibles**: Muestra coordenadas libres

### 🛡️ Manejo Comprehensivo de Errores
- **Mensajes descriptivos**: Errores específicos sin crashear la app
- **Oportunidades de reintento**: Permite corregir errores sin perder progreso
- **Validación en tiempo real**: Feedback inmediato sobre entradas inválidas
- **Recuperación de conexión**: Manejo de desconexiones del servidor

## Estructura del Código

### Separación de Responsabilidades
- **`ClienteConsola`**: Lógica de interfaz de usuario y validación
- **`ClienteMain`**: Punto de entrada con compatibilidad hacia atrás
- **Protocolo existente**: Reutiliza `Comando` y `Evento` sin cambios

### Arquitectura
```
ClienteConsola
├── Gestión de conexión (conectar/desconectar)
├── Menús interactivos (principal, ayuda, reglas)
├── Validación de entrada (nombres, coordenadas, IDs)
├── Visualización del tablero (ASCII con coordenadas)
├── Flujo del juego (turnos, movimientos, estado)
└── Manejo de errores (mensajes descriptivos)
```

## Uso

### Cliente Interactivo (Recomendado)
```bash
# Ejecutar sin argumentos para usar el cliente interactivo
java -jar app.jar
```

### Cliente Original (Compatibilidad)
```bash
# Crear partida
java -jar app.jar --cmd=create --name=Jugador1

# Unirse a partida
java -jar app.jar --cmd=join --id=PARTIDA-ABC12345 --name=Jugador2

# Realizar movimiento
java -jar app.jar --cmd=move --id=PARTIDA-ABC12345 --playerId=1001 --fila=0 --columna=0 --contenido=X
```

## Flujo de Usuario

### 1. Menú Principal
```
📋 MENÚ PRINCIPAL
──────────────────────────────────────────────────
1. 🆕 Crear nueva partida
2. 🔗 Unirse a partida existente
3. 🎯 Unirse automáticamente a cualquier partida
4. ❓ Ver reglas del juego
5. ℹ️  Ayuda y comandos
6. 🚪 Salir
──────────────────────────────────────────────────
```

### 2. Creación/Unión a Partida
- Solicitud de nombre de usuario con validación
- Para unirse: solicitud de ID de partida con validación
- Conexión automática al servidor
- Confirmación de éxito con detalles de la partida

### 3. Durante el Juego
- Visualización del estado actual (jugadores, turno, ronda)
- Tablero con coordenadas claras
- Solicitud de movimientos con formato específico
- Validación en tiempo real de movimientos
- Alternancia automática de turnos

### 4. Fin del Juego
- Detección automática de victoria/empate
- Visualización del estado final
- Opción de volver al menú principal
- Desconexión limpia del servidor

## Validaciones Implementadas

### Nombres de Usuario
- ✅ Longitud: 2-20 caracteres
- ✅ Caracteres: Solo letras, números y espacios
- ❌ Vacío, muy corto, muy largo, caracteres especiales

### IDs de Partida
- ✅ Formato: `PARTIDA-XXXXXXXX` (8 caracteres alfanuméricos)
- ❌ Formato incorrecto, longitud incorrecta

### Coordenadas
- ✅ Formato: `a1`, `b2`, `c3`, etc.
- ✅ Rango: A-H para columnas, 1-8 para filas
- ❌ Formato incorrecto, fuera de rango

### Movimientos
- ✅ Contenido válido según tipo de juego
- ✅ Posición disponible en el tablero
- ❌ Contenido inválido, posición ocupada

## Manejo de Errores

### Errores de Conexión
```
❌ ERROR: No se pudo conectar al servidor: Connection refused
──────────────────────────────────────────────────────────────────
→ Verificar que el servidor esté ejecutándose
→ Verificar host y puerto correctos
```

### Errores de Validación
```
❌ ERROR: Coordenada inválida: z9
──────────────────────────────────────────────────────────────────
→ Use formato: letra + número (ej: a1, b2, c3)
→ Verificar que esté dentro del tablero
```

### Errores de Movimiento
```
❌ ERROR: La posición a1 ya está ocupada
──────────────────────────────────────────────────────────────────
→ Seleccionar una posición disponible
→ Verificar el estado actual del tablero
```

## Beneficios de la Implementación

### Para el Usuario
- **Experiencia intuitiva**: Menús claros y navegación fácil
- **Feedback inmediato**: Validación en tiempo real
- **Recuperación de errores**: Posibilidad de corregir sin perder progreso
- **Información clara**: Estado del juego siempre visible

### Para el Desarrollador
- **Código mantenible**: Separación clara de responsabilidades
- **Fácil extensión**: Estructura modular para nuevas funcionalidades
- **Compatibilidad**: Mantiene funcionalidad original
- **Testeable**: Lógica separada de UI para testing

### Para el Sistema
- **Robustez**: Manejo de errores sin crashear
- **Eficiencia**: Conexiones optimizadas
- **Escalabilidad**: Arquitectura preparada para mejoras futuras

## Criterios de Aceptación Cumplidos

✅ **Menú principal numerado**: Opciones 1-6 claramente definidas
✅ **Visualización del tablero**: Coordenadas ASCII (a-h, 1-8) con formato legible
✅ **Validación de entrada**: Rechaza formatos inválidos con mensajes específicos
✅ **Manejo de desconexiones**: Mensajes informativos y opciones de reconexión
✅ **Ayuda integrada**: Reglas del juego y comandos disponibles
✅ **Separación de responsabilidades**: UI separada de lógica de comunicación

## Próximos Pasos Sugeridos

1. **Persistencia**: Guardar historial de partidas
2. **Configuración**: Archivo de configuración para personalización
3. **Logging**: Sistema de logs para debugging
4. **Tests automatizados**: Suite de tests para validaciones
5. **Internacionalización**: Soporte para múltiples idiomas
