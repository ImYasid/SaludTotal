package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.UserEntity

/**
 * Interfaz encargada de gestionar las cuentas de usuario en la tabla "users".
 * Controla el registro de cuentas nuevas y el proceso de inicio de sesión.
 */
@Dao
interface UserDao {

    /**
     * Crea una cuenta nueva en la aplicación (Registro).
     * Si la cédula ya existe, lanza un error (ABORT) para evitar cuentas duplicadas.
     *
     * @param usuario Objeto con los datos capturados en el formulario de registro.
     * @return El ID numérico generado para este nuevo usuario.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertar(usuario: UserEntity): Long

    /**
     * Busca a un usuario sabiendo únicamente su número de cédula.
     * Útil para saber si una cuenta ya existe antes de intentar registrarla de nuevo.
     *
     * @param cedula El número de documento (10 dígitos).
     * @return Los datos de la cuenta, o nulo si nadie se ha registrado con esa cédula.
     */
    @Query("SELECT * FROM users WHERE documentNumber = :cedula LIMIT 1")
    fun buscarUsuarioPorCedula(cedula: String): UserEntity?

    /**
     * Valida el inicio de sesión comparando la cédula y la contraseña al mismo tiempo.
     * Si las credenciales son correctas, da acceso a la aplicación.
     *
     * @param documentNumber El número de cédula ingresado en el login.
     * @param password La contraseña ingresada en el login.
     * @return El objeto del usuario si los datos coinciden, o nulo si son incorrectos.
     */
    @Query("SELECT * FROM users WHERE documentNumber = :documentNumber AND password = :password LIMIT 1")
    fun iniciarSesion(documentNumber: String, password: String): UserEntity?

    /**
     * Busca los datos de un usuario usando el ID interno de la base de datos.
     * Se usa en la pantalla de Bienvenida para extraer el nombre real y mostrar el saludo.
     *
     * @param id El ID interno de la sesión abierta.
     * @return Los datos completos de ese usuario.
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun obtenerUsuarioPorId(id: Long): UserEntity?

    /**
     * Actualiza la información de un usuario existente (ej. si cambia su correo o contraseña).
     *
     * @param usuario Objeto de usuario con los datos modificados.
     */
    @Update
    fun actualizar(usuario: UserEntity)
}