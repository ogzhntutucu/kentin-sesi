package io.github.thwisse.kentinsesi.ui.post

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import io.github.thwisse.kentinsesi.data.model.Post
import io.github.thwisse.kentinsesi.data.model.PostStatus
import io.github.thwisse.kentinsesi.databinding.FragmentPostDetailBinding
import io.github.thwisse.kentinsesi.util.Resource
import io.github.thwisse.kentinsesi.util.ValidationUtils
import androidx.core.view.isVisible
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

@AndroidEntryPoint
class PostDetailFragment : Fragment(io.github.thwisse.kentinsesi.R.layout.fragment_post_detail), OnMapReadyCallback {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    // ViewModel Tanımı
    private val viewModel: PostDetailViewModel by viewModels()
    private val commentAdapter = CommentAdapter()

    private var postLocation: LatLng? = null
    private var currentPostId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostDetailBinding.bind(view)

        val post = arguments?.getParcelable<Post>("post")

        if (post != null) {
            currentPostId = post.id
            // ViewModel'e post bilgisini set et (yetkili kontrolü için gerekli)
            viewModel.setPost(post)
            
            setupViews(post)
            setupMap(post)
            setupComments()
            setupOfficialActions() // Yetkili butonlarını ayarla

            // Yorumları Çek
            viewModel.getComments(post.id)

            // Menü kurulumu - Post sahibi için
            if (viewModel.currentUserId == post.authorId) {
                setupOwnerMenu()
            }
        }

        // --- DEĞİŞİKLİK: binding.toolbar kodları SİLİNDİ ---
        // Geri butonu artık MainActivity'deki setSupportActionBar sayesinde otomatik çalışacak.

        observeOwnerActions()
    }

    private fun setupOwnerMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(io.github.thwisse.kentinsesi.R.menu.menu_post_detail, menu)
                
                // Yetkili kontrolü - Sadece yetkili kullanıcılar durum güncelleyebilir
                viewModel.canUpdateStatus.observe(viewLifecycleOwner) { canUpdate ->
                    menu.findItem(io.github.thwisse.kentinsesi.R.id.action_resolve)?.isVisible = canUpdate
                }
                
                // Silme kontrolü - Post sahibi veya admin silebilir
                viewModel.canDeletePost.observe(viewLifecycleOwner) { canDelete ->
                    menu.findItem(io.github.thwisse.kentinsesi.R.id.action_delete)?.isVisible = canDelete
                }
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    io.github.thwisse.kentinsesi.R.id.action_resolve -> {
                        currentPostId?.let { viewModel.markAsResolved(it) }
                        true
                    }
                    io.github.thwisse.kentinsesi.R.id.action_delete -> {
                        currentPostId?.let { viewModel.deletePost(it) }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeOwnerActions() {
        viewModel.deletePostState.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Bildirim silindi.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { }
            }
        }

        viewModel.updateStatusState.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Durum güncellendi: Çözüldü!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { }
            }
        }
    }

    private fun setupViews(post: Post) {
        binding.apply {
            tvDetailTitle.text = post.title
            tvDetailDescription.text = post.description
            tvDetailCategory.text = post.category
            tvDetailDistrict.text = post.district ?: "İlçe Yok"
            ivDetailImage.load(post.imageUrl) { crossfade(true) }
        }
    }

    // ... setupMap, setupComments, onMapReady ve onDestroyView AYNI KALIYOR ...
    // Sadece yer kaplamasın diye tekrar yazmıyorum, o kısımlarda değişiklik yok.

    private fun setupMap(post: Post) {
        if (post.location != null) {
            postLocation = LatLng(post.location.latitude, post.location.longitude)
            val mapFragment = childFragmentManager.findFragmentById(io.github.thwisse.kentinsesi.R.id.mapFragment) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }
    }

    private fun setupComments() {
        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            
            // ValidationUtils kullanarak yorum kontrolü
            val commentError = ValidationUtils.getValidationError("comment", text)
            
            when {
                text.isEmpty() -> {
                    binding.etComment.error = "Yorum boş olamaz"
                    binding.etComment.requestFocus()
                    return@setOnClickListener
                }
                commentError != null -> {
                    binding.etComment.error = commentError
                    binding.etComment.requestFocus()
                    return@setOnClickListener
                }
                currentPostId == null -> {
                    Toast.makeText(requireContext(), "Post ID bulunamadı", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            
            viewModel.addComment(currentPostId!!, text)
            binding.etComment.text.clear()
            binding.etComment.error = null
        }

        viewModel.commentsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> commentAdapter.submitList(resource.data)
                is Resource.Error -> Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { }
            }
        }
        
        viewModel.addCommentState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> {
                    binding.etComment.error = resource.message
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Yorum başarıyla eklendi, yorumlar otomatik yenilenecek
                }
                is Resource.Loading -> { }
            }
        }
    }
    
    /**
     * Yetkili kullanıcılar için butonları ayarla
     */
    private fun setupOfficialActions() {
        // Yetkili kontrolü için LiveData'ları observe et
        viewModel.canUpdateStatus.observe(viewLifecycleOwner) { canUpdate ->
            // Yetkili butonlarını göster/gizle
            // Bu butonlar layout'ta olmalı, şimdilik sadece menüde gösteriyoruz
        }
        
        // Post durumunu göster
        viewModel.currentPost.observe(viewLifecycleOwner) { post ->
            post?.let {
                // Post durumunu göster (opsiyonel)
                val statusText = when (it.statusEnum) {
                    PostStatus.NEW -> "Yeni"
                    PostStatus.IN_PROGRESS -> "İşleme Alındı"
                    PostStatus.RESOLVED -> "Çözüldü"
                    PostStatus.REJECTED -> "Reddedildi"
                }
                // Eğer layout'ta status gösterilecek bir TextView varsa:
                // binding.tvStatus.text = statusText
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        postLocation?.let { location ->
            googleMap.addMarker(MarkerOptions().position(location).title("Sorun Konumu"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}