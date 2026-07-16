package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.AppointmentEntity

@Dao
interface AppointmentDao {

    // Esto es lo que debería llamarse desde RevisarCita cuando el usuario toca
    // "Sí, agendar mi cita" (hoy ese botón solo navega a CitaExitosa sin guardar nada).
    @Insert
    fun insertar(cita: AppointmentEntity): Long

    @Query("SELECT * FROM citas WHERE usuarioId = :usuarioId ORDER BY fechaHoraMillis ASC")
    fun obtenerCitasDelUsuario(usuarioId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM citas WHERE id = :citaId LIMIT 1")
    fun obtenerPorId(citaId: Int): AppointmentEntity?

    @Query("UPDATE citas SET estado = :nuevoEstado WHERE id = :citaId")
    fun actualizarEstado(citaId: Int, nuevoEstado: String)

    @Query("DELETE FROM citas WHERE id = :citaId")
    fun eliminar(citaId: Int)
}