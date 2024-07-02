package com.example.absensiapp.ui.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.example.absensiapp.R
import com.example.absensiapp.ui.izin.IzinActivity
import com.example.absensiapp.databinding.FragmentHomeBinding
import com.example.absensiapp.model.response.home.GetUserResponse
import com.example.absensiapp.ui.modal.ModalFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), HomeContract.View {

    private var _binding: FragmentHomeBinding? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var presenter: HomePresenter

    var progressDialog : Dialog? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        presenter = HomePresenter(this)
        presenter.getHome()

        binding.register.setOnClickListener {
            ModalFragment().show(parentFragmentManager, "modalIzin")
        }

        // Tambahkan OnClickListener ke textViewIzin atau elemen lainnya yang ingin Anda gunakan
        binding.izin.setOnClickListener {
            // Implementasi intent untuk berpindah ke IzinPage
            val intent = Intent(activity, IzinActivity::class.java)
            startActivity(intent)
        }

        // Mulai update waktu dan tanggal
        startUpdatingTimeAndDate()

    }

    private fun initView() {
        progressDialog = Dialog(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_loader, null)

        progressDialog?.let {
            it.setContentView(dialogLayout)
            it.setCancelable(false)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun startUpdatingTimeAndDate() {
        val updateTask = object : Runnable {
            override fun run() {
                updateTimeAndDate()
                handler.postDelayed(this, 60000) // Update setiap 60 detik
            }
        }
        handler.post(updateTask)
    }

    private fun updateTimeAndDate() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())

        binding.timeText.text = timeFormat.format(currentTime)
        binding.dateText.text = dateFormat.format(currentTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }

    override fun onHomeSucces(homeResponse: GetUserResponse) {
        // Set text pada textView atau elemen UI lainnya di sini
        binding.nameText.text = "Hello, ${homeResponse.name}"
    }

    override fun onHomeFailed(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        progressDialog?.show()
    }

    override fun dismissLoading() {
        progressDialog?.dismiss()
    }
}