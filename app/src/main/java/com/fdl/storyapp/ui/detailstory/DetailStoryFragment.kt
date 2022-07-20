package com.fdl.storyapp.ui.detailstory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fdl.storyapp.databinding.FragmentDetailStoryBinding

class DetailStoryFragment: Fragment() {

    private var _binding: FragmentDetailStoryBinding? = null
    private val binding get() = _binding!!

    val args: DetailStoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val story = args.story

        binding.apply {
            tvName.text = story.name
            tvDate.text = story.createdAt
            tvDesc.text = story.caption
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        Glide.with(requireContext())
            .load(story.imageUrl)
            .into(binding.ivImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}