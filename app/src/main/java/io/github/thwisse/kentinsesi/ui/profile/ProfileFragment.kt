package io.github.thwisse.kentinsesi.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.thwisse.kentinsesi.R
import io.github.thwisse.kentinsesi.databinding.FragmentProfileBinding
import io.github.thwisse.kentinsesi.ui.AuthActivity // <-- 1. EKSİK IMPORT EKLENDİ
import io.github.thwisse.kentinsesi.ui.home.PostAdapter
import io.github.thwisse.kentinsesi.util.Resource

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupMenu()

        setupUserInfo()
        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()

        // Admin Paneli Butonu (sadece admin kullanıcılar görebilir)
        viewModel.userProfile.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val user = resource.data
                binding.btnAdminPanel.visibility = if (user?.isAdmin == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

        binding.btnAdminPanel.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_adminPanelFragment)
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_theme -> {
                        showThemeDialog()
                        true
                    }
                    R.id.action_logout -> {
                        doLogout()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showThemeDialog() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val current = prefs.getString("theme_mode", "system")

        val options = arrayOf("Sistem", "Açık", "Koyu")
        val values = arrayOf("system", "light", "dark")
        val checkedIndex = values.indexOf(current).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(requireContext())
            .setTitle("Tema")
            .setSingleChoiceItems(options, checkedIndex) { dialog, which ->
                val selected = values.getOrNull(which) ?: "system"
                prefs.edit().putString("theme_mode", selected).apply()

                when (selected) {
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }

                requireActivity().recreate()
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun doLogout() {
        viewModel.signOut()
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupUserInfo() {
        // Email'i Firebase User'dan göster
        val firebaseUser = viewModel.currentUser
        binding.tvUserEmail.text = firebaseUser?.email ?: "Kullanıcı"
        
        // Profil bilgilerini (fullName, city, district) User model'den göster
        viewModel.userProfile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    if (user != null) {
                        // Kullanıcı adını göster
                        binding.tvUserName.text = user.fullName.ifEmpty { "İsim Belirtilmemiş" }
                        
                        // Şehir ve ilçe bilgisini birleştirerek göster
                        val locationText = when {
                            user.city.isNotEmpty() && user.district.isNotEmpty() -> 
                                "${user.city}, ${user.district}"
                            user.city.isNotEmpty() -> user.city
                            user.district.isNotEmpty() -> user.district
                            else -> "Konum Belirtilmemiş"
                        }
                        binding.tvUserLocation.text = locationText
                    } else {
                        // Data null ise varsayılan değerleri göster
                        binding.tvUserName.text = "Kullanıcı"
                        binding.tvUserLocation.text = "Konum Belirtilmemiş"
                    }
                }
                is Resource.Error -> {
                    // Hata durumunda varsayılan değerleri göster
                    binding.tvUserName.text = "Kullanıcı"
                    binding.tvUserLocation.text = "Konum Belirtilmemiş"
                }
                is Resource.Loading -> {
                    // Loading durumu
                }
            }
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onItemClick = { post ->
                // Karta tıklayınca Detay'a git (sadece post ID gönder)
                val bundle = Bundle().apply { putString("postId", post.id) }
                findNavController().navigate(R.id.action_nav_profile_to_postDetailFragment, bundle)
            }
        )
        binding.rvUserPosts.adapter = postAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAll()
        }
    }
    
    private fun setupObservers() {
        // UserPosts observer - SwipeRefresh'i kontrol et
        viewModel.userPosts.observe(viewLifecycleOwner) { resource ->
            // Loading durumunu SwipeRefresh'e bildir
            binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading
            
            when(resource) {
                is Resource.Success -> {
                    postAdapter.submitList(resource.data)
                    // Başarılı olunca refresh'i durdur
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    // Hata olsa bile refresh'i durdur
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is Resource.Loading -> { }
            }
        }
        
        // UserProfile observer - Hem profil hem postlar yüklenene kadar refresh göster
        viewModel.userProfile.observe(viewLifecycleOwner) { resource ->
            // Profil yüklenirken de refresh göster (userPosts loading değilse)
            if (resource is Resource.Loading && viewModel.userPosts.value !is Resource.Loading) {
                binding.swipeRefreshLayout.isRefreshing = true
            } else if (resource !is Resource.Loading && viewModel.userPosts.value !is Resource.Loading) {
                // Her ikisi de yüklendiğinde refresh'i durdur
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.totalPostsCount.observe(viewLifecycleOwner) { count ->
            binding.tvPostCount.text = count.toString()
        }
        viewModel.resolvedPostsCount.observe(viewLifecycleOwner) { count ->
            binding.tvResolvedCount.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}