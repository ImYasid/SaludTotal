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

/**
 * Controlador del catálogo de especialidades médicas de SaludTotal.
 * * Esta clase orquesta el flujo de selección de áreas clínicas (Paso 1 del agendamiento).
 * Implementa un motor de filtrado reactivo local en la interfaz de usuario para reducir la
 * carga cognitiva, garantizando consistencia en todos los disparadores e integrando controles
 * de navegación global accesibles.
 */
class SpecialtiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        // ==========================================
        // 1. VINCULACIÓN DE COMPONENTES DE INTERFAZ
        // ==========================================
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val gridEspecialidades = findViewById<GridLayout>(R.id.gridEspecialidades)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnMicrofono = findViewById<MaterialCardView>(R.id.btnMicrofono)
        val tvSinResultados = findViewById<TextView>(R.id.tvSinResultados)

        // Componentes estructurales de la barra de navegación (Navbar)
        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // Retorno simple al stack previo (WelcomeActivity)
        btnVolver.setOnClickListener {
            finish()
        }

        // ==========================================
        // 2. MAESTRO DE COMPONENTES E INTERACCIÓN UNIFICADA
        // ==========================================
        // Mapeo estructurado mediante pares (Pair) para centralizar y unificar los eventos
        // de clic. Esto erradica el comportamiento inconsistente de botones "mudos" o inactivos.
        val botonesEspecialidad = listOf(
            findViewById<MaterialButton>(R.id.btnMedicoGeneral) to "Médico General",
            findViewById<MaterialButton>(R.id.btnCardiologia) to "Cardiología",
            findViewById<MaterialButton>(R.id.btnTraumatologia) to "Traumatología",
            findViewById<MaterialButton>(R.id.btnOftalmologia) to "Oftalmología",
            findViewById<MaterialButton>(R.id.btnNeurologia) to "Neurología",
            findViewById<MaterialButton>(R.id.btnVacunacion) to "Vacunación"
        )

        // Iteración funcional: Asigna de forma masiva el comportamiento interactivo
        botonesEspecialidad.forEach { (boton, nombreEspecialidad) ->
            boton.setOnClickListener {
                confirmarEspecialidad(nombreEspecialidad)
            }
        }

        // ==========================================
        // 3. MOTOR DE FILTRADO REACTIVO EN TIEMPO REAL
        // ==========================================
        // Escuchador dinámico de entrada tipográfica que evalúa el texto carácter por carácter
        // para mutar la visibilidad de los nodos gráficos sin requerir recargas de la actividad.
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Sanitización y normalización de la cadena de entrada (Región / Idioma independiente)
                val textoBusqueda = s.toString().trim().lowercase(Locale.getDefault())
                var hayResultados = false

                // Evaluación de coincidencia de subcadenas sobre la colección indexada
                botonesEspecialidad.forEach { (boton, nombreEspecialidad) ->
                    val coincide = nombreEspecialidad.lowercase(Locale.getDefault()).contains(textoBusqueda)

                    // Mutación de visibilidad en base al resultado booleano
                    boton.visibility = if (coincide) android.view.View.VISIBLE else android.view.View.GONE
                    if (coincide) hayResultados = true
                }

                // Orquestación de pantallas alternas en caso de búsquedas fallidas o vacías
                tvSinResultados.visibility = if (!hayResultados) android.view.View.VISIBLE else android.view.View.GONE
                gridEspecialidades.visibility = if (!hayResultados) android.view.View.GONE else android.view.View.VISIBLE
            }
        })

        // Canal de contingencia UX: Mitiga la frustración informando de manera explícita
        // el estado de desarrollo de la función de dictado por voz.
        btnMicrofono.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Búsqueda por voz")
                .setMessage("Esta función estará disponible pronto. Por ahora, puedes escribir el nombre de la especialidad en el cuadro de búsqueda.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // ==========================================
        // 4. CONTROLADORES DE LA BARRA DE NAVEGACIÓN
        // ==========================================
        navInicio.setOnClickListener {
            finish() // Destruye la actividad actual para revelar el Inicio (WelcomeActivity) que reside abajo
        }

        navAgendar.setOnClickListener { } // Estado inerte deliberado: Evita redundancia cíclica

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Despliega un cuadro de diálogo informativo que valida la selección exitosa del área médica.
     * Actúa como un marcador de posición (placeholder) interactivo para simular el avance del flujo.
     *
     * @param nombreEspecialidad El identificador textual de la especialidad médica seleccionada.
     */
    private fun confirmarEspecialidad(nombreEspecialidad: String) {
        AlertDialog.Builder(this)
            .setTitle("Especialidad seleccionada")
            .setMessage("Elegiste: $nombreEspecialidad.\n\nEl siguiente paso (elegir fecha y doctor) estará disponible pronto.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * Lanza una advertencia imperativa de confirmación antes de la destrucción de las sesiones locales.
     * Si la acción es afirmativa, reestructura el árbol de navegación (Task Stack) para aislar el Login.
     */
    private fun confirmarCierreDeSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                // Flags de seguridad: Sanean por completo el historial de actividades de la máquina virtual
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}