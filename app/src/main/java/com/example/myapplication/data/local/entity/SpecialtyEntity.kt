package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una categoría médica dentro de la aplicación (Tabla "especialidades").
 *
 * En lugar de dibujar botones fijos (hardcodeados) en el archivo XML, la pantalla
 * SpecialtiesActivity lee esta tabla y genera los botones dinámicamente. Esto permite
 * agregar o quitar especialidades a futuro sin tener que reprogramar la pantalla.
 *
 * @param id Identificador único autogenerado.
 * @param nombre Nombre de la especialidad que aparecerá impreso en el botón (ej. "Cardiología").
 * @param colorHex Código de color hexadecimal que pintará el fondo del botón (ej. "#EF4444").
 * @param iconoResName Nombre del archivo de imagen (drawable) nativo de Android que se usará como ícono.
 */
@Entity(tableName = "especialidades")
data class SpecialtyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val colorHex: String,
    val iconoResName: String
)