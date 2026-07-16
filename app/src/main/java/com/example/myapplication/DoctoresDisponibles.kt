package com.example.myapplication

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.DoctorEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DoctoresDisponibles : AppCompatActivity() {

    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctores_disponibles)

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val tvEspecialidadElegida = findViewById<TextView>(R.id.tvEspecialidadElegida)
        val contenedorDoctores = findViewById<LinearLayout>(R.id.contenedorDoctores)
        val tvSinDoctores = findViewById<TextView>(R.id.tvSinDoctores)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // ---> Datos reales que llegan desde SpecialtiesActivity <---
        val especialidadId = intent.getIntExtra("especialidadId", -1)
        val especialidadNombre = intent.getStringExtra("especialidad") ?: "Médico General"
        val usuarioId = intent.getLongExtra("usuarioId", -1L)
        tvEspecialidadElegida.text = "Especialidad: $especialidadNombre"

        btnVolver.setOnClickListener { finish() }

        // ---> Cargar los doctores reales de esta especialidad desde Room <---
        lifecycleScope.launch {

            // 1. Movemos la consulta de Room al hilo de Entrada/Salida
            val doctores = withContext(Dispatchers.IO) {
                repository.obtenerDoctoresPorEspecialidad(especialidadId)
            }

            if (doctores.isEmpty()) {
                tvSinDoctores.visibility = View.VISIBLE
                return@launch
            }

            doctores.forEach { doctor ->
                agregarTarjetaDoctor(contenedorDoctores, doctor, especialidadNombre, usuarioId)
            }
        }

        // ==========================================
        // Navegación Inferior Consistente
        // ==========================================
        navInicio.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        navAgendar.setOnClickListener {
            finish() // Regresa al Paso 1 (Especialidades)
        }

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Infla item_doctor_card.xml y lo llena con los datos reales de un doctor
     */
    private fun agregarTarjetaDoctor(
        contenedor: LinearLayout,
        doctor: DoctorEntity,
        especialidad: String,
        usuarioId: Long
    ) {
        val tarjeta = LayoutInflater.from(this)
            .inflate(R.layout.item_doctor_card, contenedor, false) as MaterialCardView

        tarjeta.findViewById<TextView>(R.id.tvNombreDoctor).text = doctor.nombre
        tarjeta.findViewById<TextView>(R.id.tvDireccionDoctor).text = doctor.direccion
        tarjeta.findViewById<TextView>(R.id.tvDistanciaDoctor).text =
            String.format(Locale.getDefault(), "%.1f km de distancia", doctor.distanciaKm)
        tarjeta.contentDescription = "Seleccionar a ${doctor.nombre}"

        val btnMapa = tarjeta.findViewById<MaterialButton>(R.id.btnMapaDoctor)
        val btnElegir = tarjeta.findViewById<MaterialButton>(R.id.btnElegirDoctor)

        btnMapa.contentDescription = "Ver cómo llegar al consultorio de ${doctor.nombre}"

        // Clic para abrir el mapa enviando la dirección limpia
        btnMapa.setOnClickListener {
            abrirMapa(doctor.direccion)
        }

        // La tarjeta completa y el botón "Elegir Médico" hacen lo mismo: área de toque más grande
        val irAHorario = {
            confirmarDoctor(doctor, especialidad, usuarioId)
        }
        btnElegir.setOnClickListener { irAHorario() }
        tarjeta.setOnClickListener { irAHorario() }

        contenedor.addView(tarjeta)
    }

    /**
     * Abre Google Maps usando un enlace web estándar para evitar bloqueos de seguridad en Android 11+
     */
    private fun abrirMapa(direccion: String) {
        val mapaWebUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapaWebUri)

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            AlertDialog.Builder(this)
                .setTitle("Mapa no disponible")
                .setMessage("No pudimos abrir la ubicación de: $direccion")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    private fun confirmarDoctor(doctor: DoctorEntity, especialidad: String, usuarioId: Long) {
        val intent = Intent(this, Horario::class.java)
        intent.putExtra("usuarioId", usuarioId)
        intent.putExtra("doctorId", doctor.id)
        intent.putExtra("doctor", doctor.nombre)
        intent.putExtra("especialidad", especialidad)
        intent.putExtra("direccion", doctor.direccion)
        startActivity(intent)
    }

    private fun confirmarCierreDeSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}