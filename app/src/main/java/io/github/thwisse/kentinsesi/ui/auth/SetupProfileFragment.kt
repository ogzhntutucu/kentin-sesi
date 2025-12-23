package io.github.thwisse.kentinsesi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.thwisse.kentinsesi.databinding.FragmentSetupProfileBinding
import io.github.thwisse.kentinsesi.ui.AuthActivity
import io.github.thwisse.kentinsesi.ui.MainActivity
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupProfileFragment : Fragment() {

    private var _binding: FragmentSetupProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupClickListeners()
        observeState()
    }

    private fun setupDropdowns() {
        // Hatay ilçeleri listesi
        val districts = listOf(
            "Altınözü", "Antakya", "Arsuz", "Belen", "Defne", "Dörtyol",
            "Erzin", "Hassa", "İskenderun", "Kırıkhan", "Kumlu",
            "Payas", "Reyhanlı", "Samandağ", "Yayladağı"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, districts)
        binding.actvDistrict.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val city = binding.actvCity.text.toString().trim() // "Hatay" sabit zaten
            val district = binding.actvDistrict.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.tilFullName.error = "İsim boş bırakılamaz"
                return@setOnClickListener
            } else {
                binding.tilFullName.error = null
            }

            if (district.isEmpty()) {
                binding.tilDistrict.error = "Lütfen ilçe seçiniz"
                return@setOnClickListener
            } else {
                binding.tilDistrict.error = null
            }

            // ViewModel'e verileri gönder
            viewModel.completeProfile(fullName, city, district)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProfileState.observe(viewLifecycleOwner) { resource ->

                    binding.progressBar.isVisible = resource is Resource.Loading
                    binding.btnSaveProfile.isEnabled = resource !is Resource.Loading

                    when (resource) {
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), "Profilin hazır!", Toast.LENGTH_SHORT).show()
                            // Burası AuthActivity'yi bitirip MainActivity'yi başlatır
                            navigateToMain()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Loading -> { }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        // AuthActivity'den MainActivity'ye geçiş
        (activity as? AuthActivity)?.let {
            val intent = Intent(it, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}