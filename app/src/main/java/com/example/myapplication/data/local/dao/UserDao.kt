package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.UserEntity

@Dao
interface UserDao {

    // ABORT: si la cédula ya existe (índice único en documentNumber), lanza excepción
    // en vez de sobrescribir la cuenta. El repositorio la convierte en un mensaje claro.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertar(usuario: UserEntity): Long

    // ✅ CORRECTO: Busca específicamente el documento ingresado
    @Query("SELECT * FROM users WHERE documentNumber = :cedula LIMIT 1")
    fun buscarUsuarioPorCedula(cedula: String): UserEntity?

    // Reemplaza la validación manual que hoy hace LoginActivity contra SharedPreferences
    @Query("SELECT * FROM users WHERE documentNumber = :documentNumber AND password = :password LIMIT 1")
    fun iniciarSesion(documentNumber: String, password: String): UserEntity?

    // 👇 ESTA ES LA FUNCIÓN QUE FALTABA (Para que WelcomeActivity sepa tu nombre) 👇
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun obtenerUsuarioPorId(id: Long): UserEntity?

    @Update
    fun actualizar(usuario: UserEntity)
}