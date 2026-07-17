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

/**
 * Pantalla inicial de la aplicación (Login).
 * Permite a los usuarios registrados ingresar a su cuenta usando su cédula y contraseña.
 * También ofrece opciones para registrarse como usuario nuevo o solicitar ayuda.
 */
class LoginActivity : AppCompatActivity() {

    // Conexión con la base de datos para buscar si el usuario existe
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Enlaza las cajas de texto y botones, y establece qué pasa cuando el usuario intenta ingresar.
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Enlazar las vistas del diseño XML con el código Kotlin
        val tilUsuario = findViewById<TextInputLayout>(R.id.tilUsuario)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasena)
        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnIniciarSesion)
        val btnIrRegistro = findViewById<MaterialButton>(R.id.btnIrRegistro)
        val btnAyudaLogin = findViewById<MaterialButton>(R.id.btnAyudaLogin)

        // Quita el mensaje de error en color rojo apenas la persona empieza a corregir el campo
        etUsuario.setOnClickListener { tilUsuario.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }

        // 2. Lógica del botón principal "Iniciar Sesión"
        btnIniciarSesion.setOnClickListener {

            // Evita que un doble toque accidental envíe el formulario dos veces seguidas
            btnIniciarSesion.isEnabled = false
            btnIniciarSesion.postDelayed({ btnIniciarSesion.isEnabled = true }, 1000)

            val usuarioIngresado = etUsuario.text.toString().trim()
            val contrasenaIngresada = etContrasena.text.toString().trim()

            tilUsuario.error = null
            tilContrasena.error = null

            // Validamos que las cajas no estén vacías antes de buscar en la base de datos
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

            // Usuario "Maestro" oculto para poder hacer pruebas rápidas en las presentaciones
            val esUsuarioMaestro = (usuarioIngresado == "1754378097" && contrasenaIngresada == "Yasid123@")
            if (esUsuarioMaestro) {
                irAWelcome(usuarioId = -1L, nombre = "Administrador")
                return@setOnClickListener
            }

            // 3. Iniciamos la búsqueda en la base de datos sin congelar la pantalla (Corrutinas)
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    repository.iniciarSesion(usuarioIngresado, contrasenaIngresada)
                }

                // 4. Evaluamos si encontró al usuario o no
                if (user != null) {
                    // ÉXITO: El usuario existe y la contraseña es correcta
                    irAWelcome(usuarioId = user.id, nombre = user.fullName)
                } else {
                    // ERROR: Verificamos si al menos la cédula existe para darle un mejor consejo
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

        // 4. Botón de ayuda para adultos mayores o personas con dificultades técnicas
        btnAyudaLogin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Necesitas ayuda?")
                .setMessage("Llama a nuestra línea de atención al 1-800-SALUD para que un asesor te ayude a entrar o a agendar tu cita.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    /**
     * Cierra la pantalla de inicio de sesión y abre el menú principal (WelcomeActivity).
     *
     * @param usuarioId El ID único del usuario que acaba de ingresar.
     * @param nombre El nombre completo del usuario para saludarlo en la siguiente pantalla.
     */
    private fun irAWelcome(usuarioId: Long, nombre: String) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.putExtra("usuarioId", usuarioId)
        intent.putExtra("nombre", nombre)
        startActivity(intent)
        finish() // Cierra el Login para que no pueda volver atrás usando el botón "Atrás" del celular
    }

    /**
     * Muestra una ventana de error (alerta) explicando por qué no se pudo iniciar sesión.
     *
     * @param existeLaCedula Si es verdadero, significa que el usuario sí existe pero se equivocó de contraseña.
     * Si es falso, significa que ni siquiera tiene cuenta.
     */
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