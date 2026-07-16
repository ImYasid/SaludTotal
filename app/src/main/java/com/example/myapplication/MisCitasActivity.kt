package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MisCitasActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var tvSinCitas: TextView
    private lateinit var adapter: CitasAdapter
    private var usuarioId: Long = -1L

    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        // Capturamos el ID del usuario que nos mandó WelcomeActivity
        usuarioId = intent.getLongExtra("usuarioId", -1L)

        // Enlazamos las vistas (Asegúrate de que estos IDs existan en tu activity_mis_citas.xml)
        rvCitas = findViewById(R.id.rvCitas)
        tvSinCitas = findViewById(R.id.tvSinCitas)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)

        btnVolver.setOnClickListener { finish() }

        // Configuramos el RecyclerView (la lista)
        rvCitas.layoutManager = LinearLayoutManager(this)
        adapter = CitasAdapter(emptyList()) { citaSeleccionada ->
            confirmarEliminacion(citaSeleccionada)
        }
        rvCitas.adapter = adapter

        // Cargamos los datos de Room
        cargarCitas()
    }

    private fun cargarCitas() {
        lifecycleScope.launch {
            val citas = withContext(Dispatchers.IO) {
                repository.obtenerCitasDelUsuario(usuarioId.toInt())
            }

            if (citas.isEmpty()) {
                rvCitas.visibility = View.GONE
                tvSinCitas.visibility = View.VISIBLE
            } else {
                rvCitas.visibility = View.VISIBLE
                tvSinCitas.visibility = View.GONE
                adapter.actualizarLista(citas)
            }
        }
    }

    private fun confirmarEliminacion(cita: AppointmentEntity) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que quieres cancelar tu cita de ${cita.especialidadNombre}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                eliminarCitaDeBD(cita)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarCitaDeBD(cita: AppointmentEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repository.eliminarCita(cita.id)
            }
            Toast.makeText(this@MisCitasActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
            cargarCitas() // Volvemos a cargar la lista para que desaparezca la tarjeta
        }
    }
}