package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar


class RegisterActivity : AppCompatActivity() {

    // Variable global para guardar la edad calculada desde el calendario
    private var edadCalculadaDelUsuario = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilNombres = findViewById<TextInputLayout>(R.id.tilNombres)
        val tilEdad = findViewById<TextInputLayout>(R.id.tilEdad)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasenaRegistro)

        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etNombres = findViewById<TextInputEditText>(R.id.etNombres)
        val etEdad = findViewById<TextInputEditText>(R.id.etEdadRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasenaRegistro)

        val btnRegistrarCuenta = findViewById<MaterialButton>(R.id.btnRegistrarCuenta)
        val btnVolverLogin = findViewById<MaterialButton>(R.id.btnVolverLogin)

        // Limpia el error de un campo apenas la persona vuelve a tocarlo
        etCedula.setOnClickListener { tilCedula.error = null }
        etNombres.setOnClickListener { tilNombres.error = null }
        etCorreo.setOnClickListener { tilCorreo.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }

        // ---> LÓGICA DEL CALENDARIO (DatePicker) <---
        // El campo de edad ahora es de solo lectura (ver XML): la única forma de llenarlo
        // es con este calendario, así se evita que alguien escriba una fecha inválida a mano.
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

            tilCedula.error = null
            tilNombres.error = null
            tilEdad.error = null
            tilCorreo.error = null
            tilContrasena.error = null

            // Cada validación apunta al campo exacto con el problema, en vez de un mensaje genérico
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

            // Si pasa todas las validaciones, guardamos los datos
            val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("usuarioGuardado", cedula)
            editor.putString("contrasenaGuardada", contrasena)
            editor.putString("nombreGuardado", nombres)
            editor.apply()

            mostrarConfirmacionRegistro(nombres)
        }

        // Lógica del botón de volver
        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    // Diálogo que se queda en pantalla hasta que la persona lo cierre a propósito,
    // en vez de un Toast que puede desaparecer antes de que alcance a leerlo.
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