package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.google.android.material.button.MaterialButton

/**
 * Adaptador para el RecyclerView encargado de renderizar la lista de citas médicas agendadas.
 * Conecta los datos de la base de datos (Room) con el diseño visual de cada tarjeta de cita.
 *
 * @param listaCitas Lista inicial de objetos [AppointmentEntity] que se mostrarán en pantalla.
 * @param onEliminarClick Función lambda (callback) que se ejecuta cuando el usuario presiona
 * el botón de eliminar en una cita específica. Devuelve la entidad de la cita seleccionada.
 */
class CitasAdapter(
    private var listaCitas: List<AppointmentEntity>,
    private val onEliminarClick: (AppointmentEntity) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    /**
     * Patrón ViewHolder que inicializa y retiene las referencias a las vistas individuales
     * de cada elemento de la lista (layout `item_cita.xml`). Esto mejora el rendimiento
     * al evitar llamadas constantes a `findViewById`.
     *
     * @param view La vista inflada que representa una única tarjeta de cita.
     */
    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidad)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctor)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
        val btnEliminar: MaterialButton = view.findViewById(R.id.btnEliminarCita)
    }

    /**
     * Crea nuevas instancias de [CitaViewHolder] cuando el RecyclerView lo requiere.
     * Infla el diseño XML de la tarjeta de cita para que pueda ser poblado de datos.
     *
     * @param parent El ViewGroup al que se añadirá la nueva vista tras ser enlazada.
     * @param viewType El tipo de vista del nuevo elemento (útil si hubiera múltiples diseños).
     * @return Una nueva instancia de [CitaViewHolder] que contiene la vista inflada.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    /**
     * Enlaza los datos de un elemento específico del conjunto de datos con las vistas
     * retenidas en el [CitaViewHolder].
     *
     * @param holder El ViewHolder que debe ser actualizado para representar el contenido.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        // Mostramos la especialidad (Ej: "Cardiología")
        holder.tvEspecialidad.text = cita.especialidadNombre

        // Unimos el nombre del doctor y la dirección con un separador visual (viñeta)
        holder.tvDoctor.text = "${cita.doctorNombre} • ${cita.direccion}"

        // Mostramos la fecha y hora de la cita
        holder.tvFechaHora.text = "${cita.fechaTexto} - ${cita.horaTexto}"

        // Asigna la acción de eliminar, delegándola a la función pasada por parámetro
        holder.btnEliminar.setOnClickListener {
            onEliminarClick(cita)
        }
    }

    /**
     * Devuelve la cantidad total de elementos en el conjunto de datos que posee el adaptador.
     *
     * @return Número entero que representa el tamaño de la lista de citas.
     */
    override fun getItemCount(): Int = listaCitas.size

    /**
     * Actualiza el conjunto de datos del adaptador con una nueva lista y notifica al RecyclerView
     * para que refresque la interfaz gráfica de usuario.
     *
     * @param nuevaLista La lista actualizada de citas médicas obtenidas de la base de datos.
     */
    fun actualizarLista(nuevaLista: List<AppointmentEntity>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}