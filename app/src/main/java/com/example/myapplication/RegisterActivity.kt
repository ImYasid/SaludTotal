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

/**
 * Pantalla de Registro de Usuarios.
 * Permite a una persona nueva crear su cuenta en la aplicación.
 * Realiza validaciones campo por campo (cédula, contraseñas coincidentes, correo)
 * y calcula automáticamente la edad del usuario usando un calendario visual.
 */
class RegisterActivity : AppCompatActivity() {

    // Variable global para guardar la edad exacta y decidir si activamos un mensaje especial (ej. Modo Fácil para >= 60 años)
    private var edadCalculadaDelUsuario = 0

    // Guarda la fecha "limpia" (ej. "16/7/2026") que enviaremos a la base de datos,
    // para no guardar el texto largo que se muestra en pantalla ("16/7/2026 (Edad: 27 años)")
    private var fechaNacimientoParaGuardar = ""

    // Conexión con la base de datos local para guardar al nuevo usuario
    private val repository by lazy {
        val db = SaludTotalDatabase.getDatabase(applicationContext)
        SaludTotalRepository(db.userDao(), db.specialtyDao(), db.doctorDao(), db.appointmentDao())
    }

    /**
     * Prepara la pantalla al abrirse.
     * Enlaza todas las cajas de texto, configura el calendario desplegable para la fecha
     * de nacimiento y programa todas las reglas de validación del botón "Registrarse".
     *
     * @param savedInstanceState Estado guardado de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Enlazamos los contenedores de los textos (los que muestran los errores en rojo)
        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilNombres = findViewById<TextInputLayout>(R.id.tilNombres)
        val tilEdad = findViewById<TextInputLayout>(R.id.tilEdad)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasenaRegistro)
        val tilConfirmarContrasena = findViewById<TextInputLayout>(R.id.tilConfirmarContrasena)

        // Enlazamos las cajas donde el usuario realmente escribe
        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etNombres = findViewById<TextInputEditText>(R.id.etNombres)
        val etEdad = findViewById<TextInputEditText>(R.id.etEdadRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasenaRegistro)
        val etConfirmarContrasena = findViewById<TextInputEditText>(R.id.etConfirmarContrasena)

        val btnRegistrarCuenta = findViewById<MaterialButton>(R.id.btnRegistrarCuenta)
        val btnVolverLogin = findViewById<MaterialButton>(R.id.btnVolverLogin)

        // Si el usuario toca un campo que tenía un error, borramos el error para que escriba cómodo
        etCedula.setOnClickListener { tilCedula.error = null }
        etNombres.setOnClickListener { tilNombres.error = null }
        etCorreo.setOnClickListener { tilCorreo.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }
        etConfirmarContrasena.setOnClickListener { tilConfirmarContrasena.error = null }

        // ---> 2. Lógica del Calendario (DatePicker) <---
        // Cuando tocan la caja de la edad, abrimos un calendario flotante
        etEdad.setOnClickListener {
            tilEdad.error = null
            val calendario = Calendar.getInstance()
            val anioActual = calendario.get(Calendar.YEAR)
            val mesActual = calendario.get(Calendar.MONTH)
            val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

            // Configuramos qué pasa cuando el usuario elige una fecha y le da a "Aceptar"
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->

                // Calculamos la edad matemáticamente (Año actual menos el año que nació)
                edadCalculadaDelUsuario = anioActual - year

                // Si todavía no ha cumplido años en este año actual, le restamos 1 a la edad
                if (mesActual < month || (mesActual == month && diaActual < dayOfMonth)) {
                    edadCalculadaDelUsuario--
                }

                // Guardamos la fecha limpia para la base de datos
                val mesReal = month + 1
                fechaNacimientoParaGuardar = "$dayOfMonth/$mesReal/$year"

                // Mostramos un texto amigable en la caja para que el usuario vea su edad calculada
                etEdad.setText("$dayOfMonth/$mesReal/$year  (Edad: $edadCalculadaDelUsuario años)")
            }, anioActual, mesActual, diaActual)

            // Evitamos que puedan elegir fechas del futuro (nadie nace mañana)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // ---> 3. Lógica del botón de Registro (Validaciones) <---
        btnRegistrarCuenta.setOnClickListener {
            // Prevenimos que por accidente toquen dos veces y se creen dos cuentas
            btnRegistrarCuenta.isEnabled = false
            btnRegistrarCuenta.postDelayed({ btnRegistrarCuenta.isEnabled = true }, 1000)

            val cedula = etCedula.text.toString().trim()
            val nombres = etNombres.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()
            val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

            // Limpiamos los errores anteriores antes de volver a evaluar
            tilCedula.error = null
            tilNombres.error = null
            tilEdad.error = null
            tilCorreo.error = null
            tilContrasena.error = null
            tilConfirmarContrasena.error = null

            // Validamos paso a paso. Si algo falla, mostramos el error y detenemos el proceso (return)
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

            // Validamos que ambas contraseñas escritas sean idénticas
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

            // ---> 4. Guardar en la Base de Datos <---
            // Empaquetamos todos los datos limpios en la entidad de usuario
            val nuevoUsuario = UserEntity(
                documentNumber = cedula,
                fullName = nombres,
                birthDate = fechaNacimientoParaGuardar,
                email = correo,
                password = contrasena
            )

            // Abrimos un hilo en segundo plano para no congelar la pantalla al guardar
            lifecycleScope.launch {
                val resultado = withContext(Dispatchers.IO) {
                    repository.registrarUsuario(nuevoUsuario)
                }

                // Evaluamos cómo nos fue con la base de datos
                resultado.onSuccess {
                    mostrarConfirmacionRegistro(nombres)
                }.onFailure {
                    // Si falla, suele ser porque la cédula ya existe (es un campo único en Room)
                    tilCedula.error = "Ya existe una cuenta con esta cédula"
                    etCedula.requestFocus()
                    btnRegistrarCuenta.isEnabled = true
                }
            }
        }

        // Cierra la pantalla de registro y regresa al Login
        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Muestra una alerta amigable confirmando que el usuario se creó correctamente.
     * Personaliza el mensaje dependiendo de la edad (si es adulto mayor, le da un mensaje especial).
     *
     * @param nombres El nombre de la persona recién registrada, para saludarla.
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
            .setCancelable(false) // Obliga al usuario a tocar el botón para cerrar la alerta
            .setPositiveButton("Ir a Iniciar Sesión") { _, _ ->
                finish() // Cierra el registro y vuelve automáticamente a la pantalla de Login
            }
            .show()
    }
}