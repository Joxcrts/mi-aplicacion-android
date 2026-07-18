package com.joxcrts.mistareas.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que abstrae el origen de datos (Room) del resto de la app.
 * Si en el futuro se añade sincronización remota, este es el único punto
 * que necesita cambiar.
 */
class TareaRepository(private val dao: TareaDao) {

    val tareas: Flow<List<Tarea>> = dao.observarTodas()

    suspend fun obtenerPorId(id: Long): Tarea? = dao.obtenerPorId(id)

    suspend fun guardar(tarea: Tarea): Long = dao.insertar(tarea)

    suspend fun actualizar(tarea: Tarea) = dao.actualizar(tarea)

    suspend fun eliminar(tarea: Tarea) = dao.eliminar(tarea)

    suspend fun alternarCompletada(tarea: Tarea) =
        dao.actualizar(tarea.copy(completada = !tarea.completada))

    suspend fun eliminarCompletadas() = dao.eliminarCompletadas()
}
