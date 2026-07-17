package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a un médico disponible en el sistema (Tabla "doctores").
 *
 * Cada doctor pertenece estrictamente a una especialidad mediante una llave foránea (FK).
 * "onDelete = CASCADE" garantiza que, si se elimina una especialidad (ej. se deja de atender Pediatría),
 * todos los pediatras se borren automáticamente para no dejar doctores "huérfanos".
 *
 * @param id Identificador único autogenerado para el doctor.
 * @param nombre Nombre completo del profesional (ej. "Dr. Juan Pérez").
 * @param especialidadId El ID de la especialidad a la que pertenece este doctor.
 * @param direccion Dirección física del consultorio o clínica donde atiende.
 * @param distanciaKm Distancia estimada usada para mostrarse en la tarjeta de información.
 */
@Entity(
    tableName = "doctores",
    foreignKeys = [
        ForeignKey(
            entity = SpecialtyEntity::class,
            parentColumns = ["id"],
            childColumns = ["especialidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("especialidadId")]
)
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val especialidadId: Int,
    val direccion: String,
    val distanciaKm: Double
)