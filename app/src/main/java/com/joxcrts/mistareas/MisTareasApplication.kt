package com.joxcrts.mistareas

import android.app.Application
import com.joxcrts.mistareas.data.AppDatabase
import com.joxcrts.mistareas.data.TareaRepository

/**
 * Clase Application que actúa como contenedor de dependencias de la app.
 * La base de datos y el repositorio se crean de forma perezosa (lazy),
 * solo cuando se necesitan por primera vez.
 */
class MisTareasApplication : Application() {

    private val database: AppDatabase by lazy { AppDatabase.obtener(this) }

    val repositorio: TareaRepository by lazy { TareaRepository(database.tareaDao()) }
}
