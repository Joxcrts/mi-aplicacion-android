package com.joxcrts.mistareas.ui

import com.joxcrts.mistareas.data.Prioridad
import com.joxcrts.mistareas.data.Tarea
import com.joxcrts.mistareas.data.TareaDao
import com.joxcrts.mistareas.data.TareaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * DAO falso en memoria para probar el ViewModel sin necesidad de Android ni Room.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private class TareaDaoFalso : TareaDao {
    private val tareas = MutableStateFlow<List<Tarea>>(emptyList())
    private var siguienteId = 1L

    override fun observarTodas(): Flow<List<Tarea>> = tareas

    override suspend fun obtenerPorId(id: Long): Tarea? =
        tareas.value.find { it.id == id }

    override suspend fun insertar(tarea: Tarea): Long {
        val id = if (tarea.id == 0L) siguienteId++ else tarea.id
        tareas.value = tareas.value.filter { it.id != id } + tarea.copy(id = id)
        return id
    }

    override suspend fun actualizar(tarea: Tarea) {
        tareas.value = tareas.value.map { if (it.id == tarea.id) tarea else it }
    }

    override suspend fun eliminar(tarea: Tarea) {
        tareas.value = tareas.value.filter { it.id != tarea.id }
    }

    override suspend fun eliminarCompletadas() {
        tareas.value = tareas.value.filter { !it.completada }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TareasViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: TareaDaoFalso
    private lateinit var viewModel: TareasViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        dao = TareaDaoFalso()
        viewModel = TareasViewModel(TareaRepository(dao))
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `guardar una tarea nueva la agrega a la lista`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Comprar pan", "", Prioridad.ALTA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals(1, estado.totales)
        assertEquals("Comprar pan", estado.tareas.first().titulo)
        recoleccion.cancel()
    }

    @Test
    fun `guardar con titulo vacio no crea nada`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "   ", "descripción", Prioridad.MEDIA, null)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totales)
        recoleccion.cancel()
    }

    @Test
    fun `el filtro de pendientes oculta las completadas`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Pendiente", "", Prioridad.MEDIA, null)
        viewModel.guardarTarea(0L, "Hecha", "", Prioridad.MEDIA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val hecha = viewModel.uiState.value.tareas.first { it.titulo == "Hecha" }
        viewModel.alternarCompletada(hecha)
        viewModel.cambiarFiltro(FiltroTareas.PENDIENTES)
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals(listOf("Pendiente"), estado.tareas.map { it.titulo })
        assertEquals(2, estado.totales)
        assertEquals(1, estado.completadas)
        recoleccion.cancel()
    }

    @Test
    fun `la busqueda filtra por titulo y descripcion`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Llamar al médico", "", Prioridad.MEDIA, null)
        viewModel.guardarTarea(0L, "Gimnasio", "rutina de pierna", Prioridad.BAJA, null)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.cambiarBusqueda("pierna")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Gimnasio"), viewModel.uiState.value.tareas.map { it.titulo })
        recoleccion.cancel()
    }

    @Test
    fun `eliminar y deshacer restaura la tarea`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Importante", "", Prioridad.ALTA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val tarea = viewModel.uiState.value.tareas.first()
        viewModel.eliminarTarea(tarea)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.totales)

        viewModel.deshacerEliminacion()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.totales)
        assertEquals("Importante", viewModel.uiState.value.tareas.first().titulo)
        recoleccion.cancel()
    }

    @Test
    fun `editar una tarea conserva su estado de completada`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Original", "", Prioridad.MEDIA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val tarea = viewModel.uiState.value.tareas.first()
        viewModel.alternarCompletada(tarea)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.guardarTarea(tarea.id, "Editada", "nueva descripción", Prioridad.ALTA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val editada = viewModel.uiState.value.tareas.first()
        assertEquals("Editada", editada.titulo)
        assertTrue(editada.completada)
        assertEquals(1, viewModel.uiState.value.completadas)
        recoleccion.cancel()
    }

    @Test
    fun `eliminar completadas conserva las pendientes`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Pendiente", "", Prioridad.MEDIA, null)
        viewModel.guardarTarea(0L, "Hecha", "", Prioridad.MEDIA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val hecha = viewModel.uiState.value.tareas.first { it.titulo == "Hecha" }
        viewModel.alternarCompletada(hecha)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.eliminarCompletadas()
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals(listOf("Pendiente"), estado.tareas.map { it.titulo })
        assertEquals(0, estado.completadas)
        recoleccion.cancel()
    }

    @Test
    fun `los contadores de pendientes y todo completado son coherentes`() =
        runTest(dispatcher.scheduler) {
            val recoleccion = launch { viewModel.uiState.collect {} }

            viewModel.guardarTarea(0L, "Única", "", Prioridad.MEDIA, null)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.pendientes)
            assertTrue(!viewModel.uiState.value.todoCompletado)

            viewModel.alternarCompletada(viewModel.uiState.value.tareas.first())
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.pendientes)
            assertTrue(viewModel.uiState.value.todoCompletado)
            recoleccion.cancel()
        }

    @Test
    fun `el progreso refleja la proporcion de completadas`() = runTest(dispatcher.scheduler) {
        val recoleccion = launch { viewModel.uiState.collect {} }

        viewModel.guardarTarea(0L, "Una", "", Prioridad.MEDIA, null)
        viewModel.guardarTarea(0L, "Dos", "", Prioridad.MEDIA, null)
        dispatcher.scheduler.advanceUntilIdle()

        val primera = viewModel.uiState.value.tareas.first()
        viewModel.alternarCompletada(primera)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.progreso in 0.49f..0.51f)
        recoleccion.cancel()
    }
}
