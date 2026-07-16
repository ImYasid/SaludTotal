package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DoctoresDisponibles : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctores_disponibles)

        // 1. Vincular componentes
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val tvEspecialidadElegida = findViewById<TextView>(R.id.tvEspecialidadElegida)

        // Tarjetas completas (ahora toda la tarjeta selecciona al médico, no solo el botón chico)
        val cardDoctor1 = findViewById<MaterialCardView>(R.id.cardDoctor1)
        val cardDoctor2 = findViewById<MaterialCardView>(R.id.cardDoctor2)

        // Botones Doctor 1
        val btnMapaDoctor1 = findViewById<MaterialButton>(R.id.btnMapaDoctor1)
        val btnElegirDoctor1 = findViewById<MaterialButton>(R.id.btnElegirDoctor1)

        // Botones Doctor 2
        val btnMapaDoctor2 = findViewById<MaterialButton>(R.id.btnMapaDoctor2)
        val btnElegirDoctor2 = findViewById<MaterialButton>(R.id.btnElegirDoctor2)

        // Navegación Inferior
        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // ==========================================
        // 2. Mostrar la especialidad que se eligió en el paso anterior
        // ==========================================
        // Antes esta pantalla no sabía qué especialidad había elegido la persona en el Paso 1;
        // ahora la recibe por Intent y la muestra arriba, para que no pierda el hilo.
        val especialidadElegida = intent.getStringExtra("especialidad") ?: "Médico General"
        tvEspecialidadElegida.text = "Especialidad: $especialidadElegida"

        // ==========================================
        // 3. Lógica de Interfaz y Mapas
        // ==========================================

        btnVolver.setOnClickListener {
            finish() // Regresa a la pantalla de Especialidades
        }

        // --- Intent Implícito para abrir Google Maps ---
        btnMapaDoctor1.setOnClickListener {
            abrirMapa("geo:0,0?q=Av.+Principal+123,+Centro")
        }

        btnMapaDoctor2.setOnClickListener {
            abrirMapa("geo:0,0?q=Calle+Flores+456,+Norte")
        }

        // --- Selección del Médico ---
        // La tarjeta COMPLETA y el botón "Elegir Médico" hacen lo mismo: así el área de toque
        // es mucho más grande y perdona un dedo menos preciso.
        btnElegirDoctor1.setOnClickListener {
            confirmarDoctor("Dr. Juan Pérez", especialidadElegida, "Av. Principal #123, Centro")
        }
        cardDoctor1.setOnClickListener {
            confirmarDoctor("Dr. Juan Pérez", especialidadElegida, "Av. Principal #123, Centro")
        }

        btnElegirDoctor2.setOnClickListener {
            confirmarDoctor("Dra. María González", especialidadElegida, "Calle Flores #456, Norte")
        }
        cardDoctor2.setOnClickListener {
            confirmarDoctor("Dra. María González", especialidadElegida, "Calle Flores #456, Norte")
        }

        // ==========================================
        // 4. Navegación Inferior Consistente
        // ==========================================
        navInicio.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // "Agendar" regresa al Paso 1 para elegir otra especialidad, ya que este flujo
        // sigue siendo parte de "Agendar Cita"
        navAgendar.setOnClickListener {
            finish()
        }

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    private fun abrirMapa(coordenadasUri: String) {
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(coordenadasUri))
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            AlertDialog.Builder(this)
                .setTitle("Aplicación no encontrada")
                .setMessage("No tienes instalada ninguna aplicación de mapas (como Google Maps) para ver la dirección.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    private fun confirmarDoctor(nombreDoctor: String, especialidad: String, direccion: String) {
        val intent = Intent(this, Horario::class.java)
        intent.putExtra("doctor", nombreDoctor)
        intent.putExtra("especialidad", especialidad)
        intent.putExtra("direccion", direccion)
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