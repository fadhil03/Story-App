package com.fdl.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.FragmentRegisterBinding
import com.fdl.storyapp.utilities.Utils.showToast
import com.fdl.storyapp.utilities.ViewModelFactory


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        playAnimation()
    }

    private fun setupView() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()

            val message = getString(R.string.must_not_be_empty)

            if (name.isEmpty()) {
                binding.etNameRegister.error = message
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.etNameRegister.error = message
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etNameRegister.error = message
                return@setOnClickListener
            }

            viewModel.signup(name, email, password)
        }
    }

    private fun playAnimation() {
        val greeting = ObjectAnimator.ofFloat(binding.tvGreeting, View.ALPHA, 1F).setDuration(500L)
        val registerAlpha = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1F).setDuration(500L)
        val registerTranslateX = ObjectAnimator.ofFloat(binding.tvRegister, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val etNameAlpha = ObjectAnimator.ofFloat(binding.etNameRegister, View.ALPHA, 1F).setDuration(500L)
        val etNameTranslateX = ObjectAnimator.ofFloat(binding.etNameRegister, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val etEmailAlpha = ObjectAnimator.ofFloat(binding.etEmailRegister, View.ALPHA, 1F).setDuration(500L)
        val etEmailTranslateX = ObjectAnimator.ofFloat(binding.etEmailRegister, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val etPassAlpha = ObjectAnimator.ofFloat(binding.etPasswordRegister, View.ALPHA, 1F).setDuration(500L)
        val etPassTranslateX = ObjectAnimator.ofFloat(binding.etPasswordRegister, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val btnRegisterAlpha = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1F).setDuration(500L)
        val btnRegisterTranslateX = ObjectAnimator.ofFloat(binding.btnRegister, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val register = AnimatorSet().apply { playTogether(registerAlpha, registerTranslateX) }
        val name = AnimatorSet().apply { playTogether(etNameAlpha, etNameTranslateX) }
        val email = AnimatorSet().apply { playTogether(etEmailAlpha, etEmailTranslateX) }
        val password = AnimatorSet().apply { playTogether(etPassAlpha, etPassTranslateX) }
        val btnRegister = AnimatorSet().apply { playTogether(btnRegisterAlpha, btnRegisterTranslateX) }

        AnimatorSet().apply {
            playSequentially(greeting, register, name, email, password, btnRegister)
            start()
        }
    }

    private fun observeViewModel() {
        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showToast(requireContext(), getString(R.string.succes_regis))
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(requireContext(), message)
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
}