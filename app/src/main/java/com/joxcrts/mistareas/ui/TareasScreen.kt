package com.joxcrts.mistareas.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
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
import java.time.LocalDate

/** Pantalla principal: cabecera con progreso, buscador, filtros y lista de tareas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(viewModel: TareasViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tareaEliminada by viewModel.tareaEliminada.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaEnEdicion by remember { mutableStateOf<Tarea?>(null) }
    var confirmarLimpieza by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { confirmarLimpieza = true }) {
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
                trailingIcon = if (uiState.busqueda.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.cambiarBusqueda("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.limpiar_busqueda)
                            )
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            FilaFiltros(
                uiState = uiState,
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
                uiState.tareas.isEmpty() -> EstadoVacio(hayTareasOcultas = uiState.totales > 0)
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.tareas, key = { it.id }) { tarea ->
                            TarjetaTareaDeslizable(
                                tarea = tarea,
                                onAlternar = { viewModel.alternarCompletada(tarea) },
                                onEditar = {
                                    tareaEnEdicion = tarea
                                    mostrarDialogo = true
                                },
                                onEliminar = { viewModel.eliminarTarea(tarea) },
                                modifier = Modifier.animateItem()
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

    if (confirmarLimpieza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpieza = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
            title = { Text(stringResource(R.string.eliminar_completadas)) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.confirmar_eliminar_completadas,
                        uiState.completadas,
                        uiState.completadas
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarCompletadas()
                        confirmarLimpieza = false
                    }
                ) {
                    Text(stringResource(R.string.eliminar))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpieza = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
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
                text = if (uiState.todoCompletado) {
                    stringResource(R.string.todo_completado)
                } else {
                    stringResource(
                        R.string.progreso_resumen,
                        uiState.completadas,
                        uiState.totales
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FilaFiltros(
    uiState: TareasUiState,
    onFiltroSeleccionado: (FiltroTareas) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FiltroTareas.entries.forEach { filtro ->
            val etiqueta = when (filtro) {
                FiltroTareas.TODAS -> stringResource(R.string.filtro_todas)
                FiltroTareas.PENDIENTES -> stringResource(R.string.filtro_pendientes)
                FiltroTareas.COMPLETADAS -> stringResource(R.string.filtro_completadas)
            }
            val conteo = when (filtro) {
                FiltroTareas.TODAS -> uiState.totales
                FiltroTareas.PENDIENTES -> uiState.pendientes
                FiltroTareas.COMPLETADAS -> uiState.completadas
            }
            FilterChip(
                selected = uiState.filtro == filtro,
                onClick = { onFiltroSeleccionado(filtro) },
                label = {
                    Text(stringResource(R.string.filtro_con_conteo, etiqueta, conteo))
                }
            )
        }
    }
}

@Composable
private fun EstadoVacio(hayTareasOcultas: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hayTareasOcultas) Icons.Outlined.SearchOff else Icons.Outlined.Inbox,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                if (hayTareasOcultas) R.string.sin_resultados else R.string.sin_tareas
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = stringResource(
                if (hayTareasOcultas) R.string.sin_resultados_ayuda else R.string.sin_tareas_ayuda
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * Tarjeta con gestos de deslizamiento: hacia la derecha alterna
 * completada/pendiente y hacia la izquierda elimina (con opción de deshacer).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaTareaDeslizable(
    tarea: Tarea,
    onAlternar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alternarActual by rememberUpdatedState(onAlternar)
    val eliminarActual by rememberUpdatedState(onEliminar)
    val estadoDeslizamiento = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            when (valor) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    alternarActual()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    eliminarActual()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = estadoDeslizamiento,
        modifier = modifier,
        backgroundContent = {
            FondoDeslizamiento(
                estado = estadoDeslizamiento,
                completada = tarea.completada
            )
        }
    ) {
        TarjetaTarea(
            tarea = tarea,
            onAlternar = onAlternar,
            onEditar = onEditar,
            onEliminar = onEliminar
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FondoDeslizamiento(
    estado: SwipeToDismissBoxState,
    completada: Boolean
) {
    val direccion = estado.dismissDirection
    val color = when (direccion) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = when (direccion) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        }
    ) {
        when (direccion) {
            SwipeToDismissBoxValue.StartToEnd -> Icon(
                imageVector = if (completada) Icons.AutoMirrored.Filled.Undo else Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            SwipeToDismissBoxValue.EndToStart -> Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            SwipeToDismissBoxValue.Settled -> Unit
        }
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
                    EtiquetaFechaLimite(fechaMillis = fecha, completada = tarea.completada)
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

/**
 * Fecha límite de la tarjeta: muestra "Hoy" o "Mañana" cuando aplica y
 * resalta en color de error las tareas pendientes cuya fecha ya pasó.
 */
@Composable
private fun EtiquetaFechaLimite(fechaMillis: Long, completada: Boolean) {
    val fechaLocal = fechaLimiteComoLocalDate(fechaMillis)
    val hoy = LocalDate.now()
    val vencida = !completada && fechaLocal.isBefore(hoy)

    val texto = when (fechaLocal) {
        hoy -> stringResource(R.string.fecha_hoy)
        hoy.plusDays(1) -> stringResource(R.string.fecha_manana)
        else -> formatearFechaLimite(fechaMillis)
    }
    val color = if (vencida) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Event,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (vencida) stringResource(R.string.fecha_vencida, texto) else texto,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
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
