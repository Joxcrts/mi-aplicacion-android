package com.joxcrts.mistareas.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Utilidades para la fecha límite de las tareas.
 *
 * El DatePicker de Material 3 devuelve la medianoche UTC del día elegido,
 * por lo que la conversión debe hacerse siempre en UTC: interpretarla con
 * la zona horaria local mostraría el día anterior en zonas con offset
 * negativo (por ejemplo, toda América).
 */

/** Convierte los milisegundos del DatePicker al día del calendario elegido. */
fun fechaLimiteComoLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

/** True si la fecha límite ya pasó respecto a [hoy]. */
fun esFechaVencida(millis: Long, hoy: LocalDate = LocalDate.now()): Boolean =
    fechaLimiteComoLocalDate(millis).isBefore(hoy)

/** Formatea la fecha límite en el formato medio del idioma del dispositivo. */
fun formatearFechaLimite(millis: Long): String =
    fechaLimiteComoLocalDate(millis)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
