package com.joxcrts.mistareas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Prioridad de una tarea, ordenada de mayor a menor urgencia. */
enum class Prioridad {
    ALTA,
    MEDIA,
    BAJA
}

/**
 * Entidad principal de la app: una tarea pendiente o completada.
 *
 * @param fechaLimite fecha límite opcional en milisegundos (epoch), o null si no tiene.
 * @param creadaEn marca de tiempo de creación, usada para ordenar tareas recientes.
 */
@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descripcion: String = "",
    val prioridad: Prioridad = Prioridad.MEDIA,
    val completada: Boolean = false,
    val fechaLimite: Long? = null,
    val creadaEn: Long = System.currentTimeMillis()
)
