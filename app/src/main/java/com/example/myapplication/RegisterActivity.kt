package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.myapplication.data.local.SaludTotalDatabase
import com.example.myapplication.data.local.entity.UserEntity
import com.example.myapplication.data.repository.SaludTotalRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    // Variable global para guardar la edad calculada desde el calendario
    private var edadCalculadaDelUsuario = 0

    // La fecha "limpia" que se guarda en la base de datos (ej. "16/7/2026"),
    // separada del texto que se muestra en pantalla (que incluye "(Edad: 27 años)")
    private var fechaNacimientoParaGuardar = ""

    // Acceso a la base de datos real, en vez de SharedPreferences
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilNombres = findViewById<TextInputLayout>(R.id.tilNombres)
        val tilEdad = findViewById<TextInputLayout>(R.id.tilEdad)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasenaRegistro)
        val tilConfirmarContrasena = findViewById<TextInputLayout>(R.id.tilConfirmarContrasena) // <-- NUEVO

        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etNombres = findViewById<TextInputEditText>(R.id.etNombres)
        val etEdad = findViewById<TextInputEditText>(R.id.etEdadRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasenaRegistro)
        val etConfirmarContrasena = findViewById<TextInputEditText>(R.id.etConfirmarContrasena) // <-- NUEVO

        val btnRegistrarCuenta = findViewById<MaterialButton>(R.id.btnRegistrarCuenta)
        val btnVolverLogin = findViewById<MaterialButton>(R.id.btnVolverLogin)

        // Limpia el error de un campo apenas la persona vuelve a tocarlo
        etCedula.setOnClickListener { tilCedula.error = null }
        etNombres.setOnClickListener { tilNombres.error = null }
        etCorreo.setOnClickListener { tilCorreo.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }
        etConfirmarContrasena.setOnClickListener { tilConfirmarContrasena.error = null } // <-- NUEVO

        // ---> LÓGICA DEL CALENDARIO (DatePicker) <---
        etEdad.setOnClickListener {
            tilEdad.error = null
            val calendario = Calendar.getInstance()
            val anioActual = calendario.get(Calendar.YEAR)
            val mesActual = calendario.get(Calendar.MONTH)
            val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Calcular la edad matemáticamente
                edadCalculadaDelUsuario = anioActual - year
                if (mesActual < month || (mesActual == month && diaActual < dayOfMonth)) {
                    edadCalculadaDelUsuario--
                }

                // Mostrar la fecha y la edad en la caja de texto
                val mesReal = month + 1
                fechaNacimientoParaGuardar = "$dayOfMonth/$mesReal/$year"
                etEdad.setText("$dayOfMonth/$mesReal/$year  (Edad: $edadCalculadaDelUsuario años)")
            }, anioActual, mesActual, diaActual)

            // Limitar el calendario para que no puedan elegir fechas en el futuro
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // ---> LÓGICA DEL BOTÓN REGISTRARSE (Con Validaciones campo por campo) <---
        btnRegistrarCuenta.setOnClickListener {
            // Evita doble envío por doble toque accidental
            btnRegistrarCuenta.isEnabled = false
            btnRegistrarCuenta.postDelayed({ btnRegistrarCuenta.isEnabled = true }, 1000)

            val cedula = etCedula.text.toString().trim()
            val nombres = etNombres.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            val confirmarContrasena = etConfirmarContrasena.text.toString().trim() // <-- NUEVO

            tilCedula.error = null
            tilNombres.error = null
            tilEdad.error = null
            tilCorreo.error = null
            tilContrasena.error = null
            tilConfirmarContrasena.error = null // <-- NUEVO

            // Validaciones secuenciales
            if (cedula.isEmpty()) {
                tilCedula.error = "Escribe tu número de cédula"
                etCedula.requestFocus()
                return@setOnClickListener
            }
            if (cedula.length != 10 || !cedula.all { it.isDigit() }) {
                tilCedula.error = "La cédula debe tener exactamente 10 números"
                etCedula.requestFocus()
                return@setOnClickListener
            }
            if (nombres.isEmpty()) {
                tilNombres.error = "Escribe tus nombres completos"
                etNombres.requestFocus()
                return@setOnClickListener
            }
            if (edadCalculadaDelUsuario == 0) {
                tilEdad.error = "Toca este campo y elige tu fecha de nacimiento"
                return@setOnClickListener
            }
            if (correo.isEmpty()) {
                tilCorreo.error = "Escribe tu correo electrónico"
                etCorreo.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                tilCorreo.error = "Este correo no es válido, revísalo"
                etCorreo.requestFocus()
                return@setOnClickListener
            }
            if (contrasena.isEmpty()) {
                tilContrasena.error = "Escribe una contraseña"
                etContrasena.requestFocus()
                return@setOnClickListener
            }
            if (contrasena.length < 6) {
                tilContrasena.error = "La contraseña debe tener al menos 6 caracteres"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            // ---> NUEVA VALIDACIÓN: Confirmar Contraseña <---
            if (confirmarContrasena.isEmpty()) {
                tilConfirmarContrasena.error = "Vuelve a escribir tu contraseña"
                etConfirmarContrasena.requestFocus()
                return@setOnClickListener
            }
            if (contrasena != confirmarContrasena) {
                tilConfirmarContrasena.error = "Las contraseñas no coinciden"
                etConfirmarContrasena.requestFocus()
                return@setOnClickListener
            }

            // Si pasa todas las validaciones, guardamos en la base de datos real
            val nuevoUsuario = UserEntity(
                documentNumber = cedula,
                fullName = nombres,
                birthDate = fechaNacimientoParaGuardar,
                email = correo,
                password = contrasena
            )

            lifecycleScope.launch {
                val resultado = withContext(Dispatchers.IO) {
                    repository.registrarUsuario(nuevoUsuario)
                }

                resultado.onSuccess {
                    mostrarConfirmacionRegistro(nombres)
                }.onFailure { error ->
                    tilCedula.error = "Ya existe una cuenta con esta cédula"
                    etCedula.requestFocus()
                    btnRegistrarCuenta.isEnabled = true
                }
            }
        }

        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun mostrarConfirmacionRegistro(nombres: String) {
        val mensaje = if (edadCalculadaDelUsuario >= 60) {
            "¡Listo, $nombres! Tu cuenta fue creada con éxito.\n\nActivamos el Modo Fácil para ti, con letras más grandes y menos pasos."
        } else {
            "¡Listo, $nombres! Tu cuenta fue creada con éxito.\n\nAhora puedes iniciar sesión con tu cédula y tu contraseña."
        }
        AlertDialog.Builder(this)
            .setTitle("Cuenta creada")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Ir a Iniciar Sesión") { _, _ -> finish() }
            .show()
    }
}