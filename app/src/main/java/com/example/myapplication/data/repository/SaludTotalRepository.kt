package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.AppointmentDao
import com.example.myapplication.data.local.dao.DoctorDao
import com.example.myapplication.data.local.dao.SpecialtyDao
import com.example.myapplication.data.local.dao.UserDao
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.example.myapplication.data.local.entity.DoctorEntity
import com.example.myapplication.data.local.entity.SpecialtyEntity
import com.example.myapplication.data.local.entity.UserEntity

/**
 * Punto único de entrada a los datos. Las Activities llaman a este repositorio
 * en vez de hablar directo con cada DAO — así, si mañana cambias de Room a una API
 * real, solo tienes que tocar esta clase, no las 8 pantallas.
 */
class SaludTotalRepository(
    private val userDao: UserDao,
    private val specialtyDao: SpecialtyDao,
    private val doctorDao: DoctorDao,
    private val appointmentDao: AppointmentDao
) {

    // ---------- Usuarios (Login / Registro) ----------

    /** Devuelve éxito con el id generado, o falla con un mensaje si la cédula ya existe. */
    suspend fun registrarUsuario(usuario: UserEntity): Result<Long> {
        return try {
            // ✅ CORREGIDO: Usamos el nombre exacto que está en el UserDao
            val existente = userDao.buscarUsuarioPorCedula(usuario.documentNumber)

            if (existente != null) {
                Result.failure(Exception("Ya existe una cuenta registrada con esa cédula"))
            } else {
                Result.success(userDao.insertar(usuario))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** null si la cédula o la contraseña no coinciden (mismo caso que hoy manejas en LoginActivity). */
    suspend fun iniciarSesion(documentNumber: String, password: String): UserEntity? {
        return userDao.iniciarSesion(documentNumber, password)
    }

    suspend fun obtenerUsuarioPorCedula(documentNumber: String): UserEntity? {
        // ✅ CORREGIDO: Usamos el nombre exacto que está en el UserDao
        return userDao.buscarUsuarioPorCedula(documentNumber)
    }

    suspend fun obtenerUsuarioPorId(id: Long): UserEntity? {
        return userDao.obtenerUsuarioPorId(id)
    }

    // ---------- Especialidades ----------

    suspend fun obtenerEspecialidades(): List<SpecialtyEntity> {
        return specialtyDao.obtenerTodas()
    }

    // ---------- Doctores ----------

    suspend fun obtenerDoctoresPorEspecialidad(especialidadId: Int): List<DoctorEntity> {
        return doctorDao.obtenerPorEspecialidad(especialidadId)
    }

    // ---------- Citas ----------

    /** Esto es lo que RevisarCita debería llamar al tocar "Sí, agendar mi cita". */
    suspend fun agendarCita(cita: AppointmentEntity): Long {
        return appointmentDao.insertar(cita)
    }

    suspend fun obtenerCitasDelUsuario(usuarioId: Int): List<AppointmentEntity> {
        return appointmentDao.obtenerCitasDelUsuario(usuarioId)
    }

    suspend fun cancelarCita(citaId: Int) {
        appointmentDao.actualizarEstado(citaId, "Cancelada")
    }
}