package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Enlazar las vistas del XML con el código Kotlin
        val tilUsuario = findViewById<TextInputLayout>(R.id.tilUsuario)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasena)
        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnIniciarSesion)
        val btnIrRegistro = findViewById<MaterialButton>(R.id.btnIrRegistro)
        val btnAyudaLogin = findViewById<MaterialButton>(R.id.btnAyudaLogin)

        // Quita el mensaje de error apenas la persona empieza a corregir el campo
        etUsuario.setOnClickListener { tilUsuario.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }

        // 2. Lógica del botón "Iniciar Sesión"
        btnIniciarSesion.setOnClickListener {
            // Evita que un doble toque (común en manos con menos precisión) mande el formulario 2 veces
            btnIniciarSesion.isEnabled = false
            btnIniciarSesion.postDelayed({ btnIniciarSesion.isEnabled = true }, 1000)

            // Usamos tus nombres de variables originales
            val usuarioIngresado = etUsuario.text.toString().trim()
            val contrasenaIngresada = etContrasena.text.toString().trim()

            tilUsuario.error = null
            tilContrasena.error = null

            // Validación campo por campo, con mensaje pegado al campo (no desaparece solo)
            if (usuarioIngresado.isEmpty()) {
                tilUsuario.error = "Escribe tu número de cédula"
                etUsuario.requestFocus()
                return@setOnClickListener
            }
            if (contrasenaIngresada.isEmpty()) {
                tilContrasena.error = "Escribe tu contraseña"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            // Mantenemos el usuario maestro como acceso rápido de prueba/demostración.
            val esUsuarioMaestro = (usuarioIngresado == "1754378097" && contrasenaIngresada == "Yasid123@")

            if (esUsuarioMaestro) {
                irAWelcome(usuarioId = -1L, nombre = "Administrador")
                return@setOnClickListener
            }

            // 2. Iniciamos el proceso asíncrono
            lifecycleScope.launch {

                // 3. Vamos al hilo secundario a validar las credenciales con "usuarioIngresado"
                val user = withContext(Dispatchers.IO) {
                    repository.iniciarSesion(usuarioIngresado, contrasenaIngresada)
                }

                // 4. Evaluamos el resultado
                if (user != null) {
                    // ✅ ÉXITO: El usuario sí existe y la contraseña coincide
                    irAWelcome(usuarioId = user.id, nombre = user.fullName)
                } else {
                    // ❌ ERROR: Credenciales incorrectas.
                    // Vamos de nuevo al hilo secundario a ver si al menos la cédula existe
                    val existeLaCedula = withContext(Dispatchers.IO) {
                        repository.obtenerUsuarioPorCedula(usuarioIngresado) != null
                    }

                    mostrarErrorLoginPersistente(existeLaCedula)
                }
            }
        }

        // 3. Lógica del botón "Regístrate"
        btnIrRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 4. NUEVO: Botón de ayuda directa, visible desde la primera pantalla que ve el usuario
        btnAyudaLogin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Necesitas ayuda?")
                .setMessage("Llama a nuestra línea de atención al 1-800-SALUD para que un asesor te ayude a entrar o a agendar tu cita.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    private fun irAWelcome(usuarioId: Long, nombre: String) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.putExtra("usuarioId", usuarioId)
        intent.putExtra("nombre", nombre)
        startActivity(intent)
        finish()
    }

    private fun mostrarErrorLoginPersistente(existeLaCedula: Boolean) {
        val mensaje = if (existeLaCedula) {
            "La cédula o la contraseña no coinciden con tu cuenta.\n\nRevisa que no tengas activado el bloqueo de mayúsculas y vuelve a intentar."
        } else {
            "La cédula o la contraseña no son correctas.\n\nSi es tu primera vez usando la app, toca '¿No tienes cuenta? Regístrate aquí' para crear una cuenta."
        }
        AlertDialog.Builder(this)
            .setTitle("No pudimos ingresar")
            .setMessage(mensaje)
            .setPositiveButton("Intentar de nuevo", null)
            .show()
    }
}