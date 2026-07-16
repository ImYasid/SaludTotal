package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una fila por cada botón de SpecialtiesActivity (Médico General, Cardiología, etc.).
 * Guardamos color e ícono como texto para poder seguir pintando los botones dinámicamente
 * en vez de tenerlos hardcodeados uno por uno en el XML.
 */
@Entity(tableName = "especialidades")
data class SpecialtyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val colorHex: String,
    // Nombre del drawable, ej. "ic_menu_search" (el mismo que ya usas en activity_specialties.xml)
    val iconoResName: String
)