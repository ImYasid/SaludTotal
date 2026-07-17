package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.DoctorEntity

/**
 * Interfaz encargada de manejar la tabla "doctores".
 * Permite guardar médicos nuevos y filtrarlos según su especialidad.
 */
@Dao
interface DoctorDao {

    /**
     * Guarda un solo doctor en la base de datos. Si el doctor ya existe, lo reemplaza.
     *
     * @param doctor Objeto con los datos del médico.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(doctor: DoctorEntity)

    /**
     * Guarda una lista completa de doctores de un solo golpe.
     * Muy útil al inicializar la app por primera vez (SeedCallback).
     *
     * @param doctores Lista con los datos de los médicos a guardar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTodos(doctores: List<DoctorEntity>)

    /**
     * Busca únicamente a los doctores que pertenecen a una especialidad específica.
     * Esta es la función que alimenta las tarjetas visuales en el Paso 2 (DoctoresDisponibles).
     *
     * @param especialidadId El ID de la especialidad (ej. el ID de "Cardiología").
     * @return Lista de médicos que atienden esa especialidad.
     */
    @Query("SELECT * FROM doctores WHERE especialidadId = :especialidadId")
    fun obtenerPorEspecialidad(especialidadId: Int): List<DoctorEntity>
}