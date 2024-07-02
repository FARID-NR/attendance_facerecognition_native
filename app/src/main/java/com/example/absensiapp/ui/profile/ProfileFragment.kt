package com.example.absensiapp.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.absensiapp.databinding.FragmentNotificationsBinding
import com.example.absensiapp.ui.registrasi_face.RegistrasiFaceActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, start RegistrasiFaceActivity
                startRegistrasiFaceActivity()
            } else {
                // Permission denied, handle it here if needed
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.register
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val buttonRegister = binding.register
        buttonRegister.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Camera permission already granted, start RegistrasiFaceActivity
                    startRegistrasiFaceActivity()
                }

                else -> {
                    // Request CAMERA permission
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startRegistrasiFaceActivity() {
        val intent = Intent(activity, RegistrasiFaceActivity::class.java)
        startActivity(intent)
    }
}
