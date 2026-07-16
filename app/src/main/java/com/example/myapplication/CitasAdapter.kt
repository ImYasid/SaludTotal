package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.entity.AppointmentEntity
import com.google.android.material.button.MaterialButton

class CitasAdapter(
    private var listaCitas: List<AppointmentEntity>,
    private val onEliminarClick: (AppointmentEntity) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidad)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctor)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
        val btnEliminar: MaterialButton = view.findViewById(R.id.btnEliminarCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        // Mostramos la especialidad (Ej: "Cardiología")
        holder.tvEspecialidad.text = cita.especialidadNombre

        // Unimos el nombre del doctor y la dirección con un puntito separador
        holder.tvDoctor.text = "${cita.doctorNombre} • ${cita.direccion}"

        // Mostramos la fecha y hora
        holder.tvFechaHora.text = "${cita.fechaTexto} - ${cita.horaTexto}"

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(cita)
        }
    }

    override fun getItemCount(): Int = listaCitas.size

    fun actualizarLista(nuevaLista: List<AppointmentEntity>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}