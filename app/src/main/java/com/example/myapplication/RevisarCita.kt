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

class RevisarCita : AppCompatActivity() {

    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_revisar_cita)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ---> Datos reales que llegan desde el Paso 3 (Horario) <---
        val especialidad = intent.getStringExtra("especialidad")
        val doctor = intent.getStringExtra("doctor")
        val direccion = intent.getStringExtra("direccion")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")
        val fechaHoraMillis = intent.getLongExtra("fechaHoraMillis", 0L)
        val usuarioId = intent.getLongExtra("usuarioId", -1L)
        val doctorId = intent.getIntExtra("doctorId", -1)

        // Si a esta pantalla se llega sin haber completado los pasos anteriores, avisamos
        // y regresamos en vez de mostrar datos vacíos o guardar una cita incompleta.
        if (especialidad == null || doctor == null || fecha == null || hora == null || usuarioId == -1L || doctorId == -1) {
            Toast.makeText(this, "Faltan datos de la cita. Vuelve a intentarlo desde el inicio.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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

        btnVolver.setOnClickListener { finish() }
        btnCorregir.setOnClickListener { finish() }

        // ---> Confirmar: ahora sí se guarda la cita en la base de datos real <---
        btnConfirmar.setOnClickListener {
            btnConfirmar.isEnabled = false // evita doble toque mientras se guarda

            val nuevaCita = AppointmentEntity(
                usuarioId = usuarioId,
                doctorId = doctorId,
                especialidadNombre = especialidad,
                direccion = direccion ?: "Consultorio principal",
                fechaTexto = fecha,
                horaTexto = hora,
                fechaHoraMillis = fechaHoraMillis
            )

            lifecycleScope.launch {
                try {
                    // ✅ LA REGLA DE ORO APLICADA AQUÍ
                    withContext(Dispatchers.IO) {
                        repository.agendarCita(nuevaCita)
                    }

                    // Pasamos los mismos datos reales a la pantalla de éxito
                    val intent = Intent(this@RevisarCita, CitaExitosa::class.java)
                    intent.putExtra("usuarioId", usuarioId)
                    intent.putExtra("especialidad", especialidad)
                    intent.putExtra("doctor", doctor)
                    intent.putExtra("direccion", direccion)
                    intent.putExtra("fecha", fecha)
                    intent.putExtra("hora", hora)
                    intent.putExtra("fechaHoraMillis", fechaHoraMillis)
                    startActivity(intent)
                    finish() // Para que no se pueda "volver" y confirmar la misma cita otra vez
                } catch (e: Exception) {
                    Toast.makeText(this@RevisarCita, "No pudimos guardar tu cita. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                    btnConfirmar.isEnabled = true
                }
            }
        }
    }
}