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

// Representa un día real del calendario con sus horas disponibles.
// Las horas de la tarde pueden venir vacías (ej. los sábados el consultorio cierra temprano),
// así que esto ya no es un bloque de texto fijo: cambia según el día.
data class DiaDisponible(
    val fecha: Calendar,
    val horasManana: List<String>,
    val horasTarde: List<String>
)

class Horario : AppCompatActivity() {

    private var diaSeleccionadoBoton: MaterialButton? = null
    private var horaSeleccionadaBoton: MaterialButton? = null
    private var diaSeleccionadoDatos: DiaDisponible? = null
    private var horaSeleccionadaTexto: String? = null

    private val botonesDias = mutableListOf<MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horario)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val btnConfirmarCita = findViewById<MaterialButton>(R.id.btnConfirmarCita)
        val tvResumenCita = findViewById<TextView>(R.id.tvResumenCita)
        val gridDias = findViewById<GridLayout>(R.id.gridDias)

        // ---> Mostrar con quién y para qué especialidad es la cita (llega del paso anterior) <---
        val especialidad = intent.getStringExtra("especialidad") ?: "Consulta"
        val doctor = intent.getStringExtra("doctor") ?: "tu médico"
        val direccion = intent.getStringExtra("direccion") ?: "Consultorio principal"
        tvResumenCita.text = "$especialidad · $doctor"

        // ---> Generar los próximos días disponibles DE VERDAD, desde la fecha actual <---
        // Antes esto eran 4 botones con "Lunes 11 de Mayo" escrito a mano en el XML.
        val diasDisponibles = generarProximosDiasDisponibles(cantidadDias = 4)
        diasDisponibles.forEach { dia ->
            val boton = crearBotonDia(dia)
            botonesDias.add(boton)
            gridDias.addView(boton)

            boton.setOnClickListener {
                resetearBotones(botonesDias)
                seleccionarBoton(boton)
                diaSeleccionadoBoton = boton
                diaSeleccionadoDatos = dia
                mostrarHorasDelDia(dia)
                horaSeleccionadaBoton = null
                horaSeleccionadaTexto = null
                verificarBotonConfirmar(btnConfirmarCita)
            }
        }

        btnVolver.setOnClickListener { finish() }

        btnConfirmarCita.setOnClickListener {
            val fechaTexto = diaSeleccionadoDatos?.let { formatearFechaCompleta(it.fecha) } ?: ""
            val horaTexto = horaSeleccionadaTexto ?: ""

            val intent = Intent(this, RevisarCita::class.java)
            intent.putExtra("especialidad", especialidad)
            intent.putExtra("doctor", doctor)
            intent.putExtra("direccion", direccion)
            intent.putExtra("fecha", fechaTexto)
            intent.putExtra("hora", horaTexto)
            startActivity(intent)
        }

        // Navegación Inferior (antes solo "Inicio" estaba conectado)
        findViewById<LinearLayout>(R.id.navInicio).setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navAgendar).setOnClickListener {
            finish() // Regresa a elegir médico/especialidad
        }

        findViewById<LinearLayout>(R.id.navSalir).setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Calcula los próximos N días de atención a partir de mañana, saltándose los domingos
     * (el consultorio no atiende ese día). Cada día trae sus propias horas: los sábados,
     * por ejemplo, solo hay horario en la mañana.
     */
    private fun generarProximosDiasDisponibles(cantidadDias: Int): List<DiaDisponible> {
        val horasMananaEstandar = listOf("8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM")
        val horasTardeEstandar = listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")

        val dias = mutableListOf<DiaDisponible>()
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, 1) // Empezamos desde mañana, no desde hoy

        while (dias.size < cantidadDias) {
            val esDomingo = calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            val esSabado = calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY

            if (!esDomingo) {
                val fechaDelDia = calendario.clone() as Calendar
                dias.add(
                    DiaDisponible(
                        fecha = fechaDelDia,
                        horasManana = horasMananaEstandar,
                        horasTarde = if (esSabado) emptyList() else horasTardeEstandar
                    )
                )
            }
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dias
    }

    private fun crearBotonDia(dia: DiaDisponible): MaterialButton {
        val boton = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
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

    private fun mostrarHorasDelDia(dia: DiaDisponible) {
        val tvElijeDiaPrimero = findViewById<TextView>(R.id.tvElijeDiaPrimero)
        val contenedorHoras = findViewById<LinearLayout>(R.id.contenedorHoras)
        val gridManana = findViewById<GridLayout>(R.id.gridManana)
        val gridTarde = findViewById<GridLayout>(R.id.gridTarde)
        val tvTituloTarde = findViewById<TextView>(R.id.tvTituloTarde)
        val tvSinHorasTarde = findViewById<TextView>(R.id.tvSinHorasTarde)

        tvElijeDiaPrimero.visibility = android.view.View.GONE
        contenedorHoras.visibility = android.view.View.VISIBLE

        gridManana.removeAllViews()
        gridTarde.removeAllViews()

        val botonesHoras = mutableListOf<MaterialButton>()

        dia.horasManana.forEach { hora ->
            val boton = crearBotonHora(hora, botonesHoras)
            gridManana.addView(boton)
        }

        if (dia.horasTarde.isEmpty()) {
            gridTarde.visibility = android.view.View.GONE
            tvTituloTarde.visibility = android.view.View.GONE
            tvSinHorasTarde.visibility = android.view.View.VISIBLE
        } else {
            gridTarde.visibility = android.view.View.VISIBLE
            tvTituloTarde.visibility = android.view.View.VISIBLE
            tvSinHorasTarde.visibility = android.view.View.GONE
            dia.horasTarde.forEach { hora ->
                val boton = crearBotonHora(hora, botonesHoras)
                gridTarde.addView(boton)
            }
        }
    }

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
        botonesHermanos.add(boton)

        boton.setOnClickListener {
            resetearBotones(botonesHermanos)
            seleccionarBoton(boton)
            horaSeleccionadaBoton = boton
            horaSeleccionadaTexto = hora
            verificarBotonConfirmar(findViewById(R.id.btnConfirmarCita))
        }
        return boton
    }

    // --- Funciones Auxiliares para el diseño UI/UX ---

    private fun resetearBotones(botones: List<MaterialButton>) {
        botones.forEach { boton ->
            boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
            boton.setTextColor(Color.parseColor("#6B7480"))
            // Usamos backgroundTintList en vez de setBackgroundColor: así conservamos
            // las esquinas redondeadas del botón (setBackgroundColor las borraba).
            boton.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    private fun seleccionarBoton(boton: MaterialButton) {
        boton.strokeColor = ColorStateList.valueOf(Color.parseColor("#2C6BFE"))
        boton.setTextColor(Color.parseColor("#2C6BFE"))
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAF0FF"))
    }

    private fun verificarBotonConfirmar(btnConfirmarCita: MaterialButton) {
        btnConfirmarCita.isEnabled = (diaSeleccionadoDatos != null && horaSeleccionadaTexto != null)
    }

    private fun formatearBotonDia(fecha: Calendar): String {
        val formatoDia = SimpleDateFormat("EEEE", Locale("es", "ES"))
        val nombreDia = formatoDia.format(fecha.time).replaceFirstChar { it.uppercase() }
        val diaDelMes = fecha.get(Calendar.DAY_OF_MONTH)
        val formatoMes = SimpleDateFormat("MMMM", Locale("es", "ES"))
        val nombreMes = formatoMes.format(fecha.time).replaceFirstChar { it.uppercase() }
        return "$nombreDia\n$diaDelMes de $nombreMes"
    }

    private fun formatearFechaCompleta(fecha: Calendar): String {
        return formatearBotonDia(fecha).replace("\n", " ")
    }

    private fun dpAPx(dp: Int): Int {
        val densidad = resources.displayMetrics.density
        return (dp * densidad).toInt()
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