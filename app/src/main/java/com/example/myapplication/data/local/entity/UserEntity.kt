package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa la cuenta de un paciente registrado en la aplicación (Tabla "users").
 *
 * La propiedad 'indices' configura el número de cédula (documentNumber) como ÚNICO.
 * Esto asegura, a nivel de base de datos, que es matemáticamente imposible registrar
 * dos cuentas diferentes con el mismo número de documento.
 *
 * @param id Identificador único autogenerado para el usuario. Es el ID que viaja por toda la app para mantener la sesión.
 * @param documentNumber Número de cédula ecuatoriana (10 dígitos exactos).
 * @param fullName Nombres y apellidos completos del paciente.
 * @param birthDate Fecha de nacimiento guardada en formato de texto limpio (ej. "16/07/1998").
 * @param email Correo electrónico de contacto.
 * @param password Contraseña elegida por el usuario para acceder a su cuenta.
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