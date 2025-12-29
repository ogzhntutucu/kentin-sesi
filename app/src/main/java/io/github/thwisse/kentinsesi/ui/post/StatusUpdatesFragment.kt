package io.github.thwisse.kentinsesi.ui.post

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.thwisse.kentinsesi.R
import io.github.thwisse.kentinsesi.databinding.FragmentStatusUpdatesBinding
import io.github.thwisse.kentinsesi.util.Resource

@AndroidEntryPoint
class StatusUpdatesFragment : Fragment(R.layout.fragment_status_updates) {

    private var _binding: FragmentStatusUpdatesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels()
    private val adapter = StatusUpdateAdapter()

    private var currentPostId: String? = null
    private var currentPost: io.github.thwisse.kentinsesi.data.model.Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatusUpdatesBinding.bind(view)

        // PostId'yi al
        currentPostId = arguments?.getString("postId")
        if (currentPostId == null) {
            Toast.makeText(requireContext(), "Post ID bulunamadı", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()

        // İlk yükleme
        viewModel.loadStatusUpdates(currentPostId!!)
        
        // Post bilgisini de yükle (menu için gerekli)
        viewModel.loadPostById(currentPostId!!)
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: android.view.MenuInflater) {
        inflater.inflate(io.github.thwisse.kentinsesi.R.menu.menu_status_updates, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            io.github.thwisse.kentinsesi.R.id.action_update_status -> {
                // Post sahibi veya yetkili kontrolü
                val user = viewModel.currentUser.value
                val post = viewModel.currentPost.value
                val canUpdate = (user?.role == "official" || user?.role == "admin") || 
                               (post?.authorId == user?.uid)
                
                if (canUpdate) {
                    showUpdateStatusDialog()
                } else {
                    Toast.makeText(requireContext(), "Durum güncellemek için yetkiniz yok", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showUpdateStatusDialog() {
        val currentPostStatus = viewModel.currentPost.value?.statusEnum ?: io.github.thwisse.kentinsesi.data.model.PostStatus.NEW
        
        val bottomSheet = UpdateStatusBottomSheet.newInstance(currentPostStatus)
        bottomSheet.setOnStatusUpdateListener { status, note ->
            viewModel.addStatusUpdate(status, note)
        }
        bottomSheet.show(childFragmentManager, "UpdateStatusBottomSheet")
    }
    
    override fun onResume() {
        super.onResume()
        // Bottom navigation'ı gizle
        (activity as? io.github.thwisse.kentinsesi.ui.MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            io.github.thwisse.kentinsesi.R.id.bottom_nav_view
        )?.visibility = View.GONE
    }
    
    override fun onPause() {
        super.onPause()
        // Bottom navigation'ı geri göster
        (activity as? io.github.thwisse.kentinsesi.ui.MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            io.github.thwisse.kentinsesi.R.id.bottom_nav_view
        )?.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        binding.rvStatusUpdates.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            currentPostId?.let {
                viewModel.loadStatusUpdates(it)
            }
        }
    }

    private fun setupObservers() {
        viewModel.statusUpdates.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    val updates = resource.data ?: emptyList()
                    
                    if (updates.isEmpty()) {
                        binding.tvEmptyState.isVisible = true
                        binding.rvStatusUpdates.isVisible = false
                    } else {
                        binding.tvEmptyState.isVisible = false
                        binding.rvStatusUpdates.isVisible = true
                        // Önce null gönder, sonra listeyi gönder - arrow visibility için gerekli
                        adapter.submitList(null)
                        adapter.submitList(updates)
                    }
                }
                is Resource.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        // İlk yükleme için loading indicator buraya eklenebilir
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
