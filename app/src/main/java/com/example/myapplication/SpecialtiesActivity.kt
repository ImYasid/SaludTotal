package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class SpecialtiesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val gridEspecialidades = findViewById<GridLayout>(R.id.gridEspecialidades)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnMicrofono = findViewById<MaterialCardView>(R.id.btnMicrofono)
        val tvSinResultados = findViewById<TextView>(R.id.tvSinResultados)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        btnVolver.setOnClickListener {
            finish()
        }

        // ---> Los 6 botones de especialidad ahora SÍ responden, con el mismo comportamiento <---
        // Antes, 4 de los 6 botones (Traumatología, Oftalmología, Neurología, Vacunación)
        // no hacían absolutamente nada al tocarlos: eso es muy confuso para cualquier usuario.
        val botonesEspecialidad = listOf(
            findViewById<MaterialButton>(R.id.btnMedicoGeneral) to "Médico General",
            findViewById<MaterialButton>(R.id.btnCardiologia) to "Cardiología",
            findViewById<MaterialButton>(R.id.btnTraumatologia) to "Traumatología",
            findViewById<MaterialButton>(R.id.btnOftalmologia) to "Oftalmología",
            findViewById<MaterialButton>(R.id.btnNeurologia) to "Neurología",
            findViewById<MaterialButton>(R.id.btnVacunacion) to "Vacunación"
        )

        botonesEspecialidad.forEach { (boton, nombreEspecialidad) ->
            boton.setOnClickListener {
                irADoctoresDisponibles(nombreEspecialidad)
            }
        }

        // ---> Búsqueda real: mientras escribe, se ocultan las especialidades que no coinciden <---
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val textoBusqueda = s.toString().trim().lowercase(Locale.getDefault())
                var hayResultados = false

                botonesEspecialidad.forEach { (boton, nombreEspecialidad) ->
                    val coincide = nombreEspecialidad.lowercase(Locale.getDefault()).contains(textoBusqueda)
                    boton.visibility = if (coincide) android.view.View.VISIBLE else android.view.View.GONE
                    if (coincide) hayResultados = true
                }

                tvSinResultados.visibility = if (!hayResultados) android.view.View.VISIBLE else android.view.View.GONE
                gridEspecialidades.visibility = if (!hayResultados) android.view.View.GONE else android.view.View.VISIBLE
            }
        })

        // El micrófono explica lo que hace en vez de quedar mudo al tocarlo
        btnMicrofono.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Búsqueda por voz")
                .setMessage("Esta función estará disponible pronto. Por ahora, puedes escribir el nombre de la especialidad en el cuadro de búsqueda.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // ---> Misma barra de navegación inferior que en Bienvenida <---
        navInicio.setOnClickListener {
            finish() // Regresa al Welcome
        }

        navAgendar.setOnClickListener { } // Ya estamos en este paso

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    private fun irADoctoresDisponibles(nombreEspecialidad: String) {
        val intent = Intent(this, DoctoresDisponibles::class.java)
        intent.putExtra("especialidad", nombreEspecialidad)
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