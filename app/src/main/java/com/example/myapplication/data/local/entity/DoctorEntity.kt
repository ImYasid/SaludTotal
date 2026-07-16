package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un doctor pertenece a una especialidad (FK). Guarda dirección y distancia
 * porque DoctoresDisponibles ya muestra esos dos datos en cada tarjeta —
 * antes estaban escritos a mano ("Av. Principal #123, Centro", "0.5 km de distancia").
 *
 * onDelete = CASCADE: si se borra una especialidad, se borran sus doctores con ella,
 * para no dejar doctores "huérfanos" apuntando a una especialidad que ya no existe.
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