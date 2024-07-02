    package com.example.absensiapp.ui.history

    import android.app.Dialog
    import android.content.ContentValues.TAG
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import com.example.absensiapp.databinding.FragmentHistoryBinding
    import com.example.absensiapp.model.response.history.HistoryResponse
    import com.example.absensiapp.ui.home.HomePresenter
    import com.example.absensiapp.utils.CalendarAdapter
    import com.example.absensiapp.utils.CalendarDate
    import com.google.android.material.datepicker.MaterialDatePicker
    import java.text.ParseException
    import java.text.SimpleDateFormat
    import java.util.*

    class HistoryFragment : Fragment(), CalendarAdapter.CalendarInterface, HistoryContract.View {

        private var _binding: FragmentHistoryBinding? = null
        private val binding get() = _binding!!
        private val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        private val cal = Calendar.getInstance(Locale.getDefault())
        private var mStartD: Date? = null
        private var formattedDate: String = "" // Variable to store formatted date

        private val calendarAdapter = CalendarAdapter(this, arrayListOf())
        private val calendarList = ArrayList<CalendarDate>()

        private lateinit var presenter: HistoryPresenter

        var progressDialog : Dialog? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            _binding = FragmentHistoryBinding.inflate(inflater, container, false)
            val root: View = binding.root

            presenter = HistoryPresenter(this)

            // Inisialisasi formattedDate ke tanggal hari ini jika belum diatur
            if (formattedDate.isEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                formattedDate = sdf.format(Date())
            }

            presenter.getHistory(tanggal = formattedDate)

            init()
            initCalendar()

            return root
        }

        private fun init() {
            binding.apply {
                monthYearPicker.text = sdf.format(cal.time)
                calendarView.setHasFixedSize(true)
                calendarView.adapter = calendarAdapter
                monthYearPicker.setOnClickListener {
                    displayDatePicker()
                }
            }
        }

        private fun initCalendar() {
            mStartD = Date()
            cal.time = Date()
            getDates()
        }

        private fun displayDatePicker() {
            val materialDateBuilder: MaterialDatePicker.Builder<Long> = MaterialDatePicker.Builder.datePicker()
            materialDateBuilder.setTitleText("Select Date")
            val materialDatePicker = materialDateBuilder.build()
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            materialDatePicker.addOnPositiveButtonClickListener {
                try {
                    mStartD = Date(it)
                    binding.monthYearPicker.text = sdf.format(mStartD)
                    cal.time = Date(it)
                    getDates()
                    saveSelectedDate(it)
                } catch (e: ParseException) {
                    Log.e(TAG, "displayDatePicker: ${e.message}")
                }
            }
        }

        private fun saveSelectedDate(selectedDateMillis: Long) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date(selectedDateMillis)
            formattedDate = sdf.format(date)
            presenter.getHistory(formattedDate) // Pass the formatted date to presenter
        }

        private fun getDates() {
            val dateList = ArrayList<CalendarDate>()
            val dates = ArrayList<Date>()
            val monthCalendar = cal.clone() as Calendar
            val maxDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

            while (dates.size < maxDayInMonth) {
                dates.add(monthCalendar.time)
                dateList.add(CalendarDate(monthCalendar.time))
                monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            calendarList.clear()
            calendarList.addAll(dateList)
            calendarAdapter.updateList(calendarList)

            for (item in dateList.indices) {
                if (dateList[item].date == mStartD) {
                    calendarAdapter.setPosition(item)
                    binding.calendarView.scrollToPosition(item)
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        override fun onSelected(calendarDate: CalendarDate, position: Int, day: TextView) {
            calendarList.forEachIndexed { index, calendarDateModel ->
                calendarDate.isSelected = index == position
            }
            calendarAdapter.updateList(calendarList)
        }

        override fun onHistorySucces(historyResponse: HistoryResponse) {
            val firstData = historyResponse.data.firstOrNull()
            val timeIn = historyResponse.data.first().timeIn
            val timeOut = historyResponse.data.first().timeOut

            println("ini adalah ${timeIn} dan $timeOut")

            if (timeIn != "null" && timeOut != "null") {
                val latlonIn = firstData?.latlonIn
                val latlonOut = firstData?.latlonOut

                // Parse latlonIn
                val latlonInParts = latlonIn?.split(',') ?: listOf()
                val latitudeIn = latlonInParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val longitudeIn = latlonInParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0

                // Parse latlonOut
                val latlonOutParts = latlonOut?.split(',') ?: listOf()
                val latitudeOut = latlonOutParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val longitudeOut = latlonOutParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0

                binding.jamDatang.text = firstData?.timeIn
                binding.dateDatang.text = firstData?.date
                binding.longitudeDatang.text = longitudeIn.toString()
                binding.latitudeDatang.text = latitudeIn.toString()

                binding.jamPulang.text = firstData?.timeOut.toString() ?: 0.toString()
                binding.longitudePulang.text = longitudeOut.toString()
                binding.latitudePulang.text = latitudeOut.toString()
                binding.datePulang.text = firstData?.date

                // Hide no data message if data is available
                binding.noDataLayout.visibility = View.GONE
                // Show attendance layout
                binding.listAttendance.visibility = View.VISIBLE
            } else if (timeIn != null && timeOut == 0.toString()) {
                val latlonIn = firstData?.latlonIn
                val latlonOut = firstData?.latlonOut

                // Parse latlonIn
                val latlonInParts = latlonIn?.split(',') ?: listOf()
                val latitudeIn = latlonInParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val longitudeIn = latlonInParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0

                // Parse latlonOut
                val latlonOutParts = latlonOut?.split(',') ?: listOf()
                val latitudeOut = latlonOutParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val longitudeOut = latlonOutParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0

                binding.jamDatang.text = firstData?.timeIn
                binding.dateDatang.text = firstData?.date
                binding.longitudeDatang.text = longitudeIn.toString()
                binding.latitudeDatang.text = latitudeIn.toString()

                binding.jamPulang.text = firstData?.timeOut.toString()
                binding.longitudePulang.text = longitudeOut.toString()
                binding.latitudePulang.text = latitudeOut.toString()
                binding.datePulang.text = firstData?.date

                // Hide no data message if data is available
                binding.noDataLayout.visibility = View.GONE
                // Show attendance layout
                binding.listAttendance.visibility = View.VISIBLE
                binding.jamPulangNull.visibility = View.VISIBLE
            } else {
                // Handle case where data.firstOrNull() returns null
                // This might happen if historyResponse.data is empty
                // You can show an error message or handle it according to your app's logic
                // Here, show no data message and hide attendance layout
                binding.noDataLayout.visibility = View.VISIBLE
                binding.listAttendance.visibility = View.GONE
            }
        }

        override fun onHistoryFailed(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "History fetch failed: $message")
            // Show no data message and hide attendance layout on failure
            binding.noDataLayout.visibility = View.VISIBLE
            binding.jamPulangNull.visibility = View.GONE
            binding.listAttendance.visibility = View.GONE

            // Set visibility of noDataText to visible
            binding.noDataText.visibility = View.VISIBLE
        }


        override fun showLoading() {
            progressDialog?.show()
        }

        override fun dismissLoading() {
            progressDialog?.dismiss()
        }
    }
