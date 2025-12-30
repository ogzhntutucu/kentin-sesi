package io.github.thwisse.kentinsesi.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.thwisse.kentinsesi.R
import io.github.thwisse.kentinsesi.databinding.FragmentFilterPresetsBottomSheetBinding

@AndroidEntryPoint
class FilterPresetsBottomSheetFragment : BottomSheetDialogFragment() {

    private fun getStatusLabels(): Map<String, String> = mapOf(
        "new" to getString(R.string.status_new),
        "in_progress" to getString(R.string.status_in_progress),
        "resolved" to getString(R.string.status_resolved)
    )

    private var _binding: FragmentFilterPresetsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var adapter: FilterPresetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterPresetsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FilterPresetAdapter(
            onClick = { preset ->
                viewModel.applyPreset(preset.id)
                dismiss()
            },
            onLongClick = { preset ->
                val criteria = preset.criteria
                val statusLabels = getStatusLabels()

                val statusDisplay = criteria.statuses
                    .map { statusLabels[it] ?: it }

                val onlyMineDisplay = if (criteria.onlyMyPosts) getString(R.string.yes) else getString(R.string.no)

                val message = buildString {
                    appendLine("${getString(R.string.filter_districts_label)} ${criteria.districts.joinToString().ifBlank { getString(R.string.filter_all) }}")
                    appendLine("${getString(R.string.filter_categories_label)} ${criteria.categories.joinToString().ifBlank { getString(R.string.filter_all) }}")
                    appendLine("${getString(R.string.filter_statuses_label)} ${statusDisplay.joinToString().ifBlank { getString(R.string.filter_all) }}")
                    appendLine("${getString(R.string.filter_only_mine_label)} $onlyMineDisplay")
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preset.name)
                    .setMessage(message.trim())
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()
            },
            onMenuSetDefault = { preset ->
                viewModel.setDefaultPreset(preset.id)
            },
            onMenuDelete = { preset ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.filter_delete_title))
                    .setMessage(getString(R.string.filter_delete_message, preset.name))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        viewModel.deletePreset(preset.id)
                    }
                    .show()
            }
        )

        binding.rvPresets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPresets.adapter = adapter

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        viewModel.presets.observe(viewLifecycleOwner) { presets ->
            adapter.submitList(presets)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
