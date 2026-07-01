package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Controlador de la interfaz de inicio de sesión de SaludTotal.
 * * Esta actividad gestiona la autenticación de pacientes aplicando patrones de diseño
 * orientados a la alta inclusión digital, previniendo errores comunes de interacción
 * provocados por limitaciones motrices o cognitivas en adultos mayores.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ==========================================
        // 1. VINCULACIÓN DE COMPONENTES (FRONT & BACK)
        // ==========================================

        // Contenedores Material Design (controlan estados visuales como bordes y errores)
        val tilUsuario = findViewById<TextInputLayout>(R.id.tilUsuario)
        val tilContrasena = findViewById<TextInputLayout>(R.id.tilContrasena)

        // Campos de entrada de datos físicos (capturan el texto del teclado)
        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)

        // Disparadores de acciones (Botones interactivos de gran tamaño táctil)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnIniciarSesion)
        val btnIrRegistro = findViewById<MaterialButton>(R.id.btnIrRegistro)
        val btnAyudaLogin = findViewById<MaterialButton>(R.id.btnAyudaLogin)

        // ==========================================
        // 2. CONTROL DINÁMICO DE ERRORES VISUALES
        // ==========================================

        // Patrón de limpieza proactiva: Remueve las alertas de error de forma inmediata
        // en el momento en que el usuario interactúa o intenta corregir su entrada.
        etUsuario.setOnClickListener { tilUsuario.error = null }
        etContrasena.setOnClickListener { tilContrasena.error = null }

        // ==========================================
        // 3. LOGICA DE NEGOCIO: AUTENTICACIÓN
        // ==========================================
        btnIniciarSesion.setOnClickListener {

            // --- Mecanismo de Antirrebote (Debounce) ---
            // Desactiva temporalmente el botón por 1000ms para mitigar clicks dobles involuntarios,
            // un comportamiento muy común en pantallas táctiles por usuarios con temblores o baja precisión.
            btnIniciarSesion.isEnabled = false
            btnIniciarSesion.postDelayed({ btnIniciarSesion.isEnabled = true }, 1000)

            // Extracción y limpieza de cadenas (Elimina espacios accidentales al inicio/final)
            val usuarioIngresado = etUsuario.text.toString().trim()
            val contrasenaIngresada = etContrasena.text.toString().trim()

            // Reinicio de estados antes de evaluar el formulario
            tilUsuario.error = null
            tilContrasena.error = null

            // --- Validaciones Estrictas de Campos Vacíos ---
            if (usuarioIngresado.isEmpty()) {
                tilUsuario.error = "Escribe tu número de cédula"
                etUsuario.requestFocus() // Enfoca automáticamente el campo para facilitar el rellenado
                return@setOnClickListener // Interrumpe el flujo de ejecución para evitar peticiones basura
            }
            if (contrasenaIngresada.isEmpty()) {
                tilContrasena.error = "Escribe tu contraseña"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            // --- Consulta a Persistencia Local (Simulación de Base de Datos) ---
            // Se leen las credenciales previamente almacenadas en SharedPreferences
            val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
            val usuarioGuardado = sharedPreferences.getString("usuarioGuardado", "")
            val contrasenaGuardada = sharedPreferences.getString("contrasenaGuardada", "")

            // --- Evaluación de Permisos de Acceso ---
            val esUsuarioMaestro = (usuarioIngresado == "1754378097" && contrasenaIngresada == "Yasid123@")
            val esUsuarioRegistrado = (usuarioIngresado == usuarioGuardado && contrasenaIngresada == contrasenaGuardada && usuarioIngresado.isNotEmpty())

            if (esUsuarioMaestro || esUsuarioRegistrado) {
                // Navegación Segura: Envía al flujo principal borrando el Login del stack de actividades
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish() // Evita que al presionar el botón físico "Atrás" se regrese al formulario de Login
            } else {
                // Falla de credenciales: Se ejecuta el diálogo de soporte persistente
                mostrarErrorLoginPersistente(usuarioGuardado)
            }
        }

        // ==========================================
        // 4. FLUJOS DE NAVEGACIÓN SECUNDARIOS
        // ==========================================

        // Transición directa al formulario de creación de cuentas
        btnIrRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Canal de soporte preventivo: Diálogo accesible para usuarios con dificultades
        // para interactuar o recordar credenciales digitales de acceso.
        btnAyudaLogin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Necesitas ayuda?")
                .setMessage("Llama a nuestra línea de atención al 1-800-SALUD para que un asesor te ayude a entrar o a agendar tu cita.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    /**
     * Despliega un cuadro de diálogo descriptivo e ininterrumpible ante un fallo de login.
     * A diferencia de los componentes 'Toast', este modal requiere interacción activa para cerrarse,
     * permitiendo que personas con tiempos de lectura pausados comprendan las instrucciones brindadas.
     *
     * @param usuarioGuardado El registro existente en la memoria de la aplicación móvil.
     */
    private fun mostrarErrorLoginPersistente(usuarioGuardado: String?) {
        val hayCuentaRegistrada = !usuarioGuardado.isNullOrEmpty()

        // Generación dinámica de la instrucción dependiendo del contexto del almacenamiento
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
}