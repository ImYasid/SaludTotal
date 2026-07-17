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
 * Repositorio Central de Datos (Patrón de diseño Repository).
 *
 * Actúa como un intermediario o "puente" entre las pantallas de la aplicación (Activities)
 * y las fuentes de datos (en este caso, los DAOs de la base de datos Room).
 *
 * Ventaja principal: Las pantallas no saben cómo ni dónde se guardan los datos.
 * Si mañana decides conectar tu app a internet (API REST) en lugar de usar Room,
 * solo tendrás que modificar esta clase, sin tocar ni una sola línea de tus 8 pantallas.
 */
class SaludTotalRepository(
    private val userDao: UserDao,
    private val specialtyDao: SpecialtyDao,
    private val doctorDao: DoctorDao,
    private val appointmentDao: AppointmentDao
) {

    // ==========================================
    // Módulo de Usuarios (Login y Registro)
    // ==========================================

    /**
     * Intenta registrar una nueva cuenta verificando primero que la cédula no exista.
     * Todas las funciones aquí usan `suspend` porque se ejecutan de fondo (Corrutinas).
     *
     * @param usuario Objeto con los datos del nuevo paciente.
     * @return Un objeto [Result] que envuelve el éxito (con el ID generado) o el fracaso (con el mensaje de error).
     */
    suspend fun registrarUsuario(usuario: UserEntity): Result<Long> {
        return try {
            // 1. Preguntamos a la base de datos si ya hay alguien con esa cédula
            val existente = userDao.buscarUsuarioPorCedula(usuario.documentNumber)

            if (existente != null) {
                // Si existe, devolvemos un error para que la pantalla lo muestre en rojo
                Result.failure(Exception("Ya existe una cuenta registrada con esa cédula"))
            } else {
                // Si está libre, lo insertamos y devolvemos éxito
                Result.success(userDao.insertar(usuario))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si las credenciales ingresadas en el Login son correctas.
     *
     * @param documentNumber La cédula ingresada.
     * @param password La contraseña ingresada.
     * @return Los datos del usuario si hay éxito, o `null` si se equivocó en algo.
     */
    suspend fun iniciarSesion(documentNumber: String, password: String): UserEntity? {
        return userDao.iniciarSesion(documentNumber, password)
    }

    /**
     * Busca a un usuario sabiendo solo su número de cédula.
     *
     * @param documentNumber Cédula ecuatoriana a buscar.
     * @return Datos del usuario o nulo.
     */
    suspend fun obtenerUsuarioPorCedula(documentNumber: String): UserEntity? {
        return userDao.buscarUsuarioPorCedula(documentNumber)
    }

    /**
     * Busca los datos de un usuario usando su ID interno.
     * Ideal para cargar perfiles o recuperar el nombre en la pantalla de Bienvenida.
     *
     * @param id ID interno de la base de datos.
     * @return Datos del usuario o nulo.
     */
    suspend fun obtenerUsuarioPorId(id: Long): UserEntity? {
        return userDao.obtenerUsuarioPorId(id)
    }

    // ==========================================
    // Módulo de Especialidades
    // ==========================================

    /**
     * Pide a la base de datos el catálogo completo de especialidades.
     *
     * @return Lista de especialidades médicas (ordenadas de la A a la Z según el DAO).
     */
    suspend fun obtenerEspecialidades(): List<SpecialtyEntity> {
        return specialtyDao.obtenerTodas()
    }

    // ==========================================
    // Módulo de Doctores
    // ==========================================

    /**
     * Filtra a los médicos disponibles para una especialidad en específico.
     *
     * @param especialidadId El número de ID de la especialidad elegida (ej. 2 para Cardiología).
     * @return Lista de médicos que pertenecen exclusivamente a esa área.
     */
    suspend fun obtenerDoctoresPorEspecialidad(especialidadId: Int): List<DoctorEntity> {
        return doctorDao.obtenerPorEspecialidad(especialidadId)
    }

    // ==========================================
    // Módulo de Citas
    // ==========================================

    /**
     * Guarda oficialmente una nueva cita en el sistema.
     *
     * @param cita Objeto con toda la información empaquetada de la cita.
     * @return El número de confirmación (ID) de la cita guardada.
     */
    suspend fun agendarCita(cita: AppointmentEntity): Long {
        return appointmentDao.insertar(cita)
    }

    /**
     * Recupera todo el historial de citas de un solo paciente.
     *
     * @param usuarioId El ID del paciente que tiene la sesión abierta.
     * @return Lista de las citas agendadas por este usuario.
     */
    suspend fun obtenerCitasDelUsuario(usuarioId: Int): List<AppointmentEntity> {
        return appointmentDao.obtenerCitasDelUsuario(usuarioId)
    }

    /**
     * Cancelación lógica: No borra la cita de la base de datos, solo le cambia la etiqueta
     * a "Cancelada" para que quede un registro histórico. (Útil para estadísticas a futuro).
     *
     * @param citaId El ID de la cita a cancelar.
     */
    suspend fun cancelarCita(citaId: Int) {
        appointmentDao.actualizarEstado(citaId, "Cancelada")
    }

    /**
     * Borrado físico: Elimina permanentemente la cita de la base de datos.
     * Es la función que usa la pantalla de "Mis Citas" actualmente.
     *
     * @param citaId El ID de la cita a eliminar.
     */
    suspend fun eliminarCita(citaId: Int) {
        appointmentDao.eliminar(citaId)
    }
}