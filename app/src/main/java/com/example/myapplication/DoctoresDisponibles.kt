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

/**
 * Pantalla correspondiente al "Paso 2" de agendar una cita.
 * Muestra una lista de los médicos que pertenecen a la especialidad que el usuario eligió antes.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "especialidadId" (Int): El ID de la especialidad para buscar en la base de datos.
 * - "especialidad" (String): El nombre de la especialidad (ej. "Cardiología") para mostrar en el título.
 * - "usuarioId" (Long): El ID del usuario actual para no perder su sesión.
 */
class DoctoresDisponibles : AppCompatActivity() {

    // Conexión con la base de datos y el repositorio para poder leer los doctores
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Lee qué especialidad eligió el usuario y busca en la base de datos los doctores
     * disponibles para mostrarlos en forma de tarjetas.
     *
     * @param savedInstanceState Estado guardado de la pantalla (por si se gira el celular).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctores_disponibles)

        // Enlazamos los elementos visuales de la pantalla (XML) con el código
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val tvEspecialidadElegida = findViewById<TextView>(R.id.tvEspecialidadElegida)
        val contenedorDoctores = findViewById<LinearLayout>(R.id.contenedorDoctores)
        val tvSinDoctores = findViewById<TextView>(R.id.tvSinDoctores)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // ---> 1. Recibir los datos enviados desde la pantalla anterior <---
        val especialidadId = intent.getIntExtra("especialidadId", -1)
        val especialidadNombre = intent.getStringExtra("especialidad") ?: "Médico General"
        val usuarioId = intent.getLongExtra("usuarioId", -1L)
        tvEspecialidadElegida.text = "Especialidad: $especialidadNombre"

        // Botón superior para regresar
        btnVolver.setOnClickListener { finish() }

        // ---> 2. Buscar los doctores en la base de datos <---
        // Usamos lifecycleScope para no congelar la pantalla mientras busca en la base de datos
        lifecycleScope.launch {
            val doctores = withContext(Dispatchers.IO) {
                repository.obtenerDoctoresPorEspecialidad(especialidadId)
            }

            // Si no hay doctores registrados para esta especialidad, mostramos un mensaje
            if (doctores.isEmpty()) {
                tvSinDoctores.visibility = View.VISIBLE
                return@launch
            }

            // Si encontró doctores, dibujamos una tarjeta por cada uno
            doctores.forEach { doctor ->
                agregarTarjetaDoctor(contenedorDoctores, doctor, especialidadNombre, usuarioId)
            }
        }

        // ==========================================
        // Botones de la barra inferior de navegación
        // ==========================================
        navInicio.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        navAgendar.setOnClickListener {
            finish() // Cierra esta pantalla y regresa a la lista de especialidades
        }

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Dibuja (infla) una tarjeta visual (XML) con los datos de un doctor y la pega
     * en la pantalla principal. También configura qué pasa cuando tocan sus botones.
     *
     * @param contenedor El espacio en blanco en la pantalla donde se pegará la tarjeta.
     * @param doctor Los datos del médico obtenidos de la base de datos.
     * @param especialidad El nombre de la especialidad (para pasarlo al siguiente paso).
     * @param usuarioId El ID del usuario actual.
     */
    private fun agregarTarjetaDoctor(
        contenedor: LinearLayout,
        doctor: DoctorEntity,
        especialidad: String,
        usuarioId: Long
    ) {
        // "Inflar" significa convertir el diseño XML (item_doctor_card) en un objeto visual que podemos usar
        val tarjeta = LayoutInflater.from(this)
            .inflate(R.layout.item_doctor_card, contenedor, false) as MaterialCardView

        // Llenamos los textos de la tarjeta con los datos del doctor
        tarjeta.findViewById<TextView>(R.id.tvNombreDoctor).text = doctor.nombre
        tarjeta.findViewById<TextView>(R.id.tvDireccionDoctor).text = doctor.direccion
        tarjeta.findViewById<TextView>(R.id.tvDistanciaDoctor).text =
            String.format(Locale.getDefault(), "%.1f km de distancia", doctor.distanciaKm)

        tarjeta.contentDescription = "Seleccionar a ${doctor.nombre}"

        val btnMapa = tarjeta.findViewById<MaterialButton>(R.id.btnMapaDoctor)
        val btnElegir = tarjeta.findViewById<MaterialButton>(R.id.btnElegirDoctor)
        btnMapa.contentDescription = "Ver cómo llegar al consultorio de ${doctor.nombre}"

        // Si tocan "Cómo llegar", abrimos Google Maps
        btnMapa.setOnClickListener {
            abrirMapa(doctor.direccion)
        }

        // Si tocan el botón "Elegir Médico" o la tarjeta en sí, avanzamos al siguiente paso
        val irAHorario = {
            confirmarDoctor(doctor, especialidad, usuarioId)
        }
        btnElegir.setOnClickListener { irAHorario() }
        tarjeta.setOnClickListener { irAHorario() }

        // Pegamos la tarjeta ya lista en la pantalla
        contenedor.addView(tarjeta)
    }

    /**
     * Abre la aplicación de Google Maps buscando la dirección del doctor.
     * Usa un enlace web seguro (https) para que funcione en cualquier celular Android.
     *
     * @param direccion La ubicación física del consultorio (ej. "Calle Flores #456").
     */
    private fun abrirMapa(direccion: String) {
        val mapaWebUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapaWebUri)

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Si el celular no tiene Maps ni navegador de internet, avisamos con un mensaje
            AlertDialog.Builder(this)
                .setTitle("Mapa no disponible")
                .setMessage("No pudimos abrir la ubicación de: $direccion")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    /**
     * Avanza a la pantalla "Paso 3" (Horario), enviándole toda la información del doctor elegido.
     *
     * @param doctor Objeto con los datos del médico seleccionado.
     * @param especialidad Nombre de la especialidad.
     * @param usuarioId ID del usuario actual.
     */
    private fun confirmarDoctor(doctor: DoctorEntity, especialidad: String, usuarioId: Long) {
        val intent = Intent(this, Horario::class.java)
        intent.putExtra("usuarioId", usuarioId)
        intent.putExtra("doctorId", doctor.id)
        intent.putExtra("doctor", doctor.nombre)
        intent.putExtra("especialidad", especialidad)
        intent.putExtra("direccion", doctor.direccion)

        startActivity(intent)
    }

    /**
     * Muestra una ventana de confirmación antes de cerrar la sesión.
     * Si el usuario acepta, borra el historial de navegación y lo devuelve a la pantalla de inicio (Login).
     */
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