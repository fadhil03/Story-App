package com.fdl.storyapp.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.FragmentLoginBinding
import com.fdl.storyapp.utilities.Utils
import com.fdl.storyapp.utilities.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        playAnimation()
        observeViewModel()
    }

    private fun setupView() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            viewModel.login(email, password)
        }

        binding.tvOrRegis.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        validateForm()
    }

    private fun validateForm() {
        val email = MutableStateFlow("")
        val password = MutableStateFlow("")

        with(binding) {
            etEmail.doAfterTextChanged { text -> email.value = text.toString().trim() }
            etPassword.doAfterTextChanged { text -> password.value = text.toString().trim() }
        }

        lifecycleScope.launch {
            combine(email, password) { e, p ->
                Utils.validateEmail(e) && Utils.validatePassword(p)
            }.collect { isValid ->
                binding.btnLogin.isEnabled = isValid
            }
        }
    }

    private fun playAnimation() {
        val greeting = ObjectAnimator.ofFloat(binding.tvGreeting, View.ALPHA, 1F).setDuration(500L)
        val imgLogin = ObjectAnimator.ofFloat(binding.imgLogin, View.ALPHA, 1f).setDuration(500L)
        val loginAlpha = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1F).setDuration(500L)
        val loginTranslateX = ObjectAnimator.ofFloat(binding.tvLogin, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val etEmailAlpha = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1F).setDuration(500L)
        val etEmailTranslateX = ObjectAnimator.ofFloat(binding.etEmail, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val etPassAlpha = ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1F).setDuration(500L)
        val etPassTranslateX = ObjectAnimator.ofFloat(binding.etPassword, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val btnLoginAlpha = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1F).setDuration(500L)
        val btnLoginTranslateX = ObjectAnimator.ofFloat(binding.btnLogin, View.TRANSLATION_X, -100F, 0F).setDuration(500L)
        val orRegis = ObjectAnimator.ofFloat(binding.tvOrRegis, View.ALPHA, 1F).setDuration(500L)
        val login = AnimatorSet().apply { playTogether(loginAlpha, loginTranslateX) }
        val email = AnimatorSet().apply { playTogether(etEmailAlpha, etEmailTranslateX) }
        val password = AnimatorSet().apply { playTogether(etPassAlpha, etPassTranslateX) }
        val btnLogin = AnimatorSet().apply { playTogether(btnLoginAlpha, btnLoginTranslateX) }

        AnimatorSet().apply {
            playSequentially(greeting, imgLogin, login, email, password, btnLogin, orRegis)
            start()
        }
    }

    private fun observeViewModel() {
        viewModel.userModel.observe(viewLifecycleOwner) { userModel ->
            if (userModel?.isLoggedIn == true) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Utils.showToast(requireContext(), message)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { state ->
            showLoading(state)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}