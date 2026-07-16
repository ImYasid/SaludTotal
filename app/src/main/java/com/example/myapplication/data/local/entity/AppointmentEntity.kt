package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Esta es la entidad que probablemente tenías más incompleta: hoy Horario/RevisarCita/
 * CitaExitosa se pasan estos mismos 6 datos por Intent (especialidad, doctor, dirección,
 * fecha, hora y fechaHoraMillis) pero nunca se guardan en ningún lado. Esta tabla es
 * donde deberían terminar guardándose cuando el usuario confirma en RevisarCita.
 *
 * Guardamos especialidadNombre como texto (no solo el id) para no tener que hacer un JOIN
 * cada vez que quieras mostrar "Cardiología" en una lista de citas o en el resumen final.
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
    val especialidadNombre: String,
    val direccion: String,
    val fechaTexto: String,      // Ej. "Miércoles 16 de Julio" (para mostrar tal cual en pantalla)
    val horaTexto: String,       // Ej. "9:00 AM"
    val fechaHoraMillis: Long,   // La misma que ya calculas en Horario.kt para el evento de calendario
    val estado: String = "Agendada", // "Agendada" | "Cancelada" | "Completada"
    val fechaCreacionMillis: Long = System.currentTimeMillis()
)