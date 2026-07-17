package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de Bienvenida (Menú Principal / Dashboard).
 * Es la primera pantalla que ve el usuario después de iniciar sesión correctamente.
 * Permite acceder a las funciones principales de la aplicación: Agendar nuevas citas,
 * revisar el historial de citas o pedir ayuda telefónica.
 *
 * Se espera que reciba por [Intent] los siguientes Extras:
 * - "usuarioId" (Long): El ID de la sesión actual (o -1L si es el usuario Maestro de prueba).
 */
class WelcomeActivity : AppCompatActivity() {

    // Número telefónico central para soporte técnico y reservas telefónicas
    private val telefonoAyuda = "18007258383"

    // Conexión con la base de datos para consultar los datos del usuario conectado
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Carga el nombre real del usuario desde la base de datos para mostrarle un saludo personalizado
     * y configura todos los botones del menú principal.
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // 1. Enlazamos los componentes visuales del XML con el código
        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        val btnMisCitas = findViewById<MaterialButton>(R.id.btnMisCitas)
        val btnAyuda = findViewById<MaterialButton>(R.id.btnAyuda)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)

        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // 2. Capturamos la credencial del paciente enviada desde el Login
        val usuarioId = intent.getLongExtra("usuarioId", -1L)

        // ---> 3. Lógica del Saludo Personalizado <---
        if (usuarioId == -1L) {
            // Caso especial: Es la cuenta maestra de demostración
            tvSaludo.text = "Hola, Administrador"
        } else {
            // Si es un usuario real, buscamos su nombre en la base de datos sin congelar la pantalla
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    repository.obtenerUsuarioPorId(usuarioId)
                }

                // Si lo encuentra, extraemos solo su primer nombre (corta el texto en el primer espacio)
                if (user != null) {
                    val primerNombre = user.fullName.trim().substringBefore(" ")
                    tvSaludo.text = "Hola, $primerNombre"
                }
            }
        }

        // ---> 4. Configuración de los botones centrales <---
        btnAgendarCita.setOnClickListener {
            irAAgendarCita(usuarioId)
        }

        btnMisCitas.setOnClickListener {
            val intent = Intent(this, MisCitasActivity::class.java)
            intent.putExtra("usuarioId", usuarioId)
            startActivity(intent)
        }

        btnAyuda.setOnClickListener {
            llamarLineaDeAyuda()
        }

        // ---> 5. Configuración de la barra de navegación inferior <---
        navInicio.setOnClickListener {
            // Vacío intencionalmente, ya estamos en la pantalla de inicio
        }

        navAgendar.setOnClickListener {
            irAAgendarCita(usuarioId)
        }

        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    /**
     * Inicia el flujo para agendar una nueva cita (Paso 1: Seleccionar Especialidad).
     *
     * @param usuarioId ID del usuario para mantener la sesión abierta durante el proceso.
     */
    private fun irAAgendarCita(usuarioId: Long) {
        val intent = Intent(this, SpecialtiesActivity::class.java)
        intent.putExtra("usuarioId", usuarioId)
        startActivity(intent)
    }

    /**
     * Abre un cuadro de confirmación antes de permitirle al usuario cerrar su sesión,
     * para evitar salidas accidentales.
     */
    private fun confirmarCierreDeSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                // Borra el historial para que no pueda volver a entrar tocando "Atrás"
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Usa un Intent implícito de tipo ACTION_DIAL para abrir la aplicación de teléfono (marcador)
     * del celular con el número de atención al cliente ya escrito, listo para llamar.
     */
    private fun llamarLineaDeAyuda() {
        AlertDialog.Builder(this)
            .setTitle("Línea de ayuda")
            .setMessage("Vamos a abrir el marcador de tu teléfono con el número de SaludTotal ya escrito. Solo debes tocar el botón de llamar.")
            .setPositiveButton("Llamar") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefonoAyuda"))
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}