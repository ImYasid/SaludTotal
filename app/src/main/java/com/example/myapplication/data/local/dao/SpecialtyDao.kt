package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.SpecialtyEntity

@Dao
interface SpecialtyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarTodas(especialidades: List<SpecialtyEntity>)

    // Esto es lo que va a llenar el GridLayout de SpecialtiesActivity en vez de
    // los 6 MaterialButton escritos a mano en el XML.
    @Query("SELECT * FROM especialidades ORDER BY nombre ASC")
     fun obtenerTodas(): List<SpecialtyEntity>

    @Query("SELECT * FROM especialidades WHERE id = :id LIMIT 1")
     fun obtenerPorId(id: Int): SpecialtyEntity?
}