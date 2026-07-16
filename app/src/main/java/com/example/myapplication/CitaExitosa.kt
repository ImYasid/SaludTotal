package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class CitaExitosa : AppCompatActivity() {

    // Variables de instancia: las necesitamos también dentro de abrirCalendario() y abrirWhatsApp(),
    // no solo en onCreate()
    private var especialidad = ""
    private var doctor = ""
    private var direccion = ""
    private var fecha = ""
    private var hora = ""
    private var fechaHoraMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cita_exitosa)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ---> Datos reales que llegan desde RevisarCita <---
        // Antes "Dr. Juan Pérez", "Martes 12 de Mayo, 9:00 AM" y "Av. Principal #123"
        // estaban escritos directo en el XML, sin importar qué hubiera elegido la persona.
        especialidad = intent.getStringExtra("especialidad") ?: ""
        doctor = intent.getStringExtra("doctor") ?: ""
        direccion = intent.getStringExtra("direccion") ?: "Consultorio principal"
        fecha = intent.getStringExtra("fecha") ?: ""
        hora = intent.getStringExtra("hora") ?: ""
        fechaHoraMillis = intent.getLongExtra("fechaHoraMillis", 0L)
        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        // Si por algún error de navegación se llega aquí sin datos, avisamos y regresamos
        // en vez de mostrar un resumen vacío o inventado.
        if (especialidad.isEmpty() || doctor.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "No encontramos los datos de tu cita.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Llenar el resumen con los datos reales
        findViewById<TextView>(R.id.tvEspecialidadValor).text = "$doctor - $especialidad"
        findViewById<TextView>(R.id.tvFechaHoraValor).text = "$fecha, $hora"
        findViewById<TextView>(R.id.tvUbicacionValor).text = direccion

        val btnCalendario = findViewById<MaterialButton>(R.id.btnCalendario)
        val btnWhatsApp = findViewById<MaterialButton>(R.id.btnWhatsApp)
        val btnInicio = findViewById<MaterialButton>(R.id.btnInicio)

        // Acción: Abrir app de Calendario nativa
        btnCalendario.setOnClickListener {
            abrirCalendario()
        }
        // Acción: Enviar un WhatsApp al propio usuario con los datos
        btnWhatsApp.setOnClickListener {
            abrirWhatsApp()
        }
        // Acción: Volver al Dashboard borrando el historial de pantallas
        btnInicio.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /**
     * Usa un Intent implícito para abrir el calendario de Google (o el predeterminado)
     * e inserta un nuevo evento con la especialidad, el doctor y el lugar reales.
     * Ahora también incluye la hora exacta (fechaHoraMillis), que antes no se enviaba:
     * sin eso, el calendario podía crear el evento sin horario definido.
     */
    private fun abrirCalendario() {
        val duracionEventoMs = 30 * 60 * 1000 // 30 minutos de duración estimada
        val horaInicio = if (fechaHoraMillis > 0) fechaHoraMillis else System.currentTimeMillis()
        val horaFin = horaInicio + duracionEventoMs

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "Cita Médica - $especialidad con $doctor")
            .putExtra(CalendarContract.Events.EVENT_LOCATION, direccion)
            .putExtra(CalendarContract.Events.DESCRIPTION, "Cita de $especialidad agendada vía SaludTotal")
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, horaInicio)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, horaFin)

        // Evita que la app crashee si el celular no tiene calendario
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No encontramos una app de calendario instalada.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Usa un URI especial de WhatsApp (wa.me) para abrir un chat con los datos reales de la cita.
     * Si no se pone número telefónico, WhatsApp abre la lista de contactos para que
     * el usuario elija a quién enviarlo (por ejemplo, a un hijo o a sí mismo).
     */
    private fun abrirWhatsApp() {
        val mensaje = "¡Hola! Te comparto los datos de mi cita médica:\n" +
                "$doctor - $especialidad\n" +
                "$fecha, $hora\n" +
                direccion
        val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(mensaje)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No encontramos WhatsApp instalado en este teléfono.", Toast.LENGTH_LONG).show()
        }
    }
}