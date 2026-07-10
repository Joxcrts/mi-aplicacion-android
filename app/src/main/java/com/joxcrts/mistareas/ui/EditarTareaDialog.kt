package com.joxcrts.mistareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joxcrts.mistareas.R
import com.joxcrts.mistareas.data.Prioridad
import com.joxcrts.mistareas.data.Tarea
import java.text.DateFormat
import java.util.Date

/**
 * Diálogo para crear una tarea nueva (si [tarea] es null) o editar una existente.
 * Valida que el título no esté vacío antes de permitir guardar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTareaDialog(
    tarea: Tarea?,
    onGuardar: (id: Long, titulo: String, descripcion: String, prioridad: Prioridad, fechaLimite: Long?) -> Unit,
    onCerrar: () -> Unit
) {
    var titulo by rememberSaveable(tarea) { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by rememberSaveable(tarea) { mutableStateOf(tarea?.descripcion ?: "") }
    var prioridad by rememberSaveable(tarea) { mutableStateOf(tarea?.prioridad ?: Prioridad.MEDIA) }
    var fechaLimite by rememberSaveable(tarea) { mutableStateOf(tarea?.fechaLimite) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var intentoGuardar by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                stringResource(
                    if (tarea == null) R.string.nueva_tarea else R.string.editar_tarea
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.campo_titulo)) },
                    isError = intentoGuardar && titulo.isBlank(),
                    supportingText = {
                        if (intentoGuardar && titulo.isBlank()) {
                            Text(stringResource(R.string.error_titulo_vacio))
                        }
                    },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.campo_descripcion)) },
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.campo_prioridad),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridad.entries.forEach { opcion ->
                        FilterChip(
                            selected = prioridad == opcion,
                            onClick = { prioridad = opcion },
                            label = { Text(etiquetaDePrioridad(opcion)) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { mostrarSelectorFecha = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null
                        )
                        Text(
                            text = fechaLimite?.let {
                                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))
                            } ?: stringResource(R.string.campo_fecha_limite),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (fechaLimite != null) {
                        IconButton(onClick = { fechaLimite = null }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.quitar_fecha)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    intentoGuardar = true
                    if (titulo.isNotBlank()) {
                        onGuardar(tarea?.id ?: 0L, titulo, descripcion, prioridad, fechaLimite)
                    }
                }
            ) {
                Text(stringResource(R.string.guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )

    if (mostrarSelectorFecha) {
        val estadoFecha = rememberDatePickerState(initialSelectedDateMillis = fechaLimite)
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fechaLimite = estadoFecha.selectedDateMillis
                        mostrarSelectorFecha = false
                    }
                ) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}

@Composable
private fun etiquetaDePrioridad(prioridad: Prioridad): String = stringResource(
    when (prioridad) {
        Prioridad.ALTA -> R.string.prioridad_alta
        Prioridad.MEDIA -> R.string.prioridad_media
        Prioridad.BAJA -> R.string.prioridad_baja
    }
)
