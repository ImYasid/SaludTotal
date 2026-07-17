package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.SpecialtyEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pantalla correspondiente al "Paso 1" de agendar una cita.
 * Muestra una cuadrícula dinámica con todas las especialidades médicas disponibles en la base de datos.
 * Incluye un buscador en tiempo real para filtrar las especialidades rápidamente.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "usuarioId" (Long): El ID del usuario actual, necesario para arrastrar su sesión al agendar la cita.
 */
class SpecialtiesActivity : AppCompatActivity() {

    // Conexión principal con la base de datos local (Room)
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    // Guardamos cada botón visual (MaterialButton) emparejado con sus datos reales (SpecialtyEntity)
    // Esto es vital para que la barra de búsqueda sepa qué botón ocultar o mostrar al escribir.
    private val botonesEspecialidad = mutableListOf<Pair<MaterialButton, SpecialtyEntity>>()

    /**
     * Prepara la pantalla al abrirse.
     * Carga las especialidades desde la base de datos, dibuja sus botones correspondientes
     * y configura la lógica del buscador para filtrar en tiempo real.
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        // Enlazamos las vistas visuales del XML
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val gridEspecialidades = findViewById<GridLayout>(R.id.gridEspecialidades)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnMicrofono = findViewById<MaterialCardView>(R.id.btnMicrofono)
        val tvSinResultados = findViewById<TextView>(R.id.tvSinResultados)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        btnVolver.setOnClickListener { finish() }

        // ---> 1. Cargar las especialidades reales desde la Base de Datos <---
        lifecycleScope.launch {
            // Usamos Dispatchers.IO para operaciones pesadas de base de datos sin trabar la pantalla
            val especialidades = withContext(Dispatchers.IO) {
                repository.obtenerEspecialidades()
            }

            // Si por algún error la base estuviera vacía, mostramos un aviso
            if (especialidades.isEmpty()) {
                tvSinResultados.text = "Todavía no hay especialidades cargadas."
                tvSinResultados.visibility = android.view.View.VISIBLE
                return@launch
            }

            // Dibujamos un botón cuadrado grande por cada especialidad que encontró
            especialidades.forEach { especialidad ->
                val boton = crearBotonEspecialidad(especialidad)

                // Lo guardamos en nuestra lista maestra para el buscador
                botonesEspecialidad.add(boton to especialidad)

                // Lo pegamos visualmente en el GridLayout de la pantalla
                gridEspecialidades.addView(boton)

                // Acción: ¿Qué pasa si lo tocan? Vamos al Paso 2 (Doctores)
                boton.setOnClickListener {
                    irADoctoresDisponibles(especialidad, usuarioId)
                }
            }
        }

        // ---> 2. Buscador en Tiempo Real <---
        // Reacciona cada vez que el usuario escribe o borra una sola letra
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Convertimos lo que escribió a minúsculas ("Cardio" -> "cardio")
                val textoBusqueda = s.toString().trim().lowercase(Locale.getDefault())
                var hayResultados = false

                // Revisamos cada botón de nuestra lista maestra
                botonesEspecialidad.forEach { (boton, especialidad) ->
                    // Si el nombre real contiene las letras que escribió el usuario...
                    val coincide = especialidad.nombre.lowercase(Locale.getDefault()).contains(textoBusqueda)

                    // ...lo mostramos. Si no coincide, lo ocultamos visualmente.
                    boton.visibility = if (coincide) android.view.View.VISIBLE else android.view.View.GONE
                    if (coincide) hayResultados = true
                }

                // Si ninguna especialidad coincidió (ej. escribió "Aviones"), mostramos un mensaje de error
                tvSinResultados.visibility = if (!hayResultados) android.view.View.VISIBLE else android.view.View.GONE
                gridEspecialidades.visibility = if (!hayResultados) android.view.View.GONE else android.view.View.VISIBLE
            }
        })

        // ---> 3. Botón del Micrófono (Demostración) <---
        btnMicrofono.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Búsqueda por voz")
                .setMessage("Esta función estará disponible pronto. Por ahora, puedes escribir el nombre de la especialidad en el cuadro de búsqueda.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // ---> 4. Navegación Inferior <---
        navInicio.setOnClickListener {
            finish() // Cierra y regresa a WelcomeActivity
        }

        navAgendar.setOnClickListener { } // Vacío intencionalmente, ya estamos aquí

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Dibuja y configura un botón visual por código (sin XML predefinido).
     * Le aplica color, tamaño, bordes curvos y centra el ícono sobre el texto.
     *
     * @param especialidad Objeto con los datos que definen el color, nombre e ícono de este botón.
     * @return El [MaterialButton] completamente configurado listo para ser añadido a la vista.
     */
    private fun crearBotonEspecialidad(especialidad: SpecialtyEntity): MaterialButton {
        val boton = MaterialButton(this)

        // Configuramos sus medidas (ancho expansible, alto fijo y márgenes)
        boton.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = dpAPx(130)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dpAPx(8), dpAPx(8), dpAPx(8), dpAPx(8))
        }

        // Estética: Si el nombre tiene dos palabras, lo baja de línea (Ej. "Médico\nGeneral")
        boton.text = especialidad.nombre.replace(" ", "\n")
        boton.isAllCaps = false
        boton.textSize = 16f
        boton.setTypeface(boton.typeface, android.graphics.Typeface.BOLD)
        boton.setTextColor(Color.WHITE)
        boton.cornerRadius = dpAPx(16)
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(especialidad.colorHex))

        // Centrado de elementos
        boton.gravity = android.view.Gravity.CENTER
        boton.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER

        // Apilamos el ícono y el texto como si fueran un bloque sólido en el centro
        boton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
        boton.iconPadding = dpAPx(8) // Separación entre ícono y texto
        boton.iconSize = dpAPx(36)
        boton.iconTint = ColorStateList.valueOf(Color.WHITE)
        boton.icon = obtenerIconoPorNombre(especialidad.iconoResName)
        boton.contentDescription = "Elegir especialidad ${especialidad.nombre}"

        return boton
    }

    /**
     * Busca un recurso de imagen (Drawable) en Android basado únicamente en su nombre de archivo (String).
     * Busca primero en los recursos del proyecto (res/drawable) y luego en el sistema nativo de Android.
     *
     * @param nombreIcono El nombre del archivo sin su extensión (Ej: "outline_cardiology_24").
     * @return El Drawable encontrado, o null si no existe.
     */
    private fun obtenerIconoPorNombre(nombreIcono: String): android.graphics.drawable.Drawable? {
        // 1. Buscamos en nuestra propia carpeta de recursos
        var idRecurso = resources.getIdentifier(nombreIcono, "drawable", packageName)

        // 2. Si no lo hallamos, buscamos en la librería de iconos gratuita del sistema Android
        if (idRecurso == 0) {
            idRecurso = resources.getIdentifier(nombreIcono, "drawable", "android")
        }

        return if (idRecurso != 0) androidx.core.content.ContextCompat.getDrawable(this, idRecurso) else null
    }

    /**
     * Convierte la medida estándar de diseño en Android (dp) a píxeles exactos
     * del celular actual, para asegurar que el botón se vea bien en todas las pantallas.
     */
    private fun dpAPx(dp: Int): Int {
        val densidad = resources.displayMetrics.density
        return (dp * densidad).toInt()
    }

    /**
     * Avanza al Paso 2 (DoctoresDisponibles).
     *
     * @param especialidad Objeto con los datos de la especialidad elegida.
     * @param usuarioId ID de la sesión actual.
     */
    private fun irADoctoresDisponibles(especialidad: SpecialtyEntity, usuarioId: Long) {
        val intent = Intent(this, DoctoresDisponibles::class.java)
        intent.putExtra("especialidadId", especialidad.id)
        intent.putExtra("especialidad", especialidad.nombre)
        intent.putExtra("usuarioId", usuarioId)
        startActivity(intent)
    }

    /**
     * Muestra alerta para cerrar sesión y volver a la pantalla inicial de Login.
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