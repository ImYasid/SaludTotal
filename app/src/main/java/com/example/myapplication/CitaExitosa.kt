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

/**
 * Pantalla final del flujo de agendamiento.
 * Muestra el resumen de la cita confirmada y ofrece opciones adicionales
 * como agregar la cita al calendario del dispositivo o compartirla por WhatsApp.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "especialidad" (String): Nombre de la especialidad.
 * - "doctor" (String): Nombre del doctor asignado.
 * - "direccion" (String): Ubicación del consultorio.
 * - "fecha" (String): Fecha formateada (ej. "Miércoles 15 de Octubre").
 * - "hora" (String): Hora formateada (ej. "09:00 AM").
 * - "fechaHoraMillis" (Long): Timestamp exacto para la creación del evento en el calendario.
 * - "usuarioId" (Long): Identificador del usuario actual para el retorno al inicio.
 */
class CitaExitosa : AppCompatActivity() {

    private var especialidad = ""
    private var doctor = ""
    private var direccion = ""
    private var fecha = ""
    private var hora = ""
    private var fechaHoraMillis = 0L

    /**
     * Inicializa la interfaz de usuario, recupera los datos de la cita desde el Intent,
     * configura la vista del resumen y asigna los listeners a los botones de acción.
     *
     * @param savedInstanceState Estado guardado de la actividad en caso de recreación.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cita_exitosa)

        // Configuración para el padding dinámico de las barras del sistema (Edge to Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperación de datos del Intent
        especialidad = intent.getStringExtra("especialidad") ?: ""
        doctor = intent.getStringExtra("doctor") ?: ""
        direccion = intent.getStringExtra("direccion") ?: "Consultorio principal"
        fecha = intent.getStringExtra("fecha") ?: ""
        hora = intent.getStringExtra("hora") ?: ""
        fechaHoraMillis = intent.getLongExtra("fechaHoraMillis", 0L)
        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        // Validación de datos requeridos para evitar mostrar un resumen vacío
        if (especialidad.isEmpty() || doctor.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "No encontramos los datos de tu cita.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Poblado de la interfaz con los datos reales
        findViewById<TextView>(R.id.tvEspecialidadValor).text = "$doctor - $especialidad"
        findViewById<TextView>(R.id.tvFechaHoraValor).text = "$fecha, $hora"
        findViewById<TextView>(R.id.tvUbicacionValor).text = direccion

        val btnCalendario = findViewById<MaterialButton>(R.id.btnCalendario)
        val btnWhatsApp = findViewById<MaterialButton>(R.id.btnWhatsApp)
        val btnInicio = findViewById<MaterialButton>(R.id.btnInicio)

        btnCalendario.setOnClickListener {
            abrirCalendario()
        }

        btnWhatsApp.setOnClickListener {
            abrirWhatsApp()
        }

        btnInicio.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            // Borra el historial de navegación para que el usuario no pueda "retroceder" a la cita
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /**
     * Construye y lanza un Intent para crear un nuevo evento en la aplicación de
     * calendario predeterminada del dispositivo.
     *
     * Rellena automáticamente los campos de título, ubicación, descripción,
     * fecha/hora de inicio y fecha/hora de fin (duración estimada de 30 minutos).
     * Incluye manejo de excepciones en caso de que el dispositivo no cuente con app de calendario.
     */
    private fun abrirCalendario() {
        val duracionEventoMs = 30 * 60 * 1000 // 30 minutos
        val horaInicio = if (fechaHoraMillis > 0) fechaHoraMillis else System.currentTimeMillis()
        val horaFin = horaInicio + duracionEventoMs

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "Cita Médica - $especialidad con $doctor")
            .putExtra(CalendarContract.Events.EVENT_LOCATION, direccion)
            .putExtra(CalendarContract.Events.DESCRIPTION, "Cita de $especialidad agendada vía SaludTotal")
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, horaInicio)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, horaFin)

        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No encontramos una app de calendario instalada.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Construye y lanza un Intent con un enlace web universal de WhatsApp (`https://api.whatsapp.com/send`)
     * para redactar un mensaje con los detalles de la cita.
     *
     * Al usar un URI genérico, el sistema operativo delega la acción a la app instalada
     * o, en su defecto, abre el navegador. Incluye manejo de excepciones.
     */
    private fun abrirWhatsApp() {
        val mensaje = "¡Hola! Te comparto los datos de mi cita médica:\n" +
                "$doctor - $especialidad\n" +
                "$fecha, $hora\n" +
                direccion

        val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(mensaje)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No pudimos abrir WhatsApp.", Toast.LENGTH_LONG).show()
        }
    }
}