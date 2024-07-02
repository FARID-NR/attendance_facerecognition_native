package com.example.absensiapp.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.absensiapp.R
import com.example.absensiapp.databinding.DateItemBinding
import java.text.ParsePosition
import java.time.MonthDay

class CalendarAdapter(
    private val calendarInterface: CalendarInterface,
    private val list: ArrayList<CalendarDate>
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {


    var pos:Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    inner class ViewHolder(private val binding: DateItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDateModel: CalendarDate, position: Int) {
            val calendarDate = binding.tvDateCalendar
            val calendarDay = binding.tvCalendarDay
            val cardView = binding.root
            if (pos == position) {
                calendarDateModel.isSelected = true
            }
            if (calendarDateModel.isSelected) {
                pos = -1
                calendarDate.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
                calendarDay.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primaryColor
                    )
                )
            } else {
                calendarDay.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primaryColor
                    )
                )

                calendarDate.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primaryColor
                    )
                )

                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )

            }
            calendarDate.text = calendarDateModel.calendarDate
            calendarDay.text = calendarDateModel.calendarDay
            binding.root.setOnClickListener {
                calendarInterface.onSelected(calendarDateModel, adapterPosition, calendarDate)
            }
        }
    }

    interface CalendarInterface {
        fun onSelected(calendarDate: CalendarDate, position: Int, day: TextView)
    }

    fun setPosition(pos: Int) {
        this.pos = pos
    }

    fun updateList(calendarList: ArrayList<CalendarDate>) {
        list.clear()
        list.addAll(calendarList)
        notifyDataSetChanged()
    }
}