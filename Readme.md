# Mis Tareas 📋

Aplicación Android completa de gestión de tareas, construida con las tecnologías modernas recomendadas por Google.

## ✨ Funcionalidades

- **Crear, editar y eliminar tareas** con título, descripción, prioridad y fecha límite opcional.
- **Marcar tareas como completadas** con un toque.
- **Prioridades con colores** (Alta 🔴, Media 🟠, Baja 🟢) y ordenamiento automático: las pendientes y urgentes aparecen primero.
- **Búsqueda en tiempo real** por título o descripción.
- **Filtros**: Todas / Pendientes / Completadas.
- **Barra de progreso diario** que muestra cuántas tareas llevas completadas.
- **Deshacer eliminación** desde un Snackbar, para no perder tareas por accidente.
- **Limpieza rápida** de todas las tareas completadas desde la barra superior.
- **Tema claro y oscuro** automático, con colores dinámicos (Material You) en Android 12+.
- **Persistencia local**: las tareas se guardan en el dispositivo con Room y sobreviven al cierre de la app.

## 🏗️ Arquitectura y tecnologías

| Capa | Tecnología |
|------|------------|
| Interfaz | Jetpack Compose + Material 3 |
| Presentación | ViewModel + StateFlow (patrón MVVM, estado unidireccional) |
| Datos | Repositorio + Room (SQLite) |
| Lenguaje | Kotlin 2.0 con corrutinas y Flow |
| Dependencias | Catálogo de versiones (`gradle/libs.versions.toml`) |

Estructura del código:

```
app/src/main/java/com/joxcrts/mistareas/
├── MainActivity.kt              # Punto de entrada, tema y pantalla principal
├── MisTareasApplication.kt      # Contenedor de dependencias
├── data/
│   ├── Tarea.kt                 # Entidad y prioridades
│   ├── TareaDao.kt              # Consultas a la base de datos
│   ├── AppDatabase.kt           # Base de datos Room (singleton)
│   └── TareaRepository.kt       # Abstracción del origen de datos
└── ui/
    ├── TareasViewModel.kt       # Lógica de presentación y estado
    ├── TareasScreen.kt          # Pantalla principal en Compose
    ├── EditarTareaDialog.kt     # Diálogo de creación/edición
    └── theme/                   # Colores, tipografía y tema Material 3
```

## 🚀 Cómo compilar y ejecutar

1. Abre el proyecto en **Android Studio** (Ladybug o más reciente).
2. Espera a que Gradle sincronice las dependencias.
3. Conecta un dispositivo o inicia un emulador (Android 8.0 / API 26 o superior).
4. Pulsa **Run ▶**.

Desde la terminal:

```bash
./gradlew assembleDebug      # Genera el APK de depuración
./gradlew testDebugUnitTest  # Ejecuta las pruebas unitarias
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## ✅ Pruebas

El proyecto incluye pruebas unitarias del ViewModel (`app/src/test/`) que cubren:
creación y validación de tareas, filtros, búsqueda, deshacer eliminación y cálculo de progreso.

## 📱 Requisitos

- Android 8.0 (API 26) o superior.
- La versión release usa R8 con minificación y reducción de recursos activadas.
