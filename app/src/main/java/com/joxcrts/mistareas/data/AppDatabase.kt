package com.joxcrts.mistareas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos Room de la aplicación. Se expone como singleton para
 * evitar múltiples instancias abiertas sobre el mismo archivo.
 */
@Database(entities = [Tarea::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_tareas.db"
                ).build().also { instancia = it }
            }
        }
    }
}
