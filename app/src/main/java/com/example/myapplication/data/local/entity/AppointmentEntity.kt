package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val doctorNombre: String,    // <--- ¡NUEVO CAMPO AGREGADO!
    val especialidadNombre: String,
    val direccion: String,
    val fechaTexto: String,
    val horaTexto: String,
    val fechaHoraMillis: Long,
    val estado: String = "Agendada",
    val fechaCreacionMillis: Long = System.currentTimeMillis()
)