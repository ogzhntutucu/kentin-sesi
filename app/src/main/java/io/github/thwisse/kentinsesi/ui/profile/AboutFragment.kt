package io.github.thwisse.kentinsesi.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.thwisse.kentinsesi.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        // Hide bottom navigation when About screen is shown
        (activity as? io.github.thwisse.kentinsesi.ui.MainActivity)?.hideBottomNavigation()
    }
    
    override fun onPause() {
        super.onPause()
        // Show bottom navigation when leaving About screen
        (activity as? io.github.thwisse.kentinsesi.ui.MainActivity)?.showBottomNavigation()
    }

    private fun setupClickListeners() {
        // GitHub link
        binding.llGithubLink.setOnClickListener {
            openUrl("https://github.com/zibidiSoft")
        }

        // Email link
        binding.llEmailLink.setOnClickListener {
            openEmail("zibidisoft@protonmail.com")
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
