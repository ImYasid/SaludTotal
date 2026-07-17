package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.SpecialtyEntity

/**
 * Interfaz encargada de manejar la tabla "especialidades".
 * Permite guardar el catálogo de especialidades y mostrarlas en pantalla.
 */
@Dao
interface SpecialtyDao {

    /**
     * Guarda una lista inicial de especialidades. Si una ya existe, la ignora para no duplicarla.
     *
     * @param especialidades Lista de especialidades (nombre, color, ícono).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarTodas(especialidades: List<SpecialtyEntity>)

    /**
     * Trae todo el catálogo de especialidades médicas ordenadas alfabéticamente (A-Z).
     * Esta función es la que dibuja los botones de colores dinámicos en el Paso 1 (SpecialtiesActivity).
     *
     * @return Lista completa de todas las especialidades disponibles.
     */
    @Query("SELECT * FROM especialidades ORDER BY nombre ASC")
    fun obtenerTodas(): List<SpecialtyEntity>

    /**
     * Busca una especialidad específica a través de su ID.
     *
     * @param id El ID de la especialidad buscada.
     * @return Los datos de la especialidad, o nulo si no la encuentra.
     */
    @Query("SELECT * FROM especialidades WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): SpecialtyEntity?
}