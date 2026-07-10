package com.joxcrts.mistareas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joxcrts.mistareas.MisTareasApplication
import com.joxcrts.mistareas.data.Prioridad
import com.joxcrts.mistareas.data.Tarea
import com.joxcrts.mistareas.data.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Filtros disponibles para la lista de tareas. */
enum class FiltroTareas {
    TODAS,
    PENDIENTES,
    COMPLETADAS
}

/** Estado inmutable que consume la pantalla principal. */
data class TareasUiState(
    val tareas: List<Tarea> = emptyList(),
    val filtro: FiltroTareas = FiltroTareas.TODAS,
    val busqueda: String = "",
    val totales: Int = 0,
    val completadas: Int = 0,
    val cargando: Boolean = true
) {
    /** Progreso entre 0f y 1f para la barra de avance. */
    val progreso: Float
        get() = if (totales == 0) 0f else completadas.toFloat() / totales
}

class TareasViewModel(private val repositorio: TareaRepository) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroTareas.TODAS)
    private val busqueda = MutableStateFlow("")

    /** Última tarea eliminada, retenida para poder deshacer desde el Snackbar. */
    private val _tareaEliminada = MutableStateFlow<Tarea?>(null)
    val tareaEliminada: StateFlow<Tarea?> = _tareaEliminada.asStateFlow()

    val uiState: StateFlow<TareasUiState> =
        combine(repositorio.tareas, filtro, busqueda) { tareas, filtroActual, textoBusqueda ->
            val visibles = tareas
                .filter { tarea ->
                    when (filtroActual) {
                        FiltroTareas.TODAS -> true
                        FiltroTareas.PENDIENTES -> !tarea.completada
                        FiltroTareas.COMPLETADAS -> tarea.completada
                    }
                }
                .filter { tarea ->
                    textoBusqueda.isBlank() ||
                        tarea.titulo.contains(textoBusqueda, ignoreCase = true) ||
                        tarea.descripcion.contains(textoBusqueda, ignoreCase = true)
                }
            TareasUiState(
                tareas = visibles,
                filtro = filtroActual,
                busqueda = textoBusqueda,
                totales = tareas.size,
                completadas = tareas.count { it.completada },
                cargando = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TareasUiState()
        )

    fun cambiarFiltro(nuevo: FiltroTareas) {
        filtro.value = nuevo
    }

    fun cambiarBusqueda(texto: String) {
        busqueda.value = texto
    }

    fun guardarTarea(
        id: Long,
        titulo: String,
        descripcion: String,
        prioridad: Prioridad,
        fechaLimite: Long?
    ) {
        val tituloLimpio = titulo.trim()
        if (tituloLimpio.isEmpty()) return
        viewModelScope.launch {
            if (id == 0L) {
                repositorio.guardar(
                    Tarea(
                        titulo = tituloLimpio,
                        descripcion = descripcion.trim(),
                        prioridad = prioridad,
                        fechaLimite = fechaLimite
                    )
                )
            } else {
                val existente = uiState.value.tareas.find { it.id == id }
                repositorio.guardar(
                    Tarea(
                        id = id,
                        titulo = tituloLimpio,
                        descripcion = descripcion.trim(),
                        prioridad = prioridad,
                        fechaLimite = fechaLimite,
                        completada = existente?.completada ?: false,
                        creadaEn = existente?.creadaEn ?: System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun alternarCompletada(tarea: Tarea) {
        viewModelScope.launch { repositorio.alternarCompletada(tarea) }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repositorio.eliminar(tarea)
            _tareaEliminada.value = tarea
        }
    }

    /** Restaura la última tarea eliminada (acción "Deshacer" del Snackbar). */
    fun deshacerEliminacion() {
        val tarea = _tareaEliminada.value ?: return
        viewModelScope.launch {
            repositorio.guardar(tarea)
            _tareaEliminada.value = null
        }
    }

    fun confirmarEliminacion() {
        _tareaEliminada.value = null
    }

    fun eliminarCompletadas() {
        viewModelScope.launch { repositorio.eliminarCompletadas() }
    }

    companion object {
        /** Fábrica que obtiene el repositorio desde el contenedor de la Application. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MisTareasApplication
                TareasViewModel(app.repositorio)
            }
        }
    }
}
