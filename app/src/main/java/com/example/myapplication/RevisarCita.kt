package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de "Revisión de Cita" (Paso final antes de guardar).
 * Muestra al usuario un resumen detallado con todos los datos que ha elegido hasta el momento.
 * Si el usuario confirma, esta clase se encarga de guardar oficialmente la cita
 * en la base de datos (Room) y avanzar a la pantalla de éxito.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "especialidad" (String): Nombre de la especialidad elegida (ej. "Pediatría").
 * - "doctor" (String): Nombre del médico asignado.
 * - "direccion" (String): Ubicación del consultorio.
 * - "fecha" (String): Fecha elegida en formato de texto amigable.
 * - "hora" (String): Hora de la cita.
 * - "fechaHoraMillis" (Long): Milisegundos exactos de la cita (útil para eventos de calendario).
 * - "usuarioId" (Long): ID del usuario que está conectado actualmente.
 * - "doctorId" (Int): ID del médico elegido en la base de datos.
 */
class RevisarCita : AppCompatActivity() {

    // Conexión con la base de datos para poder insertar (guardar) la nueva cita
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Recibe los datos enviados desde la pantalla de Horarios, verifica que no falte nada,
     * los muestra en las tarjetas resumen y configura el botón de guardar en la base de datos.
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_revisar_cita)

        // Configuración para que el diseño respete las barras del sistema (batería, reloj, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ---> 1. Recibir los datos reales que llegan desde el Paso 3 (Horario) <---
        val especialidad = intent.getStringExtra("especialidad")
        val doctor = intent.getStringExtra("doctor")
        val direccion = intent.getStringExtra("direccion")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")
        val fechaHoraMillis = intent.getLongExtra("fechaHoraMillis", 0L)
        val usuarioId = intent.getLongExtra("usuarioId", -1L)
        val doctorId = intent.getIntExtra("doctorId", -1)

        // Verificamos de forma estricta que haya llegado toda la información.
        // Si falta algo, bloqueamos el proceso para evitar que la app crashee o guarde datos en blanco.
        if (especialidad == null || doctor == null || fecha == null || hora == null || usuarioId == -1L || doctorId == -1) {
            Toast.makeText(this, "Faltan datos de la cita. Vuelve a intentarlo desde el inicio.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Enlazamos los textos del diseño y los llenamos con la información recibida
        val tvTituloEspecialidad = findViewById<TextView>(R.id.tvTituloEspecialidad)
        val tvDoctor = findViewById<TextView>(R.id.tvDoctor)
        val tvConsultorio = findViewById<TextView>(R.id.tvConsultorio)
        val tvFecha = findViewById<TextView>(R.id.tvFecha)
        val tvHora = findViewById<TextView>(R.id.tvHora)

        tvTituloEspecialidad.text = "Tu cita de $especialidad"
        tvDoctor.text = doctor
        tvConsultorio.text = direccion ?: "Consultorio principal"
        tvFecha.text = fecha
        tvHora.text = hora

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)
        val btnCorregir = findViewById<MaterialButton>(R.id.btnCorregir)

        // Si el usuario quiere regresar a cambiar algo, simplemente cerramos esta pantalla
        btnVolver.setOnClickListener { finish() }
        btnCorregir.setOnClickListener { finish() }

        // ---> 3. Lógica del Botón Confirmar (Guardar en Base de Datos) <---
        btnConfirmar.setOnClickListener {

            // Apagamos el botón momentáneamente para evitar que lo toquen dos veces
            // y se guarden dos citas idénticas por accidente
            btnConfirmar.isEnabled = false

            // Creamos el "paquete" con los datos que exige la base de datos
            val nuevaCita = AppointmentEntity(
                usuarioId = usuarioId,
                doctorId = doctorId,
                doctorNombre = doctor,
                especialidadNombre = especialidad,
                direccion = direccion ?: "Consultorio principal",
                fechaTexto = fecha,
                horaTexto = hora,
                fechaHoraMillis = fechaHoraMillis
            )

            // Usamos hilos secundarios (Corrutinas) para no trabar el celular mientras se guarda
            lifecycleScope.launch {
                try {
                    // Guardamos oficialmente en Room (Dispatchers.IO es el hilo adecuado para bases de datos)
                    withContext(Dispatchers.IO) {
                        repository.agendarCita(nuevaCita)
                    }

                    // Si todo salió bien, avanzamos a la pantalla de éxito (CitaExitosa)
                    // pasándole exactamente los mismos datos para que el usuario pueda enviarlos por WhatsApp
                    val intent = Intent(this@RevisarCita, CitaExitosa::class.java)
                    intent.putExtra("usuarioId", usuarioId)
                    intent.putExtra("especialidad", especialidad)
                    intent.putExtra("doctor", doctor)
                    intent.putExtra("direccion", direccion)
                    intent.putExtra("fecha", fecha)
                    intent.putExtra("hora", hora)
                    intent.putExtra("fechaHoraMillis", fechaHoraMillis)
                    startActivity(intent)

                    // Cerramos esta pantalla para que el usuario no pueda darle "Atrás" y volver a agendarla
                    finish()
                } catch (e: Exception) {
                    // Si hubo algún error (ej. falta de memoria), avisamos y volvemos a encender el botón
                    Toast.makeText(this@RevisarCita, "No pudimos guardar tu cita. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                    btnConfirmar.isEnabled = true
                }
            }
        }

    }
}