package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Plantilla de datos que representa un solo día de atención médica.
 *
 * @param fecha La fecha exacta de este día en el calendario.
 * @param horasManana Lista de horas disponibles en la mañana (ej. "9:00 AM").
 * @param horasTarde Lista de horas disponibles en la tarde. Puede estar vacía los sábados.
 */
data class DiaDisponible(
    val fecha: Calendar,
    val horasManana: List<String>,
    val horasTarde: List<String>
)

/**
 * Pantalla correspondiente al "Paso 3" de agendar una cita (Selección de fecha y hora).
 * Aquí el usuario ve los próximos días de atención reales (saltando domingos)
 * y elige en qué horario quiere ser atendido.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "especialidad" (String): La especialidad elegida (ej. "Cardiología").
 * - "doctor" (String): El nombre del doctor elegido.
 * - "direccion" (String): La dirección del consultorio.
 * - "usuarioId" (Long): El ID del usuario que está usando la app.
 * - "doctorId" (Int): El ID del doctor seleccionado.
 */
class Horario : AppCompatActivity() {

    // Guardamos qué botones (día y hora) ha seleccionado el usuario
    private var diaSeleccionadoBoton: MaterialButton? = null
    private var horaSeleccionadaBoton: MaterialButton? = null

    // Guardamos los datos exactos del día y la hora que eligió
    private var diaSeleccionadoDatos: DiaDisponible? = null
    private var horaSeleccionadaTexto: String? = null

    // Lista temporal para poder "apagar" todos los botones de los días cuando se elige uno nuevo
    private val botonesDias = mutableListOf<MaterialButton>()

    /**
     * Prepara la pantalla al abrirse.
     * Calcula los próximos días reales de atención, dibuja los botones y configura
     * qué pasa cuando el usuario elige una fecha y una hora.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horario)

        // Configuración para que el diseño no se superponga con la barra de estado del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val btnConfirmarCita = findViewById<MaterialButton>(R.id.btnConfirmarCita)
        val tvResumenCita = findViewById<TextView>(R.id.tvResumenCita)
        val gridDias = findViewById<GridLayout>(R.id.gridDias)

        // ---> 1. Recibimos y mostramos los datos del paso anterior <---
        val especialidad = intent.getStringExtra("especialidad") ?: "Consulta"
        val doctor = intent.getStringExtra("doctor") ?: "tu médico"
        val direccion = intent.getStringExtra("direccion") ?: "Consultorio principal"
        val usuarioId = intent.getLongExtra("usuarioId", -1L)
        val doctorId = intent.getIntExtra("doctorId", -1)

        tvResumenCita.text = "$especialidad · $doctor"

        // ---> 2. Calculamos y dibujamos los próximos días hábiles <---
        val diasDisponibles = generarProximosDiasDisponibles(cantidadDias = 4)

        diasDisponibles.forEach { dia ->
            // Creamos un botón visual por cada día disponible
            val boton = crearBotonDia(dia)
            botonesDias.add(boton)
            gridDias.addView(boton)

            // ¿Qué pasa al tocar un día?
            boton.setOnClickListener {
                resetearBotones(botonesDias) // Despinta los otros días
                seleccionarBoton(boton)      // Pinta este día de azul

                diaSeleccionadoBoton = boton
                diaSeleccionadoDatos = dia

                mostrarHorasDelDia(dia) // Muestra las horas de este día específico

                // Borra la hora que se haya elegido antes, para obligarlo a elegir de nuevo
                horaSeleccionadaBoton = null
                horaSeleccionadaTexto = null
                verificarBotonConfirmar(btnConfirmarCita)
            }
        }

        btnVolver.setOnClickListener { finish() }

        // ---> 3. Avanzar a la pantalla final de Confirmación <---
        btnConfirmarCita.setOnClickListener {
            val diaElegido = diaSeleccionadoDatos
            val horaTexto = horaSeleccionadaTexto ?: ""
            val fechaTexto = diaElegido?.let { formatearFechaCompleta(it.fecha) } ?: ""

            // Calculamos el milisegundo exacto para poder agendarlo en el calendario del celular
            val fechaHoraMillis = if (diaElegido != null) calcularFechaHoraMillis(diaElegido.fecha, horaTexto) else System.currentTimeMillis()

            val intent = Intent(this, RevisarCita::class.java)
            intent.putExtra("usuarioId", usuarioId)
            intent.putExtra("doctorId", doctorId)
            intent.putExtra("especialidad", especialidad)
            intent.putExtra("doctor", doctor)
            intent.putExtra("direccion", direccion)
            intent.putExtra("fecha", fechaTexto)
            intent.putExtra("hora", horaTexto)
            intent.putExtra("fechaHoraMillis", fechaHoraMillis)
            startActivity(intent)
        }

        // ==========================================
        // Barra inferior de navegación
        // ==========================================
        findViewById<LinearLayout>(R.id.navInicio).setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navAgendar).setOnClickListener {
            finish() // Cierra y regresa a la pantalla anterior
        }

        findViewById<LinearLayout>(R.id.navSalir).setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Calcula una lista con los próximos días hábiles en los que el doctor atiende.
     * Empieza a buscar desde mañana y se salta automáticamente los domingos.
     *
     * @param cantidadDias Cuántos días hábiles queremos mostrarle al usuario (ej. 4 días).
     * @return Una lista de objetos [DiaDisponible] con sus fechas y horas.
     */
    private fun generarProximosDiasDisponibles(cantidadDias: Int): List<DiaDisponible> {
        val horasMananaEstandar = listOf("8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM")
        val horasTardeEstandar = listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")

        val dias = mutableListOf<DiaDisponible>()
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, 1) // Sumamos 1 para empezar desde el día de mañana

        // Mientras no tengamos la cantidad de días que pedimos, seguimos buscando
        while (dias.size < cantidadDias) {
            val esDomingo = calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            val esSabado = calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY

            if (!esDomingo) {
                val fechaDelDia = calendario.clone() as Calendar
                dias.add(
                    DiaDisponible(
                        fecha = fechaDelDia,
                        horasManana = horasMananaEstandar,
                        // Si es sábado, devolvemos una lista vacía para la tarde
                        horasTarde = if (esSabado) emptyList() else horasTardeEstandar
                    )
                )
            }
            calendario.add(Calendar.DAY_OF_MONTH, 1) // Pasamos al siguiente día
        }
        return dias
    }

    /**
     * Dibuja (crea mediante código) un botón visual para elegir un día.
     *
     * @param dia El objeto de día para sacar la fecha y ponerla como texto.
     * @return El botón (MaterialButton) ya configurado con bordes, color y texto.
     */
    private fun crearBotonDia(dia: DiaDisponible): MaterialButton {
        val boton = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)

        // Configuramos tamaño y márgenes
        boton.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = dpAPx(70)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dpAPx(4), dpAPx(4), dpAPx(4), dpAPx(4))
        }

        boton.text = formatearBotonDia(dia.fecha)
        boton.isAllCaps = false
        boton.cornerRadius = dpAPx(12)
        boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
        boton.setTextColor(Color.parseColor("#6B7480"))
        boton.contentDescription = "Elegir el día ${formatearFechaCompleta(dia.fecha)}"

        return boton
    }

    /**
     * Borra las horas que estuvieran mostrándose y dibuja los nuevos botones de
     * la mañana y de la tarde según el día que eligió el usuario.
     *
     * @param dia El día que acaba de seleccionar el usuario.
     */
    private fun mostrarHorasDelDia(dia: DiaDisponible) {
        val tvElijeDiaPrimero = findViewById<TextView>(R.id.tvElijeDiaPrimero)
        val contenedorHoras = findViewById<LinearLayout>(R.id.contenedorHoras)
        val gridManana = findViewById<GridLayout>(R.id.gridManana)
        val gridTarde = findViewById<GridLayout>(R.id.gridTarde)
        val tvTituloTarde = findViewById<TextView>(R.id.tvTituloTarde)
        val tvSinHorasTarde = findViewById<TextView>(R.id.tvSinHorasTarde)

        // Ocultamos el mensaje de "Elige un día primero" y mostramos el panel de horas
        tvElijeDiaPrimero.visibility = android.view.View.GONE
        contenedorHoras.visibility = android.view.View.VISIBLE

        gridManana.removeAllViews()
        gridTarde.removeAllViews()

        val botonesHoras = mutableListOf<MaterialButton>()

        // Dibujar botones de la mañana
        dia.horasManana.forEach { hora ->
            val boton = crearBotonHora(hora, botonesHoras)
            gridManana.addView(boton)
        }

        // Si es sábado (u otro día sin tarde), ocultamos el panel de la tarde y mostramos un mensaje
        if (dia.horasTarde.isEmpty()) {
            gridTarde.visibility = android.view.View.GONE
            tvTituloTarde.visibility = android.view.View.GONE
            tvSinHorasTarde.visibility = android.view.View.VISIBLE
        } else {
            // Si sí hay horas en la tarde, dibujamos sus botones
            gridTarde.visibility = android.view.View.VISIBLE
            tvTituloTarde.visibility = android.view.View.VISIBLE
            tvSinHorasTarde.visibility = android.view.View.GONE
            dia.horasTarde.forEach { hora ->
                val boton = crearBotonHora(hora, botonesHoras)
                gridTarde.addView(boton)
            }
        }
    }

    /**
     * Dibuja (crea mediante código) un botón visual para una hora específica.
     * También configura la lógica para que al tocarlo, se pinte de azul y despinte los demás.
     *
     * @param hora El texto de la hora (ej. "9:00 AM").
     * @param botonesHermanos La lista de todas las horas, usada para despintarlas.
     * @return El botón ya configurado.
     */
    private fun crearBotonHora(hora: String, botonesHermanos: MutableList<MaterialButton>): MaterialButton {
        val boton = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)

        boton.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = dpAPx(60)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dpAPx(4), dpAPx(4), dpAPx(4), dpAPx(4))
        }

        boton.text = hora
        boton.isAllCaps = false
        boton.textSize = 16f
        boton.cornerRadius = dpAPx(12)
        boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
        boton.setTextColor(Color.parseColor("#6B7480"))
        boton.contentDescription = "Elegir la hora $hora"

        // Guardamos el botón en la lista de hermanos
        botonesHermanos.add(boton)

        boton.setOnClickListener {
            resetearBotones(botonesHermanos)
            seleccionarBoton(boton)

            horaSeleccionadaBoton = boton
            horaSeleccionadaTexto = hora

            // Evaluamos si ya podemos habilitar el botón grande de "Continuar"
            verificarBotonConfirmar(findViewById(R.id.btnConfirmarCita))
        }
        return boton
    }

    /**
     * Devuelve los botones a su estilo original (bordes grises y sin fondo azul).
     */
    private fun resetearBotones(botones: List<MaterialButton>) {
        botones.forEach { boton ->
            boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
            boton.setTextColor(Color.parseColor("#6B7480"))
            boton.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    /**
     * Pinta un botón de azul claro (fondo y letras) para indicar que está seleccionado.
     */
    private fun seleccionarBoton(boton: MaterialButton) {
        boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#2C6BFE"))
        boton.setTextColor(Color.parseColor("#2C6BFE"))
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAF0FF"))
    }

    /**
     * Habilita el botón de continuar únicamente si el usuario ya escogió un día Y una hora.
     */
    private fun verificarBotonConfirmar(btnConfirmarCita: MaterialButton) {
        btnConfirmarCita.isEnabled = (diaSeleccionadoDatos != null && horaSeleccionadaTexto != null)
    }

    /**
     * Convierte una fecha de calendario a texto con formato de 2 líneas.
     * Ejemplo: "Lunes\n15 de Mayo"
     */
    private fun formatearBotonDia(fecha: Calendar): String {
        val formatoDia = SimpleDateFormat("EEEE", Locale("es", "ES"))
        val nombreDia = formatoDia.format(fecha.time).replaceFirstChar { it.uppercase() }
        val diaDelMes = fecha.get(Calendar.DAY_OF_MONTH)
        val formatoMes = SimpleDateFormat("MMMM", Locale("es", "ES"))
        val nombreMes = formatoMes.format(fecha.time).replaceFirstChar { it.uppercase() }
        return "$nombreDia\n$diaDelMes de $nombreMes"
    }

    /**
     * Convierte una fecha de calendario a texto de una sola línea.
     * Ejemplo: "Lunes 15 de Mayo"
     */
    private fun formatearFechaCompleta(fecha: Calendar): String {
        return formatearBotonDia(fecha).replace("\n", " ")
    }

    /**
     * Mezcla la fecha elegida con la hora elegida y lo convierte en "Milisegundos".
     * Este número es lo que necesitan las aplicaciones de calendario (como Google Calendar)
     * para saber exactamente a qué segundo agendar el evento.
     *
     * @param dia La fecha (ej. 15 de Mayo).
     * @param horaTexto La hora en texto (ej. "9:00 AM").
     * @return El tiempo exacto representado en milisegundos.
     */
    private fun calcularFechaHoraMillis(dia: Calendar, horaTexto: String): Long {
        return try {
            val formatoHora = SimpleDateFormat("h:mm a", Locale.US)
            val horaParseada = formatoHora.parse(horaTexto)
            val calendarioHora = Calendar.getInstance()
            calendarioHora.time = horaParseada!!

            val resultado = dia.clone() as Calendar
            resultado.set(Calendar.HOUR_OF_DAY, calendarioHora.get(Calendar.HOUR_OF_DAY))
            resultado.set(Calendar.MINUTE, calendarioHora.get(Calendar.MINUTE))
            resultado.set(Calendar.SECOND, 0)
            resultado.set(Calendar.MILLISECOND, 0)

            resultado.timeInMillis
        } catch (e: Exception) {
            dia.timeInMillis
        }
    }

    /**
     * Convierte medidas de DP (Android) a Píxeles reales de la pantalla.
     * Necesario al crear vistas mediante código (como los botones de días).
     */
    private fun dpAPx(dp: Int): Int {
        val densidad = resources.displayMetrics.density
        return (dp * densidad).toInt()
    }

    /**
     * Muestra una advertencia antes de cerrar la sesión del usuario.
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