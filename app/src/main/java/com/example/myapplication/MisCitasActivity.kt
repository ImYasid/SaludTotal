package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de "Mis Citas".
 * Muestra el historial de citas médicas que el usuario ha agendado.
 * Permite visualizar los detalles básicos de cada cita y cancelarlas si es necesario.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "usuarioId" (Long): El ID del usuario actual para buscar solo sus citas en la base de datos.
 */
class MisCitasActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var tvSinCitas: TextView
    private lateinit var adapter: CitasAdapter
    private var usuarioId: Long = -1L

    // Conexión con la base de datos para consultar o borrar citas
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Recupera el ID del usuario, configura la lista visual (RecyclerView) y manda a buscar
     * las citas guardadas en la base de datos.
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        // Capturamos el ID del usuario que nos mandó el menú principal (WelcomeActivity)
        usuarioId = intent.getLongExtra("usuarioId", -1L)

        // 1. Enlazamos los elementos visuales del XML con el código
        rvCitas = findViewById(R.id.rvCitas)
        tvSinCitas = findViewById(R.id.tvSinCitas)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)

        btnVolver.setOnClickListener { finish() }

        // 2. Configuramos cómo se verá la lista (de arriba hacia abajo) y conectamos el adaptador
        rvCitas.layoutManager = LinearLayoutManager(this)

        // El adaptador recibe una lista vacía al principio, y una instrucción de qué hacer
        // cuando tocan el botón de "Eliminar" en alguna tarjeta
        adapter = CitasAdapter(emptyList()) { citaSeleccionada ->
            confirmarEliminacion(citaSeleccionada)
        }
        rvCitas.adapter = adapter

        // 3. Vamos a buscar las citas a la base de datos
        cargarCitas()
    }

    /**
     * Busca las citas del usuario en la base de datos trabajando en un hilo secundario
     * (para no congelar la pantalla). Si encuentra citas, las muestra en la lista.
     * Si no encuentra ninguna, muestra el mensaje de texto avisando que no hay citas.
     */
    private fun cargarCitas() {
        lifecycleScope.launch {
            val citas = withContext(Dispatchers.IO) {
                repository.obtenerCitasDelUsuario(usuarioId.toInt())
            }

            if (citas.isEmpty()) {
                // Oculta la lista vacía y muestra el texto de aviso
                rvCitas.visibility = View.GONE
                tvSinCitas.visibility = View.VISIBLE
            } else {
                // Oculta el aviso y llena la lista con las tarjetas de las citas
                rvCitas.visibility = View.VISIBLE
                tvSinCitas.visibility = View.GONE
                adapter.actualizarLista(citas)
            }
        }
    }

    /**
     * Muestra una ventana de alerta preguntando al usuario si realmente desea cancelar la cita.
     * Esto evita que borren una cita por accidente al tocar mal la pantalla.
     *
     * @param cita Objeto con toda la información de la cita que se intentó borrar.
     */
    private fun confirmarEliminacion(cita: AppointmentEntity) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que quieres cancelar tu cita de ${cita.especialidadNombre}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                // Si el usuario confirma, procedemos a borrarla de la base de datos
                eliminarCitaDeBD(cita)
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Borra permanentemente una cita de la base de datos y refresca la pantalla.
     *
     * @param cita Objeto de la cita que será eliminada.
     */
    private fun eliminarCitaDeBD(cita: AppointmentEntity) {
        lifecycleScope.launch {
            // Vamos al hilo secundario a borrar la cita
            withContext(Dispatchers.IO) {
                repository.eliminarCita(cita.id)
            }

            // Avisamos que se borró con éxito
            Toast.makeText(this@MisCitasActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()

            // Volvemos a consultar la base de datos para que la lista se actualice sola (desaparece la tarjeta)
            cargarCitas()
        }
    }
}