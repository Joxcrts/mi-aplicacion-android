package com.joxcrts.mistareas.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joxcrts.mistareas.R
import com.joxcrts.mistareas.data.Prioridad
import com.joxcrts.mistareas.data.Tarea
import com.joxcrts.mistareas.ui.theme.PrioridadAlta
import com.joxcrts.mistareas.ui.theme.PrioridadBaja
import com.joxcrts.mistareas.ui.theme.PrioridadMedia
import java.text.DateFormat
import java.util.Date

/** Pantalla principal: cabecera con progreso, buscador, filtros y lista de tareas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(viewModel: TareasViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tareaEliminada by viewModel.tareaEliminada.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaEnEdicion by remember { mutableStateOf<Tarea?>(null) }

    val mensajeEliminada = stringResource(R.string.tarea_eliminada)
    val accionDeshacer = stringResource(R.string.deshacer)

    LaunchedEffect(tareaEliminada) {
        val tarea = tareaEliminada ?: return@LaunchedEffect
        val resultado = snackbarHostState.showSnackbar(
            message = mensajeEliminada.format(tarea.titulo),
            actionLabel = accionDeshacer,
            withDismissAction = true
        )
        if (resultado == SnackbarResult.ActionPerformed) {
            viewModel.deshacerEliminacion()
        } else {
            viewModel.confirmarEliminacion()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (uiState.completadas > 0) {
                        IconButton(onClick = viewModel::eliminarCompletadas) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.eliminar_completadas)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    tareaEnEdicion = null
                    mostrarDialogo = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.nueva_tarea)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            CabeceraProgreso(uiState)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = viewModel::cambiarBusqueda,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.buscar_tareas)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            FilaFiltros(
                filtroActual = uiState.filtro,
                onFiltroSeleccionado = viewModel::cambiarFiltro
            )

            Spacer(Modifier.height(8.dp))

            when {
                uiState.cargando -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.tareas.isEmpty() -> EstadoVacio()
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.tareas, key = { it.id }) { tarea ->
                            TarjetaTarea(
                                tarea = tarea,
                                onAlternar = { viewModel.alternarCompletada(tarea) },
                                onEditar = {
                                    tareaEnEdicion = tarea
                                    mostrarDialogo = true
                                },
                                onEliminar = { viewModel.eliminarTarea(tarea) }
                            )
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        EditarTareaDialog(
            tarea = tareaEnEdicion,
            onGuardar = { id, titulo, descripcion, prioridad, fechaLimite ->
                viewModel.guardarTarea(id, titulo, descripcion, prioridad, fechaLimite)
                mostrarDialogo = false
            },
            onCerrar = { mostrarDialogo = false }
        )
    }
}

@Composable
private fun CabeceraProgreso(uiState: TareasUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.progreso_titulo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { uiState.progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.progreso_resumen,
                    uiState.completadas,
                    uiState.totales
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FilaFiltros(
    filtroActual: FiltroTareas,
    onFiltroSeleccionado: (FiltroTareas) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FiltroTareas.entries.forEach { filtro ->
            val etiqueta = when (filtro) {
                FiltroTareas.TODAS -> stringResource(R.string.filtro_todas)
                FiltroTareas.PENDIENTES -> stringResource(R.string.filtro_pendientes)
                FiltroTareas.COMPLETADAS -> stringResource(R.string.filtro_completadas)
            }
            FilterChip(
                selected = filtroActual == filtro,
                onClick = { onFiltroSeleccionado(filtro) },
                label = { Text(etiqueta) }
            )
        }
    }
}

@Composable
private fun EstadoVacio() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.sin_tareas),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = stringResource(R.string.sin_tareas_ayuda),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun TarjetaTarea(
    tarea: Tarea,
    onAlternar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAlternar) {
                Icon(
                    imageVector = if (tarea.completada) {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = stringResource(
                        if (tarea.completada) R.string.marcar_pendiente else R.string.marcar_completada
                    ),
                    tint = if (tarea.completada) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        colorDePrioridad(tarea.prioridad)
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IndicadorPrioridad(tarea.prioridad)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (tarea.descripcion.isNotBlank()) {
                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                tarea.fechaLimite?.let { fecha ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = DateFormat.getDateInstance(DateFormat.MEDIUM)
                                .format(Date(fecha)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.editar_tarea),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.eliminar_tarea),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun IndicadorPrioridad(prioridad: Prioridad) {
    Surface(
        color = colorDePrioridad(prioridad),
        shape = CircleShape,
        modifier = Modifier.size(8.dp)
    ) {}
}

internal fun colorDePrioridad(prioridad: Prioridad) = when (prioridad) {
    Prioridad.ALTA -> PrioridadAlta
    Prioridad.MEDIA -> PrioridadMedia
    Prioridad.BAJA -> PrioridadBaja
}
