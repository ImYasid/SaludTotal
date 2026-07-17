package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una cita médica agendada en la base de datos (Tabla "citas").
 *
 * Esta tabla conecta a un Paciente (UserEntity) con un Médico (DoctorEntity).
 * Al usar "ForeignKey.CASCADE", le decimos a la base de datos: "Si este paciente
 * o este doctor es eliminado del sistema, borra automáticamente todas sus citas".
 *
 * @param id Identificador numérico único autogenerado para esta cita.
 * @param usuarioId El ID del paciente dueño de la cita (Llave foránea).
 * @param doctorId El ID del médico asignado (Llave foránea).
 * @param doctorNombre Nombre del doctor (Se guarda como texto para no tener que buscarlo en otra tabla al mostrar la lista).
 * @param especialidadNombre Nombre de la especialidad (ej. "Cardiología").
 * @param direccion Ubicación del consultorio.
 * @param fechaTexto Fecha formateada para mostrar en pantalla (ej. "Lunes 15 de Mayo").
 * @param horaTexto Hora seleccionada por el usuario (ej. "9:00 AM").
 * @param fechaHoraMillis Momento exacto de la cita en milisegundos (Vital para integrarlo con el calendario del celular).
 * @param estado Indica el estado actual ("Agendada", "Cancelada" o "Completada").
 * @param fechaCreacionMillis Cuándo se registró esta cita en el sistema.
 */
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId"), Index("doctorId")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Long,
    val doctorId: Int,
    val doctorNombre: String,
    val especialidadNombre: String,
    val direccion: String,
    val fechaTexto: String,
    val horaTexto: String,
    val fechaHoraMillis: Long,
    val estado: String = "Agendada",
    val fechaCreacionMillis: Long = System.currentTimeMillis()
)