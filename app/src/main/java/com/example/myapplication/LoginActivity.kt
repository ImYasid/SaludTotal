package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.LinearLayout
import android.widget.TextView


class LoginActivity : AppCompatActivity() {

    private val telefonoAyuda = "18007258383"

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

            val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
            val usuarioGuardado = sharedPreferences.getString("usuarioGuardado", "")
            val contrasenaGuardada = sharedPreferences.getString("contrasenaGuardada", "")

            val esUsuarioMaestro = (usuarioIngresado == "1754378097" && contrasenaIngresada == "Yasid123@")
            val esUsuarioRegistrado = (usuarioIngresado == usuarioGuardado && contrasenaIngresada == contrasenaGuardada && usuarioIngresado.isNotEmpty())

            if (esUsuarioMaestro || esUsuarioRegistrado) {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Diálogo en vez de Toast: se queda en pantalla hasta que la persona lo cierre,
                // y le dice exactamente qué hacer a continuación.
                mostrarErrorLoginPersistente(usuarioGuardado)
            }
        }

        // 3. Lógica del botón "Regístrate"
        btnIrRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 4. NUEVO: Botón de ayuda directa, visible desde la primera pantalla que ve el usuario
        btnAyudaLogin.setOnClickListener {
            llamarLineaDeAyuda()
        }
    }

    private fun mostrarErrorLoginPersistente(usuarioGuardado: String?) {
        val hayCuentaRegistrada = !usuarioGuardado.isNullOrEmpty()
        val mensaje = if (hayCuentaRegistrada) {
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