package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {

    // Cambia este número por el real de la línea de atención del hospital
    private val telefonoAyuda = "18007258383"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        val btnAyuda = findViewById<MaterialButton>(R.id.btnAyuda)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)

        // Navegación inferior con texto visible, en vez del menú ☰ que antes estaba escondido
        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
        val nombreCompleto = sharedPreferences.getString("nombreGuardado", "Administrador") ?: "Administrador"
        val primerNombre = nombreCompleto.trim().substringBefore(" ")

        tvSaludo.text = "Hola, $primerNombre"

        btnAgendarCita.setOnClickListener {
            irAAgendarCita()
        }

        // El botón de ayuda ahora sí hace algo: abre el marcador con el número ya escrito,
        // para que la persona solo tenga que tocar "llamar".
        btnAyuda.setOnClickListener {
            llamarLineaDeAyuda()
        }

        // Ya estamos en Inicio, así que solo confirma visualmente (no navega a ningún lado)
        navInicio.setOnClickListener { }

        navAgendar.setOnClickListener {
            irAAgendarCita()
        }

        // Cerrar sesión ahora pide confirmación: un toque accidental ya no saca a la persona
        // de golpe sin que lo haya decidido.
        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    private fun irAAgendarCita() {
        val intent = Intent(this, SpecialtiesActivity::class.java)
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