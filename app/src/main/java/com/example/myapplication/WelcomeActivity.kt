package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * Actividad central (Dashboard) de la aplicación SaludTotal.
 * * Punto de entrada post-autenticación que orquesta la navegación hacia los módulos principales.
 * El rediseño sustituye menús ocultos por navegación inferior explícita (Bottom Navigation)
 * para reducir la carga cognitiva, y delega acciones nativas (llamadas) al OS para maximizar
 * la accesibilidad y seguridad del prototipo.
 */
class WelcomeActivity : AppCompatActivity() {

    // Constante de configuración: Teléfono oficial de soporte del Hospital
    private val telefonoAyuda = "18007258383"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // ==========================================
        // 1. VINCULACIÓN DE COMPONENTES DE INTERFAZ
        // ==========================================
        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        val btnAyuda = findViewById<MaterialButton>(R.id.btnAyuda)
        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)

        // Botones contenedores de la Barra de Navegación Inferior (Accesibilidad visual directa)
        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navAgendar = findViewById<LinearLayout>(R.id.navAgendar)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        // ==========================================
        // 2. RECUPERACIÓN DE ESTADO Y PERSONALIZACIÓN
        // ==========================================
        // Acceso a la persistencia local para personalizar la experiencia de usuario
        val sharedPreferences = getSharedPreferences("SaludTotalApp", MODE_PRIVATE)
        val nombreCompleto = sharedPreferences.getString("nombreGuardado", "Administrador") ?: "Administrador"

        // Extracción limpia del primer nombre para generar cercanía y evitar formatos rígidos
        val primerNombre = nombreCompleto.trim().substringBefore(" ")
        tvSaludo.text = "Hola, $primerNombre"

        // ==========================================
        // 3. ASIGNACIÓN DE EVENTOS (LISTENERS)
        // ==========================================

        // Flujo principal (Call to Action primario)
        btnAgendarCita.setOnClickListener {
            irAAgendarCita()
        }

        // Canal de soporte: Ejecuta la delegación de intenciones al OS
        btnAyuda.setOnClickListener {
            llamarLineaDeAyuda()
        }

        // --- Comportamiento de la Barra de Navegación Inferior ---

        // Estado inerte deliberado: Mitiga recargas cíclicas de la misma actividad
        navInicio.setOnClickListener { }

        // Redundancia de navegación: Ofrece múltiples caminos para la acción principal
        navAgendar.setOnClickListener {
            irAAgendarCita()
        }

        // Interrupción segura del ciclo de vida de la sesión
        navSalir.setOnClickListener {
            confirmarCierreDeSesion()
        }
    }

    // ==========================================
    // 4. MÉTODOS DE ORQUESTACIÓN Y LÓGICA
    // ==========================================

    /**
     * Transiciona el contexto hacia el flujo de agendamiento médico.
     */
    private fun irAAgendarCita() {
        val intent = Intent(this, SpecialtiesActivity::class.java)
        startActivity(intent)
    }

    /**
     * Levanta un modal preventivo para la destrucción de la sesión.
     * Patrón de diseño crucial: Previene deslogueos accidentales por toques erráticos
     * en la zona inferior de la pantalla (Bottom Nav).
     */
    private fun confirmarCierreDeSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir de tu cuenta?")
            .setPositiveButton("Sí, salir") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                // Sanitización del árbol de actividades (Task Stack)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null) // Cierra el modal sin ejecutar acción
            .show()
    }

    /**
     * Despliega un modal informativo previo a la invocación del marcador telefónico.
     * Utiliza [Intent.ACTION_DIAL] (implícito) en lugar de ACTION_CALL. Esto evita solicitar
     * permisos invasivos en tiempo de ejecución, permitiendo al usuario revisar el número
     * antes de emitir la llamada por la red móvil.
     */
    private fun llamarLineaDeAyuda() {
        AlertDialog.Builder(this)
            .setTitle("Línea de ayuda")
            .setMessage("Vamos a abrir el marcador de tu teléfono con el número de SaludTotal ya escrito. Solo debes tocar el botón de llamar.")
            .setPositiveButton("Llamar") { _, _ ->
                // Uri.parse formatea la cadena al estándar tel: esperado por el OS Android
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefonoAyuda"))
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}