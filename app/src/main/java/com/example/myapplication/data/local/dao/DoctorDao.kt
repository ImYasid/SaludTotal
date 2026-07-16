package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
// 👇 ESTA ES LA LÍNEA QUE SEGURAMENTE FALTABA 👇
import com.example.myapplication.data.local.entity.DoctorEntity

@Dao
interface DoctorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(doctor: DoctorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTodos(doctores: List<DoctorEntity>)

    @Query("SELECT * FROM doctores WHERE especialidadId = :especialidadId")
    fun obtenerPorEspecialidad(especialidadId: Int): List<DoctorEntity>
}