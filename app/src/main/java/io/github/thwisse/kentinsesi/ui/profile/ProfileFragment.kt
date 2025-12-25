package io.github.thwisse.kentinsesi.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        setupUserInfo()
        setupRecyclerView()
        setupObservers()

        // Çıkış Butonu
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
            // Auth ekranına at ve Activity geçmişini temizle
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupUserInfo() {
        val user = viewModel.currentUser
        binding.tvUserEmail.text = user?.email ?: "Kullanıcı"
    }

    private fun setupRecyclerView() {
        val currentUserId = viewModel.currentUser?.uid ?: ""

        postAdapter = PostAdapter(
            currentUserId = currentUserId,
            onUpvoteClick = { /* Profilde beğeni işlemi opsiyonel */ },
            onItemClick = { post ->
                // Karta tıklayınca Detay'a git
                val bundle = Bundle().apply { putParcelable("post", post) }
                // SENİN DÜZELTTİĞİN ID BURAYA YAZILDI:
                findNavController().navigate(R.id.action_nav_profile_to_postDetailFragment, bundle)
            }
        )
        binding.rvUserPosts.adapter = postAdapter
    }

    private fun setupObservers() {
        viewModel.userPosts.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Success -> postAdapter.submitList(resource.data)
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { }
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