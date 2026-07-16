package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class RevisarCita : AppCompatActivity() {
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
        // Antes "Dr. Juan Pérez", "Martes 12 de Mayo" y "9:00 AM" estaban escritos directo
        // en el XML: sin importar qué eligiera la persona, esta pantalla siempre mostraba lo mismo.
        val especialidad = intent.getStringExtra("especialidad")
        val doctor = intent.getStringExtra("doctor")
        val direccion = intent.getStringExtra("direccion")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")

        // Si a esta pantalla se llega sin haber completado los pasos anteriores (por ejemplo,
        // por un error de navegación), avisamos y regresamos en vez de mostrar datos vacíos o falsos.
        if (especialidad == null || doctor == null || fecha == null || hora == null) {
            android.widget.Toast.makeText(
                this,
                "Faltan datos de la cita. Vuelve a intentarlo desde el inicio.",
                android.widget.Toast.LENGTH_LONG
            ).show()
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

        // Vincular los botones
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)
        val btnCorregir = findViewById<MaterialButton>(R.id.btnCorregir)

        // Volver atrás (flecha superior) y "No, quiero corregir" hacen lo mismo:
        // regresan al Paso 3 con la selección de día y hora tal como estaba.
        btnVolver.setOnClickListener { finish() }
        btnCorregir.setOnClickListener { finish() }

        // Confirmar: pasamos los mismos datos reales a la pantalla de éxito,
        // para que tampoco tenga que inventar información.
        btnConfirmar.setOnClickListener {
            val intent = Intent(this, CitaExitosa::class.java)
            intent.putExtra("especialidad", especialidad)
            intent.putExtra("doctor", doctor)
            intent.putExtra("direccion", direccion)
            intent.putExtra("fecha", fecha)
            intent.putExtra("hora", hora)
            startActivity(intent)
            finish() // Destruimos esta pantalla para que no se pueda "volver" a confirmar la misma cita
        }
    }
}