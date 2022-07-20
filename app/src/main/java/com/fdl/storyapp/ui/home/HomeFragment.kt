package com.fdl.storyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.FragmentHomeBinding
import com.fdl.storyapp.databinding.PopupMenuBinding
import com.fdl.storyapp.model.UserModel
import com.fdl.storyapp.ui.LoadingStateAdapter
import com.fdl.storyapp.ui.StoryAdapter
import com.fdl.storyapp.ui.maps.MapsFragment
import com.fdl.storyapp.utilities.Utils
import com.fdl.storyapp.utilities.ViewModelFactory
import com.fdl.storyapp.utilities.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: UserModel

    private lateinit var adapter: StoryAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isFromOtherScreen = false

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        setupPopupMenu()
        adapter = StoryAdapter().apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0 && isFromOtherScreen.not()) {
                        binding.rvStory.smoothScrollToPosition(0)
                    }
                }
            })
        }

        val adapterWithLoading =
            adapter.withLoadStateFooter(footer = LoadingStateAdapter { adapter.retry() })
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvStory.layoutManager = layoutManager
        binding.rvStory.adapter = adapterWithLoading
        adapter.refresh()

        binding.swipeLayout.setOnRefreshListener {
            adapter.refresh()
            binding.swipeLayout.isRefreshing = false
        }

        wrapEspressoIdlingResource {
            lifecycleScope.launch {
                adapter.loadStateFlow.collect {
                    binding.loadingProgressBar.isVisible = (it.refresh is LoadState.Loading)
                    binding.tvEmpty.isVisible = it.source.refresh is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount < 1
                    if (it.refresh is LoadState.Error) {
                        Utils.showToast(
                            requireContext(),
                            (it.refresh as LoadState.Error).error.localizedMessage?.toString()
                                ?: getString(R.string.load_stories_failed)
                        )
                    }
                }
            }
        }

        binding.btnAddStory.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostStoryFragment())
        }

        binding.btnMap.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToMapsFragment(
                    action = MapsFragment.ACTION_STORIES
                )
            )
        }

        setFragmentResultListener(MapsFragment.KEY_RESULT) { _, bundle ->
            isFromOtherScreen = bundle.getBoolean(KEY_FROM_OTHER_SCREEN, false)
        }
    }

    private fun setupPopupMenu() {
        val popupBinding = PopupMenuBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true
            elevation = 10F
        }

        binding.btnMenu.setOnClickListener { btn ->
            popupWindow.showAsDropDown(btn, 0, -btn.height)
        }

        popupBinding.tvMenuLanguages.setOnClickListener {
            popupWindow.dismiss()
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        popupBinding.tvMenuLogout.setOnClickListener {
            popupWindow.dismiss()
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        viewModel.userModel.observe(viewLifecycleOwner) { userModel ->
            if (userModel?.isLoggedIn == false) {
                findNavController().navigateUp()
            }
            this.user = userModel
        }

        viewModel.stories.observe(viewLifecycleOwner) { stories ->
            adapter.submitData(lifecycle, stories)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Utils.showToast(requireContext(), message)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { state ->
            showLoading(state)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_FROM_OTHER_SCREEN = "other_screen"
    }
}