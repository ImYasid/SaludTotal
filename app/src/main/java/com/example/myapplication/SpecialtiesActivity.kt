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

class SpecialtiesActivity : AppCompatActivity() {

    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    // Guardamos el botón junto con la entidad completa (para tener el id real al navegar)
    private val botonesEspecialidad = mutableListOf<Pair<MaterialButton, SpecialtyEntity>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)
        val gridEspecialidades = findViewById<GridLayout>(R.id.gridEspecialidades)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnMicrofono = findViewById<MaterialCardView>(R.id.btnMicrofono)
        val tvSinResultados = findViewById<TextView>(R.id.tvSinResultados)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        btnVolver.setOnClickListener { finish() }

        // ---> Cargar las especialidades reales desde Room, en vez de 6 botones fijos <---
        lifecycleScope.launch {
            // ✅ AQUÍ ESTÁ EL CAMBIO: Movemos la consulta al hilo secundario de Entrada/Salida (IO)
            val especialidades = withContext(Dispatchers.IO) {
                repository.obtenerEspecialidades()
            }

            if (especialidades.isEmpty()) {
                // No debería pasar (SeedCallback las precarga), pero por si acaso
                tvSinResultados.text = "Todavía no hay especialidades cargadas."
                tvSinResultados.visibility = android.view.View.VISIBLE
                return@launch
            }

            especialidades.forEach { especialidad ->
                val boton = crearBotonEspecialidad(especialidad)
                botonesEspecialidad.add(boton to especialidad)
                gridEspecialidades.addView(boton)

                boton.setOnClickListener {
                    irADoctoresDisponibles(especialidad, usuarioId)
                }
            }
        }

        // ---> Búsqueda real: mientras escribe, se ocultan las especialidades que no coinciden <---
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val textoBusqueda = s.toString().trim().lowercase(Locale.getDefault())
                var hayResultados = false

                botonesEspecialidad.forEach { (boton, especialidad) ->
                    val coincide = especialidad.nombre.lowercase(Locale.getDefault()).contains(textoBusqueda)
                    boton.visibility = if (coincide) android.view.View.VISIBLE else android.view.View.GONE
                    if (coincide) hayResultados = true
                }

                tvSinResultados.visibility = if (!hayResultados) android.view.View.VISIBLE else android.view.View.GONE
                gridEspecialidades.visibility = if (!hayResultados) android.view.View.GONE else android.view.View.VISIBLE
            }
        })

        // El micrófono explica lo que hace en vez de quedar mudo al tocarlo
        btnMicrofono.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Búsqueda por voz")
                .setMessage("Esta función estará disponible pronto. Por ahora, puedes escribir el nombre de la especialidad en el cuadro de búsqueda.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // ---> Misma barra de navegación inferior que en Bienvenida <---
        navInicio.setOnClickListener {
            finish() // Regresa al Welcome
        }

        navAgendar.setOnClickListener { } // Ya estamos en este paso

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    private fun crearBotonEspecialidad(especialidad: SpecialtyEntity): MaterialButton {
        val boton = MaterialButton(this)
        boton.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = dpAPx(130)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(dpAPx(8), dpAPx(8), dpAPx(8), dpAPx(8))
        }
        boton.text = especialidad.nombre.replace(" ", "\n")
        boton.isAllCaps = false
        boton.textSize = 16f
        boton.setTypeface(boton.typeface, android.graphics.Typeface.BOLD)
        boton.setTextColor(Color.WHITE)
        boton.cornerRadius = dpAPx(16)
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(especialidad.colorHex))

        // Alineación del texto
        boton.gravity = android.view.Gravity.CENTER
        boton.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER

        // 👇 AQUÍ ESTÁ EL CAMBIO PARA UNIRLOS 👇
        // Esto agrupa el ícono y el texto, y los centra verticalmente como un solo bloque
        boton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP

        // Esto define la separación exacta (en dp) entre el ícono y las letras.
        // Puedes subir o bajar este 8 si los quieres aún más juntos o separados.
        boton.iconPadding = dpAPx(8)

        boton.iconSize = dpAPx(36)
        boton.iconTint = ColorStateList.valueOf(Color.WHITE)
        boton.icon = obtenerIconoPorNombre(especialidad.iconoResName)
        boton.contentDescription = "Elegir especialidad ${especialidad.nombre}"

        return boton
    }

    /** Los íconos guardados en Room son nombres de drawables. */
    private fun obtenerIconoPorNombre(nombreIcono: String): android.graphics.drawable.Drawable? {
        // 1. Primero busca en TU propia carpeta res/drawable (usando packageName)
        var idRecurso = resources.getIdentifier(nombreIcono, "drawable", packageName)

        // 2. Si no lo encuentra ahí, busca en los íconos por defecto de Android
        if (idRecurso == 0) {
            idRecurso = resources.getIdentifier(nombreIcono, "drawable", "android")
        }

        return if (idRecurso != 0) androidx.core.content.ContextCompat.getDrawable(this, idRecurso) else null
    }

    private fun dpAPx(dp: Int): Int {
        val densidad = resources.displayMetrics.density
        return (dp * densidad).toInt()
    }

    private fun irADoctoresDisponibles(especialidad: SpecialtyEntity, usuarioId: Long) {
        val intent = Intent(this, DoctoresDisponibles::class.java)
        intent.putExtra("especialidadId", especialidad.id)
        intent.putExtra("especialidad", especialidad.nombre)
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
}