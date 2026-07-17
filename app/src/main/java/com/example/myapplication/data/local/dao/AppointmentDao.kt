package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.AppointmentEntity

/**
 * Interfaz encargada de manejar todas las operaciones de la tabla "citas".
 * Actúa como un puente para guardar, leer, actualizar o borrar citas médicas.
 */
@Dao
interface AppointmentDao {

    /**
     * Guarda una nueva cita médica en la base de datos.
     * Esta es la función que se ejecuta cuando el usuario confirma en la pantalla "RevisarCita".
     *
     * @param cita El objeto con todos los datos de la cita a guardar.
     * @return El número de ID (Long) que la base de datos le asignó a esta nueva cita.
     */
    @Insert
    fun insertar(cita: AppointmentEntity): Long

    /**
     * Busca todas las citas agendadas por un usuario específico, ordenadas cronológicamente
     * (de la más próxima a la más lejana) usando su fecha exacta en milisegundos.
     *
     * @param usuarioId El ID numérico del usuario actual.
     * @return Una lista de citas (puede estar vacía si no tiene ninguna).
     */
    @Query("SELECT * FROM citas WHERE usuarioId = :usuarioId ORDER BY fechaHoraMillis ASC")
    fun obtenerCitasDelUsuario(usuarioId: Int): List<AppointmentEntity>

    /**
     * Busca una cita específica usando su número de ID único.
     *
     * @param citaId El ID de la cita que queremos encontrar.
     * @return El objeto de la cita, o nulo si no existe.
     */
    @Query("SELECT * FROM citas WHERE id = :citaId LIMIT 1")
    fun obtenerPorId(citaId: Int): AppointmentEntity?

    /**
     * Cambia el estado de una cita (por ejemplo, de "Agendada" a "Completada").
     *
     * @param citaId El ID de la cita a modificar.
     * @param nuevoEstado El texto con el nuevo estado.
     */
    @Query("UPDATE citas SET estado = :nuevoEstado WHERE id = :citaId")
    fun actualizarEstado(citaId: Int, nuevoEstado: String)

    /**
     * Borra permanentemente una cita de la base de datos.
     * Esta es la función que se ejecuta desde la pantalla "Mis Citas".
     *
     * @param citaId El ID de la cita que se va a eliminar.
     */
    @Query("DELETE FROM citas WHERE id = :citaId")
    fun eliminar(citaId: Int)
}