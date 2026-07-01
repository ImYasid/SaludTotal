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

/**
 * Controlador de la interfaz de registro de usuarios de SaludTotal.
 * * Esta clase gestiona el alta de nuevos pacientes, aplicando validaciones granulares
 * en tiempo de ejecución. Integra un selector de fecha nativo para mitigar errores de
 * entrada manual y un mecanismo persistente de confirmación de registro adaptado
 * a las necesidades de accesibilidad del adulto mayor.
 */
class RegisterActivity : AppCompatActivity() {

    // Almacena el cálculo matemático de la edad del paciente derivado del DatePicker
    private var edadCalculadaDelUsuario = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ==========================================
        // 1. VINCULACIÓN DE CONTENEDORES (MATERIAL DESIGN)
        // ==========================================
        // Estructuras que encapsulan las cajas de texto y gestionan las alertas de error fijas
        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilNombres = findViewById<TextInputLayout>(R.id.tilNombres)
        val tilEdad = findViewById<TextInputLayout>(R.id.tilEdad)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasenaRegistro)

        // ==========================================
        // 2. VINCULACIÓN DE CAMPOS DE ENTRADA Y BOTONES
        // ==========================================
        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etNombres = findViewById<TextInputEditText>(R.id.etNombres)
        val etEdad = findViewById<TextInputEditText>(R.id.etEdadRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasenaRegistro)

        val btnRegistrarCuenta = findViewById<MaterialButton>(R.id.btnRegistrarCuenta)
        val btnVolverLogin = findViewById<MaterialButton>(R.id.btnVolverLogin)

        // ==========================================
        // 3. LIMPIEZA PROACTIVA DE ESTADOS DE ERROR
        // ==========================================
        // Patrón UX: Disminuye la frustración visual liberando la alerta roja
        // de inmediato cuando el usuario enfoca nuevamente el componente para editarlo.
        etCedula.setOnClickListener { tilCedula.error = null }
        etNombres.setOnClickListener { tilNombres.error = null }
        etCorreo.setOnClickListener { tilCorreo.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }

        // ==========================================
        // 4. CONTROLADOR DE ENTRADA INTERACTIVA (CALENDARIO)
        // ==========================================
        // Al marcar el campo como 'focusable="false"' en el XML, este click listener
        // actúa como un botón para desplegar un DatePickerDialog nativo. Se anula la
        // inserción manual de datos erróneos (ej. formatos de fecha inexistentes).
        etEdad.setOnClickListener {
            tilEdad.error = null
            val calendario = Calendar.getInstance()
            val anioActual = calendario.get(Calendar.YEAR)
            val mesActual = calendario.get(Calendar.MONTH)
            val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Cálculo aritmético preciso de la edad del paciente
                edadCalculadaDelUsuario = anioActual - year
                if (mesActual < month || (mesActual == month && diaActual < dayOfMonth)) {
                    edadCalculadaDelUsuario--
                }

                // Inserción de cadena formateada que provee retroalimentación visual inmediata
                val mesReal = month + 1
                etEdad.setText("$dayOfMonth/$mesReal/$year  (Edad: $edadCalculadaDelUsuario años)")
            }, anioActual, mesActual, diaActual)

            // Restricción técnica: Impide la selección de marcas de tiempo futuras
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // ==========================================
        // 5. MÓDULO DE VALIDACIÓN Y PERSISTENCIA
        // ==========================================
        btnRegistrarCuenta.setOnClickListener {

            // Mecanismo Antirrebote (Debounce) preventivo para clicks repetitivos involuntarios
            btnRegistrarCuenta.isEnabled = false
            btnRegistrarCuenta.postDelayed({ btnRegistrarCuenta.isEnabled = true }, 1000)

            // Sanitización preliminar de entradas mediante el truncado de espacios vacíos
            val cedula = etCedula.text.toString().trim()
            val nombres = etNombres.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Reset general de la UI antes del procesamiento de los datos
            tilCedula.error = null
            tilNombres.error = null
            tilEdad.error = null
            tilCorreo.error = null
            tilContrasena.error = null

            // --- Orquestación de Reglas de Validación Estrictas ---
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
            // Patrón de expresión regular provisto por el SDK de Android para correos
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

            // --- Confirmación y Escritura de Credenciales (Persistencia Local) ---
            val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("usuarioGuardado", cedula)
            editor.putString("contrasenaGuardada", contrasena)
            editor.putString("nombreGuardado", nombres)
            editor.apply() // Commit asíncrono optimizado para hilos de interfaz de usuario

            // Renderizado del diálogo de confirmación contextual personalizado
            mostrarConfirmacionRegistro(nombres)
        }

        // Retorno simple al stack previo (LoginActivity)
        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Despliega un cuadro de diálogo modal e imperativo que confirma el éxito de la transacción.
     * Evalúa la edad del registro actual para adaptar el mensaje descriptivo y notificar la
     * inicialización automática de las propiedades de interfaz del "Modo Fácil".
     *
     * @param nombres El nombre completo del usuario utilizado en la personalización de la alerta.
     */
    private fun mostrarConfirmacionRegistro(nombres: String) {
        val mensaje = if (edadCalculadaDelUsuario >= 60) {
            "¡Listo, $nombres! Tu cuenta fue creada con éxito.\n\nActivamos el Modo Fácil para ti, con letras más grandes y menos pasos."
        } else {
            "¡Listo, $nombres! Tu cuenta fue creada con éxito.\n\nAhora puedes iniciar sesión con tu cédula y tu contraseña."
        }

        AlertDialog.Builder(this)
            .setTitle("Cuenta creada")
            .setMessage(mensaje)
            .setCancelable(false) // Forzar lectura e interacción deliberada del usuario
            .setPositiveButton("Ir a Iniciar Sesión") { _, _ -> finish() }
            .show()
    }
}