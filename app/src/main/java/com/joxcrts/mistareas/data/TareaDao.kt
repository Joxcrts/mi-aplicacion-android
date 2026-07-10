package com.joxcrts.mistareas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acceso a la tabla de tareas. Las consultas devuelven [Flow]
 * para que la interfaz se actualice automáticamente ante cualquier cambio.
 */
@Dao
interface TareaDao {

    @Query(
        """
        SELECT * FROM tareas
        ORDER BY completada ASC,
                 CASE prioridad
                     WHEN 'ALTA' THEN 0
                     WHEN 'MEDIA' THEN 1
                     ELSE 2
                 END ASC,
                 creadaEn DESC
        """
    )
    fun observarTodas(): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: Tarea): Long

    @Update
    suspend fun actualizar(tarea: Tarea)

    @Delete
    suspend fun eliminar(tarea: Tarea)

    @Query("DELETE FROM tareas WHERE completada = 1")
    suspend fun eliminarCompletadas()
}
