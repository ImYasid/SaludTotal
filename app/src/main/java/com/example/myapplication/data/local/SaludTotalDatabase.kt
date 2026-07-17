package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.AppointmentDao
import com.example.myapplication.data.local.dao.DoctorDao
import com.example.myapplication.data.local.dao.SpecialtyDao
import com.example.myapplication.data.local.dao.UserDao
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.example.myapplication.data.local.entity.DoctorEntity
import com.example.myapplication.data.local.entity.SpecialtyEntity
import com.example.myapplication.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase principal de la base de datos local usando la librería Room.
 * Actúa como el motor central que administra todas las tablas (Entidades) de la aplicación.
 *
 * Si modificas alguna tabla (agregas o quitas columnas), debes subir el número de "version".
 */
@Database(
    entities = [
        UserEntity::class,
        SpecialtyEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SaludTotalDatabase : RoomDatabase() {

    // Declaración de los "mensajeros" (DAOs) que permiten acceder a cada tabla
    abstract fun userDao(): UserDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        // Variable volátil para que todos los hilos del procesador vean siempre la misma instancia
        @Volatile
        private var INSTANCE: SaludTotalDatabase? = null

        /**
         * Aplica el patrón de diseño "Singleton".
         * Garantiza que el celular abra una sola conexión a la base de datos para toda la aplicación.
         * Si múltiples pantallas intentan crear una base de datos al mismo tiempo, esto evita
         * que la aplicación colapse o consuma demasiada memoria.
         *
         * @param context El contexto de la aplicación.
         * @return La única instancia activa de SaludTotalDatabase.
         */
        fun getDatabase(context: Context): SaludTotalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    SaludTotalDatabase::class.java,
                    "saludtotal_db"
                )
                    // Conecta el "sembrador" de datos iniciales
                    .addCallback(SeedCallback(context))
                    // Ideal para proyectos académicos: Si detecta que cambiaste el esquema (versión),
                    // borra la base vieja y crea una limpia, evitando que la app crashee pidiendo una Migración.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instancia
                instancia
            }
        }
    }

    /**
     * Clase auxiliar encargada de "sembrar" (precargar) datos iniciales en la base de datos.
     * El método [onCreate] se ejecuta ÚNICAMENTE la primera vez que la app crea el archivo .db
     * en el almacenamiento del celular. Así aseguramos que la app nunca inicie con pantallas vacías.
     */
    private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Usamos un hilo secundario (Dispatchers.IO) porque guardar datos puede ser lento
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)

                // ---> 1. Creamos e insertamos el catálogo base de Especialidades <---
                val especialidades = listOf(
                    SpecialtyEntity(
                        nombre = "Médico General",
                        colorHex = "#3B82F6",
                        iconoResName = "outline_medical_services_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Cardiología",
                        colorHex = "#EF4444",
                        iconoResName = "outline_cardiology_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Traumatología",
                        colorHex = "#F97316",
                        iconoResName = "outline_accessibility_new_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Oftalmología",
                        colorHex = "#A855F7",
                        iconoResName = "outline_visibility_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Neurología",
                        colorHex = "#6366F1",
                        iconoResName = "outline_neurology_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Vacunación",
                        colorHex = "#22C55E",
                        iconoResName = "outline_vaccines_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Odontología",
                        colorHex = "#00BCD4",
                        iconoResName = "outline_dentistry_24"
                    ),
                    SpecialtyEntity(
                        nombre = "Pediatría",
                        colorHex = "#FF9800",
                        iconoResName = "outline_pediatrics_24"
                    )
                )
                database.specialtyDao().insertarTodas(especialidades)

                // ---> 2. Recuperamos los IDs dinámicos que Room les asignó <---
                // Necesitamos los IDs reales para poder decirle a Room a qué especialidad pertenece cada doctor
                val especialidadesGuardadas = database.specialtyDao().obtenerTodas()

                val idMedicoGeneral = especialidadesGuardadas.first { it.nombre == "Médico General" }.id
                val idCardiologia = especialidadesGuardadas.first { it.nombre == "Cardiología" }.id
                val idOdontologia = especialidadesGuardadas.first { it.nombre == "Odontología" }.id
                val idPediatria = especialidadesGuardadas.first { it.nombre == "Pediatría" }.id

                // ---> 3. Creamos e insertamos a los Doctores conectados a sus Especialidades <---
                val doctores = listOf(
                    // Médicos Generales
                    DoctorEntity(
                        nombre = "Dr. Juan Pérez",
                        especialidadId = idMedicoGeneral,
                        direccion = "Av. Principal #123, Centro",
                        distanciaKm = 0.5
                    ),
                    DoctorEntity(
                        nombre = "Dra. María González",
                        especialidadId = idMedicoGeneral,
                        direccion = "Calle Flores #456, Norte",
                        distanciaKm = 1.2
                    ),
                    // Cardiólogos
                    DoctorEntity(
                        nombre = "Dr. Carlos Ruiz",
                        especialidadId = idCardiologia,
                        direccion = "Clínica del Corazón - Piso 3",
                        distanciaKm = 3.4
                    ),
                    // Odontólogos
                    DoctorEntity(
                        nombre = "Dra. Ana López",
                        especialidadId = idOdontologia,
                        direccion = "Torre Médica 2 - Consultorio 4B",
                        distanciaKm = 2.1
                    ),
                    // Pediatras
                    DoctorEntity(
                        nombre = "Dr. Luis Martínez",
                        especialidadId = idPediatria,
                        direccion = "Centro Pediátrico Sonrisas",
                        distanciaKm = 4.0
                    )
                )
                database.doctorDao().insertarTodos(doctores)
            }
        }
    }
}