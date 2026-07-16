package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeActivity : AppCompatActivity() {

    // Cambia este número por el real de la línea de atención del hospital
    private val telefonoAyuda = "18007258383"

    // 1. Instanciamos el repositorio para poder buscar al usuario
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // 2. PRIMERO enlazamos las vistas del XML (Ahora sí, la pantalla ya existe)
        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        val btnMisCitas = findViewById<MaterialButton>(R.id.btnMisCitas) // ✅ AQUÍ ESTÁ EN EL LUGAR CORRECTO
        val btnAyuda = findViewById<MaterialButton>(R.id.btnAyuda)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // 3. Capturamos el ID del paciente que viaja por el Intent
        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        // 4. LÓGICA INTELIGENTE: Buscamos el nombre real en Room
        if (usuarioId == -1L) {
            tvSaludo.text = "Hola, Administrador"
        } else {
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    repository.obtenerUsuarioPorId(usuarioId)
                }

                if (user != null) {
                    val primerNombre = user.fullName.trim().substringBefore(" ")
                    tvSaludo.text = "Hola, $primerNombre"
                }
            }
        }

        // 5. Configuración de los botones
        btnAgendarCita.setOnClickListener {
            irAAgendarCita(usuarioId)
        }

        btnMisCitas.setOnClickListener {
            val intent = Intent(this, MisCitasActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            startActivity(intent)
        }

        btnAyuda.setOnClickListener {
            llamarLineaDeAyuda()
        }

        // Navegación inferior
        navInicio.setOnClickListener { }

        navAgendar.setOnClickListener {
            irAAgendarCita(usuarioId)
        }

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    private fun irAAgendarCita(usuarioId: Long) {
        val intent = Intent(this, SpecialtiesActivity::class.java)
        intent.putExtra("usuarioId", usuarioId)
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

    private fun llamarLineaDeAyuda() {
        AlertDialog.Builder(this)
            .setTitle("Línea de ayuda")
            .setMessage("Vamos a abrir el marcador de tu teléfono con el número de SaludTotal ya escrito. Solo debes tocar el botón de llamar.")
            .setPositiveButton("Llamar") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefonoAyuda"))
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}