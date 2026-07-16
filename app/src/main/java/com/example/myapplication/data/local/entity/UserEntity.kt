package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Los 5 datos reales que se piden al registrar una cuenta:
 * - documentNumber: cédula, string de 10 dígitos, ÚNICA (índice unique abajo)
 * - fullName: nombres completos
 * - birthDate: fecha de nacimiento (guardada como texto, ej. "16/07/1998")
 * - email: correo
 * - password: contraseña
 *
 * Quité "phoneNumber" porque no lo estás pidiendo en ninguna pantalla, y renombré
 * "pin" a "password" para que sea claro qué es. Si más adelante SÍ necesitas teléfono,
 * agrégalo aquí y en RegisterActivity al mismo tiempo.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["documentNumber"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentNumber: String,
    val fullName: String,
    val birthDate: String,
    val email: String,
    val password: String
)