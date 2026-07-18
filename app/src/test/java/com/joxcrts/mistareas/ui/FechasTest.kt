package com.joxcrts.mistareas.ui

import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FechasTest {

    private fun millisDe(fecha: LocalDate): Long =
        fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `la medianoche UTC del DatePicker se convierte al dia elegido`() {
        val elegido = LocalDate.of(2026, 7, 18)
        assertEquals(elegido, fechaLimiteComoLocalDate(millisDe(elegido)))
    }

    @Test
    fun `una fecha anterior a hoy esta vencida`() {
        val hoy = LocalDate.of(2026, 7, 18)
        assertTrue(esFechaVencida(millisDe(hoy.minusDays(1)), hoy))
    }

    @Test
    fun `la fecha de hoy y las futuras no estan vencidas`() {
        val hoy = LocalDate.of(2026, 7, 18)
        assertFalse(esFechaVencida(millisDe(hoy), hoy))
        assertFalse(esFechaVencida(millisDe(hoy.plusDays(3)), hoy))
    }
}
