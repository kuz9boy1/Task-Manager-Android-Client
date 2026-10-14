package com.example.myapplication.widget
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dto.TaskResponseDto

import com.example.myapplication.model.Task

class TaskAdapter(
    private val tasks: List<TaskResponseDto>,
    private val onTaskClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvNumber)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvDeadline: TextView = view.findViewById(R.id.tvDeadline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvNumber.text = (position + 1).toString()
        holder.tvName.text = task.title
        holder.tvDescription.text = task.description.ifBlank { "—" }
        holder.tvDeadline.text = formatDate(task.dueDate)

        holder.itemView.setOnClickListener {
            onTaskClick(position)
        }
    }

    override fun getItemCount() = tasks.size

    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val parts = isoDate.split("-")
            if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }
}