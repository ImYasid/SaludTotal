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

@Database(
    entities = [
        UserEntity::class,
        SpecialtyEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SaludTotalDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: SaludTotalDatabase? = null

        fun getDatabase(context: Context): SaludTotalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    SaludTotalDatabase::class.java,
                    "saludtotal_db"
                )
                    .addCallback(SeedCallback(context))
                    // Para un proyecto académico esto está bien: si cambias el esquema mientras
                    // desarrollas, borra la base y la crea de nuevo en vez de pedirte una migración.
                    // Antes de entregar el proyecto, considera escribir una Migration real.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instancia
                instancia
            }
        }
    }

    /**
     * Se ejecuta SOLO la primera vez que se crea la base de datos (cuando el archivo .db
     * no existe todavía en el celular). Aquí sembramos las mismas especialidades y doctores
     * que hoy tienes escritos a mano en el XML, para que la app funcione desde el primer
     * arranque sin pantallas vacías.
     */
    private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)

                // 1. Agregamos las nuevas especialidades a la lista
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
                    // 👇 NUEVAS ESPECIALIDADES AQUÍ 👇
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

                // 2. Necesitamos los ids reales que Room asignó para poder enlazar los doctores.
                val especialidadesGuardadas = database.specialtyDao().obtenerTodas()

                val idMedicoGeneral =
                    especialidadesGuardadas.first { it.nombre == "Médico General" }.id
                val idCardiologia = especialidadesGuardadas.first { it.nombre == "Cardiología" }.id
                val idOdontologia = especialidadesGuardadas.first { it.nombre == "Odontología" }.id
                val idPediatria = especialidadesGuardadas.first { it.nombre == "Pediatría" }.id

                // 3. Creamos los doctores asignándoles su especialidad respectiva
                val doctores = listOf(
                    // -- Médicos Generales --
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
                    // -- Cardiólogos --
                    DoctorEntity(
                        nombre = "Dr. Carlos Ruiz",
                        especialidadId = idCardiologia,
                        direccion = "Clínica del Corazón - Piso 3",
                        distanciaKm = 3.4
                    ),
                    // -- Odontólogos --
                    DoctorEntity(
                        nombre = "Dra. Ana López",
                        especialidadId = idOdontologia,
                        direccion = "Torre Médica 2 - Consultorio 4B",
                        distanciaKm = 2.1
                    ),
                    // -- Pediatras --
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